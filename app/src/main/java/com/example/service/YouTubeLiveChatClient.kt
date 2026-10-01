package com.example.service

import com.example.data.CountryRepository
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
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

sealed class YouTubeConnectionState {
    object Disconnected : YouTubeConnectionState()
    object Connecting : YouTubeConnectionState()
    data class Connected(
        val videoId: String,
        val streamTitle: String,
        val channelTitle: String,
        val liveChatId: String
    ) : YouTubeConnectionState()
    data class Error(val message: String) : YouTubeConnectionState()
}

class YouTubeLiveChatClient(
    private val scope: CoroutineScope,
    private val countryListProvider: () -> List<CountryItem>
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<YouTubeConnectionState>(YouTubeConnectionState.Disconnected)
    val connectionState: StateFlow<YouTubeConnectionState> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<LiveEvent>(extraBufferCapacity = 128)
    val incomingEvents: SharedFlow<LiveEvent> = _incomingEvents.asSharedFlow()

    private var pollingJob: Job? = null
    private val processedMessageIds = HashSet<String>()
    private var nextPageToken: String? = null

    // Track user levels based on chat frequency
    private val userChatCounts = HashMap<String, Int>()

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

        disconnect()
        _connectionState.value = YouTubeConnectionState.Connecting

        scope.launch(Dispatchers.IO) {
            try {
                // Step 1: Fetch Video Details and Active Live Chat ID
                val videoUrl = "https://www.googleapis.com/youtube/v3/videos?part=snippet,liveStreamingDetails&id=$videoId&key=$apiKey"
                val request = Request.Builder().url(videoUrl).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        val errorBody = response.body?.string() ?: "Network error"
                        _connectionState.value = YouTubeConnectionState.Error("YouTube API Error (${response.code}): $errorBody")
                        return@launch
                    }

                    val responseString = response.body?.string() ?: ""
                    val json = JSONObject(responseString)
                    val items = json.optJSONArray("items")

                    if (items == null || items.length() == 0) {
                        _connectionState.value = YouTubeConnectionState.Error("Video not found or stream is private.")
                        return@launch
                    }

                    val videoItem = items.getJSONObject(0)
                    val snippet = videoItem.optJSONObject("snippet")
                    val streamTitle = snippet?.optString("title", "Live Stream") ?: "Live Stream"
                    val channelTitle = snippet?.optString("channelTitle", "") ?: ""

                    val liveDetails = videoItem.optJSONObject("liveStreamingDetails")
                    val liveChatId = liveDetails?.optString("activeLiveChatId", "") ?: ""

                    if (liveChatId.isBlank()) {
                        _connectionState.value = YouTubeConnectionState.Error("This video does not have an active Live Chat. Ensure the stream is currently LIVE.")
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        _connectionState.value = YouTubeConnectionState.Connected(
                            videoId = videoId,
                            streamTitle = streamTitle,
                            channelTitle = channelTitle,
                            liveChatId = liveChatId
                        )
                    }

                    // Step 2: Start Polling Live Chat Messages
                    startPollingChat(liveChatId, apiKey)
                }
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
            while (true) {
                var pollingDelayMs = 2500L
                try {
                    val pageParam = if (nextPageToken != null) "&pageToken=$nextPageToken" else ""
                    val chatUrl = "https://www.googleapis.com/youtube/v3/liveChat/messages?liveChatId=$liveChatId&part=snippet,authorDetails&key=$apiKey$pageParam"
                    val request = Request.Builder().url(chatUrl).build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string() ?: ""
                            val json = JSONObject(responseBody)

                            nextPageToken = json.optString("nextPageToken", null)
                            val pollInterval = json.optLong("pollingIntervalMillis", 2500L)
                            if (pollInterval in 1000L..10000L) {
                                pollingDelayMs = pollInterval
                            }

                            val items = json.optJSONArray("items")
                            if (items != null) {
                                for (i in 0 until items.length()) {
                                    val item = items.getJSONObject(i)
                                    val messageId = item.optString("id", "")
                                    if (messageId.isNotEmpty() && !processedMessageIds.contains(messageId)) {
                                        processedMessageIds.add(messageId)

                                        val authorDetails = item.optJSONObject("authorDetails")
                                        val authorName = authorDetails?.optString("displayName", "Viewer") ?: "Viewer"

                                        val snippet = item.optJSONObject("snippet")
                                        val displayMessage = snippet?.optString("displayMessage", "") ?: ""

                                        processComment(authorName, displayMessage)
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Transient network hiccup; retry after delay
                    delay(3000L)
                }

                delay(pollingDelayMs)
            }
        }
    }

    /**
     * Processes any incoming comment text:
     * Matches country name or flag, awards points, triggers coin burst and emits event.
     */
    fun processComment(authorName: String, commentText: String) {
        val countries = countryListProvider()
        val detectedCountry = CountryDetector.detectCountry(commentText, countries) ?: return

        val count = (userChatCounts[authorName] ?: 0) + 1
        userChatCounts[authorName] = count

        // Level grows with chats: 1..5
        val level = (1 + (count / 4)).coerceAtMost(5)

        val isLikeSub = CountryDetector.isLikeAndSubscribe(commentText)
        val points = if (isLikeSub) 400L else level.toLong()

        val event = LiveEvent(
            type = if (isLikeSub) LiveEventType.LIKE_SUBSCRIBE else LiveEventType.COMMENT,
            username = authorName,
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
