package com.example.service

import com.example.model.CountryItem
import com.example.model.LiveEvent
import com.example.model.LiveEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class YouTubeConnectionState {
    object Disconnected : YouTubeConnectionState()
    object Connecting : YouTubeConnectionState()
    data class Connected(
        val videoTitle: String,
        val channelName: String,
        val liveChatId: String,
        val viewerCount: String = "LIVE",
        val streamTitle: String = videoTitle
    ) : YouTubeConnectionState()
    data class Error(val message: String) : YouTubeConnectionState()
}

class YouTubeLiveChatClient(
    private val scope: CoroutineScope,
    private val countryListProvider: () -> List<CountryItem>
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<YouTubeConnectionState>(YouTubeConnectionState.Disconnected)
    val connectionState: StateFlow<YouTubeConnectionState> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<LiveEvent>(extraBufferCapacity = 256)
    val incomingEvents: SharedFlow<LiveEvent> = _incomingEvents.asSharedFlow()

    private val _totalMessagesProcessed = MutableStateFlow(0)
    val totalMessagesProcessed: StateFlow<Int> = _totalMessagesProcessed.asStateFlow()

    private var pollingJob: Job? = null
    private val processedMessageIds = HashSet<String>()
    private var nextPageToken: String? = null

    // Track user levels based on chat frequency
    private val userChatCounts = HashMap<String, Int>()
    // Remember country assigned to user across multiple chats/super chats
    private val userAssignedCountries = HashMap<String, String>()

    private var lastConnectedUrl: String = ""
    private var lastApiKey: String = ""

    /**
     * Extracts YouTube Video ID from standard YouTube URLs or direct ID
     */
    fun extractVideoId(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.matches(Regex("^[a-zA-Z0-9_-]{11}$"))) {
            return trimmed
        }

        val patterns = listOf(
            "(?:https?://)?(?:www\\.)?youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})",
            "(?:https?://)?(?:www\\.)?youtube\\.com/live/([a-zA-Z0-9_-]{11})",
            "(?:https?://)?(?:www\\.)?youtu\\.be/([a-zA-Z0-9_-]{11})",
            "(?:https?://)?(?:www\\.)?youtube\\.com/embed/([a-zA-Z0-9_-]{11})"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(trimmed)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }

        return null
    }

    /**
     * Connects to YouTube Live Stream using Video ID and Google Data API Key
     */
    fun connect(videoIdOrUrl: String, apiKey: String) {
        val videoId = extractVideoId(videoIdOrUrl)
        if (videoId == null) {
            _connectionState.value = YouTubeConnectionState.Error("Invalid YouTube URL or Video ID. Example: youtube.com/live/xyz123")
            return
        }

        if (apiKey.isBlank()) {
            _connectionState.value = YouTubeConnectionState.Error("Please enter your YouTube Data API v3 Key.")
            return
        }

        lastConnectedUrl = videoIdOrUrl
        lastApiKey = apiKey
        _connectionState.value = YouTubeConnectionState.Connecting

        scope.launch(Dispatchers.IO) {
            try {
                // Step 1: Query YouTube Data API for live stream details & active liveChatId
                val videoDetailsUrl = "https://www.googleapis.com/youtube/v3/videos?id=$videoId&part=snippet,liveStreamingDetails&key=$apiKey"
                val request = Request.Builder().url(videoDetailsUrl).build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string() ?: ""
                    _connectionState.value = YouTubeConnectionState.Error(
                        if (code == 403) "YouTube API Quota exceeded or invalid API Key."
                        else "Failed to connect to YouTube (HTTP $code): $errorBody"
                    )
                    return@launch
                }

                val bodyStr = response.body?.string() ?: ""
                val json = JSONObject(bodyStr)
                val items = json.optJSONArray("items")

                if (items == null || items.length() == 0) {
                    _connectionState.value = YouTubeConnectionState.Error("Video not found. Please verify the link.")
                    return@launch
                }

                val videoItem = items.getJSONObject(0)
                val snippet = videoItem.optJSONObject("snippet")
                val title = snippet?.optString("title", "YouTube Live Stream") ?: "YouTube Live Stream"
                val channelTitle = snippet?.optString("channelTitle", "Host Channel") ?: "Host Channel"

                val liveStreamingDetails = videoItem.optJSONObject("liveStreamingDetails")
                val liveChatId = liveStreamingDetails?.optString("activeLiveChatId", null)

                if (liveChatId.isNullOrEmpty()) {
                    // Not currently streaming live or live chat is disabled
                    _connectionState.value = YouTubeConnectionState.Error("Live chat is not active or video is not a live stream.")
                    return@launch
                }

                _connectionState.value = YouTubeConnectionState.Connected(
                    videoTitle = title,
                    channelName = channelTitle,
                    liveChatId = liveChatId
                )

                // Step 2: Start polling live chat messages automatically
                startPollingChat(liveChatId, apiKey)
            } catch (e: Exception) {
                _connectionState.value = YouTubeConnectionState.Error("Connection failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    private fun startPollingChat(liveChatId: String, apiKey: String) {
        pollingJob?.cancel()
        processedMessageIds.clear()
        nextPageToken = null

        pollingJob = scope.launch(Dispatchers.IO) {
            var consecutiveErrors = 0
            while (true) {
                var pollingDelayMs = 2000L
                try {
                    val pageParam = if (nextPageToken != null) "&pageToken=$nextPageToken" else ""
                    val chatUrl = "https://www.googleapis.com/youtube/v3/liveChat/messages?liveChatId=$liveChatId&part=snippet,authorDetails&key=$apiKey$pageParam"
                    val request = Request.Builder().url(chatUrl).build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            consecutiveErrors = 0
                            val responseBody = response.body?.string() ?: ""
                            val json = JSONObject(responseBody)

                            nextPageToken = json.optString("nextPageToken", null)
                            val pollInterval = json.optLong("pollingIntervalMillis", 2000L)
                            if (pollInterval in 500L..10000L) {
                                pollingDelayMs = pollInterval
                            }

                            val items = json.optJSONArray("items")
                            if (items != null) {
                                for (i in 0 until items.length()) {
                                    val item = items.getJSONObject(i)
                                    val messageId = item.optString("id", "")
                                    if (messageId.isNotEmpty() && !processedMessageIds.contains(messageId)) {
                                        processedMessageIds.add(messageId)
                                        _totalMessagesProcessed.value++

                                        val authorDetails = item.optJSONObject("authorDetails")
                                        val authorName = authorDetails?.optString("displayName", "Viewer") ?: "Viewer"
                                        val channelId = authorDetails?.optString("channelId", "") ?: ""
                                        val profileImageUrl = authorDetails?.optString("profileImageUrl", "") ?: ""

                                        val snippet = item.optJSONObject("snippet")
                                        val eventType = snippet?.optString("type", "textMessageEvent") ?: "textMessageEvent"
                                        val displayMessage = snippet?.optString("displayMessage", "") ?: ""

                                        processYouTubeMessage(
                                            authorName = authorName,
                                            channelId = channelId,
                                            profileImageUrl = profileImageUrl,
                                            eventType = eventType,
                                            snippet = snippet,
                                            displayMessage = displayMessage
                                        )
                                    }
                                }
                            }
                        } else {
                            consecutiveErrors++
                            if (consecutiveErrors >= 6) {
                                val code = response.code
                                if (code == 404 || code == 403) {
                                    _connectionState.value = YouTubeConnectionState.Error("Stream ended or API quota reached (HTTP $code)")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    consecutiveErrors++
                    // Auto-reconnect / retry after short delay for network stability
                    delay(3000L)
                }

                delay(pollingDelayMs)
            }
        }
    }

    /**
     * Automatically dispatches YouTube message:
     * - Super Chat: 1000 points per dollar ($1 = 1000, $5 = 5000)
     * - Gifts / Super Stickers: 50 points
     * - Comments: Country detection and points
     */
    fun processYouTubeMessage(
        authorName: String,
        channelId: String,
        profileImageUrl: String,
        eventType: String,
        snippet: JSONObject?,
        displayMessage: String
    ) {
        val countries = countryListProvider()
        if (countries.isEmpty()) return

        val count = (userChatCounts[authorName] ?: 0) + 1
        userChatCounts[authorName] = count
        val level = (1 + (count / 4)).coerceAtMost(5)

        val previousCountryId = userAssignedCountries[authorName]

        when (eventType) {
            "superChatEvent" -> {
                val superChatDetails = snippet?.optJSONObject("superChatDetails")
                val amountMicros = superChatDetails?.optLong("amountMicros", 1_000_000L) ?: 1_000_000L
                val dollarAmount = (amountMicros / 1_000_000.0).coerceAtLeast(1.0)
                val userComment = superChatDetails?.optString("userComment", "") ?: displayMessage
                val commentToSearch = if (userComment.isNotBlank()) userComment else displayMessage

                val detectedCountry = CountryDetector.detectCountryWithAuthor(commentToSearch, authorName, countries)
                    ?: countries.find { it.id == previousCountryId }
                    ?: countries.firstOrNull() ?: return

                userAssignedCountries[authorName] = detectedCountry.id
                val points = (dollarAmount * 1000).toLong() // 1000 points per dollar!

                val event = LiveEvent(
                    type = LiveEventType.SUPER_CHAT,
                    username = authorName,
                    userId = channelId.ifBlank { "@$authorName" },
                    userAvatarUrl = profileImageUrl,
                    countryId = detectedCountry.id,
                    countryName = detectedCountry.name,
                    countryFlag = detectedCountry.flag,
                    level = 5,
                    points = points,
                    dollarAmount = dollarAmount,
                    message = if (userComment.isNotBlank()) userComment else "Super Chat $$dollarAmount!"
                )
                _incomingEvents.tryEmit(event)
            }
            "superStickerEvent", "membershipGiftingEvent", "giftMembershipReceivedEvent" -> {
                val detectedCountry = CountryDetector.detectCountryWithAuthor(displayMessage, authorName, countries)
                    ?: countries.find { it.id == previousCountryId }
                    ?: countries.firstOrNull() ?: return

                userAssignedCountries[authorName] = detectedCountry.id
                val points = 50L // 50 points per gift!

                val event = LiveEvent(
                    type = LiveEventType.GIFT,
                    username = authorName,
                    userId = channelId.ifBlank { "@$authorName" },
                    userAvatarUrl = profileImageUrl,
                    countryId = detectedCountry.id,
                    countryName = detectedCountry.name,
                    countryFlag = detectedCountry.flag,
                    level = 3,
                    points = points,
                    message = displayMessage.ifBlank { "Sent a Gift! 🎁" }
                )
                _incomingEvents.tryEmit(event)
            }
            else -> {
                // Regular comment
                processComment(authorName, displayMessage, channelId, profileImageUrl)
            }
        }
    }

    fun processComment(
        authorName: String,
        commentText: String,
        channelId: String = "",
        profileImageUrl: String = ""
    ) {
        val countries = countryListProvider()
        if (countries.isEmpty()) return

        val previousCountryId = userAssignedCountries[authorName]

        // Intelligent country detection:
        // 1. In comment text (name, alias, or flag)
        // 2. In author name (e.g. Alex_USA, Kenji_JP)
        // 3. Previously assigned country for this viewer
        // 4. If generic cheer ("+1", "boost", "let's go"), assign to previous country or current leader
        val detectedCountry = CountryDetector.detectCountry(commentText, countries)
            ?: CountryDetector.detectCountry(authorName, countries)
            ?: countries.find { it.id == previousCountryId }
            ?: if (CountryDetector.isGenericCheer(commentText)) {
                countries.find { it.id == previousCountryId } ?: countries.first()
            } else {
                null
            } ?: return

        userAssignedCountries[authorName] = detectedCountry.id

        val count = (userChatCounts[authorName] ?: 0) + 1
        userChatCounts[authorName] = count

        val level = (1 + (count / 4)).coerceAtMost(5)
        val isLikeSub = CountryDetector.isLikeAndSubscribe(commentText)
        val points = if (isLikeSub) 400L else level.toLong()

        val event = LiveEvent(
            type = if (isLikeSub) LiveEventType.LIKE_SUBSCRIBE else LiveEventType.COMMENT,
            username = authorName,
            userId = channelId.ifBlank { "@$authorName" },
            userAvatarUrl = profileImageUrl,
            countryId = detectedCountry.id,
            countryName = detectedCountry.name,
            countryFlag = detectedCountry.flag,
            level = level,
            points = points,
            message = commentText
        )

        _incomingEvents.tryEmit(event)
    }

    fun disconnect() {
        pollingJob?.cancel()
        pollingJob = null
        _connectionState.value = YouTubeConnectionState.Disconnected
    }
}
