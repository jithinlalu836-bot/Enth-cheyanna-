package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleAuthManager
import com.example.auth.GoogleAuthState
import com.example.data.CountryRepository
import com.example.model.Chatter
import com.example.model.CoinBurstData
import com.example.model.CommentBannerData
import com.example.model.CountryItem
import com.example.model.LiveEvent
import com.example.model.LiveEventType
import com.example.model.RoundRecord
import com.example.service.MockLiveChatSimulator
import com.example.service.YouTubeConnectionState
import com.example.service.YouTubeLiveChatClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LiveOverlayViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("boost_your_country_prefs", Context.MODE_PRIVATE)

    private val _countries = MutableStateFlow<List<CountryItem>>(emptyList())
    val countries: StateFlow<List<CountryItem>> = _countries.asStateFlow()

    private val _topChatters = MutableStateFlow<List<Chatter>>(emptyList())
    val topChatters: StateFlow<List<Chatter>> = _topChatters.asStateFlow()

    private val _latestEvent = MutableStateFlow<LiveEvent?>(null)
    val latestEvent: StateFlow<LiveEvent?> = _latestEvent.asStateFlow()

    private val _eventHistory = MutableStateFlow<List<LiveEvent>>(emptyList())
    val eventHistory: StateFlow<List<LiveEvent>> = _eventHistory.asStateFlow()

    private val _coinBursts = MutableStateFlow<List<CoinBurstData>>(emptyList())
    val coinBursts: StateFlow<List<CoinBurstData>> = _coinBursts.asStateFlow()

    private val _isSimulationRunning = MutableStateFlow(true)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    private val _simulationSpeed = MutableStateFlow(1400L)
    val simulationSpeed: StateFlow<Long> = _simulationSpeed.asStateFlow()

    private val _isTransparentBackground = MutableStateFlow(false)
    val isTransparentBackground: StateFlow<Boolean> = _isTransparentBackground.asStateFlow()

    // Configurable Scoring & Anti-Spam
    private val _isScoringActive = MutableStateFlow(true)
    val isScoringActive: StateFlow<Boolean> = _isScoringActive.asStateFlow()

    private val _pointsPerComment = MutableStateFlow(prefs.getLong("points_per_comment", 1L))
    val pointsPerComment: StateFlow<Long> = _pointsPerComment.asStateFlow()

    private val _superChatPointsPerDollar = MutableStateFlow(prefs.getLong("sc_points_per_dollar", 1000L))
    val superChatPointsPerDollar: StateFlow<Long> = _superChatPointsPerDollar.asStateFlow()

    private val _giftPoints = MutableStateFlow(prefs.getLong("gift_points", 50L))
    val giftPoints: StateFlow<Long> = _giftPoints.asStateFlow()

    private val _blockedViewers = MutableStateFlow(
        prefs.getStringSet("blocked_viewers", emptySet())?.toSet() ?: emptySet()
    )
    val blockedViewers: StateFlow<Set<String>> = _blockedViewers.asStateFlow()

    // Round System
    private val _roundNumber = MutableStateFlow(prefs.getInt("round_number", 1))
    val roundNumber: StateFlow<Int> = _roundNumber.asStateFlow()

    private val _savedRounds = MutableStateFlow<List<RoundRecord>>(emptyList())
    val savedRounds: StateFlow<List<RoundRecord>> = _savedRounds.asStateFlow()

    private val _totalRoundDuration = MutableStateFlow(300)
    val totalRoundDuration: StateFlow<Int> = _totalRoundDuration.asStateFlow()

    private val _roundTimeRemaining = MutableStateFlow(300)
    val roundTimeRemaining: StateFlow<Int> = _roundTimeRemaining.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(true)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _isRoundFinished = MutableStateFlow(false)
    val isRoundFinished: StateFlow<Boolean> = _isRoundFinished.asStateFlow()

    // 2-second live comment alert banner
    private val _activeCommentBanner = MutableStateFlow<CommentBannerData?>(null)
    val activeCommentBanner: StateFlow<CommentBannerData?> = _activeCommentBanner.asStateFlow()

    private var commentBannerJob: Job? = null
    private var bannerIdCounter = 0L

    private var roundTimerJob: Job? = null
    private var burstIdCounter = 0L

    // Anti-spam recent timestamp tracker (username/userId -> lastMessageTime)
    private val recentUserMessageTimes = HashMap<String, Long>()

    val youTubeClient = YouTubeLiveChatClient(
        scope = viewModelScope,
        countryListProvider = { _countries.value }
    )
    val youTubeConnectionState: StateFlow<YouTubeConnectionState> = youTubeClient.connectionState

    val googleAuthManager = GoogleAuthManager(application)
    val googleAuthState: StateFlow<GoogleAuthState> = googleAuthManager.authState

    private val _savedYouTubeUrl = MutableStateFlow(prefs.getString("youtube_url", "") ?: "")
    val savedYouTubeUrl: StateFlow<String> = _savedYouTubeUrl.asStateFlow()

    private val _savedYouTubeApiKey = MutableStateFlow(
        prefs.getString("youtube_api_key", "")?.ifBlank { googleAuthManager.defaultApiKey } ?: googleAuthManager.defaultApiKey
    )
    val savedYouTubeApiKey: StateFlow<String> = _savedYouTubeApiKey.asStateFlow()

    private val simulator = MockLiveChatSimulator(
        scope = viewModelScope,
        countryProvider = {
            _countries.value.map { Pair(it.id, Pair(it.name, it.flag)) }
        }
    )

    init {
        loadPersistedState()

        // Start active countdown timer by default (5 minutes = 300s)
        startCountdownTimer(300)

        // Listen for live events from simulator
        viewModelScope.launch {
            simulator.eventsFlow.collect { event ->
                handleIncomingEvent(event)
            }
        }

        // Listen for live events from real YouTube Live Stream
        viewModelScope.launch {
            youTubeClient.incomingEvents.collect { event ->
                handleIncomingEvent(event)
            }
        }

        // Automatically connect to saved YouTube Live Stream during startup
        val autoUrl = _savedYouTubeUrl.value
        val autoKey = _savedYouTubeApiKey.value
        if (autoUrl.isNotBlank() && autoKey.isNotBlank()) {
            connectYouTube(autoUrl, autoKey)
        }

        // Automatically manage simulation when live stream connects
        viewModelScope.launch {
            youTubeConnectionState.collect { state ->
                if (state is YouTubeConnectionState.Connected) {
                    if (_isSimulationRunning.value) {
                        _isSimulationRunning.value = false
                        simulator.stop()
                    }
                }
            }
        }
    }

    private fun loadPersistedState() {
        val initialList = CountryRepository.allCountries.map { country ->
            val persisted = prefs.getLong("country_score_${country.id}", country.score)
            country.copy(score = persisted)
        }
        reRankAndPublish(initialList)
        _topChatters.value = CountryRepository.initialTopChatters
    }

    private fun persistScore(countryId: String, score: Long) {
        prefs.edit().putLong("country_score_$countryId", score).apply()
    }

    private fun reRankAndPublish(list: List<CountryItem>) {
        val sorted = list.sortedWith(
            compareByDescending<CountryItem> { it.score }
                .thenBy { it.name }
        )
        val ranked = sorted.mapIndexed { index, item ->
            item.copy(
                previousRank = item.rank,
                rank = index + 1
            )
        }
        _countries.value = ranked
    }

    private fun handleIncomingEvent(event: LiveEvent) {
        // Anti-Spam Check 1: Scoring active
        if (!_isScoringActive.value) return

        // Anti-Spam Check 2: Blocked Viewer Check
        val isBlocked = _blockedViewers.value.any { blocked ->
            blocked.equals(event.username, ignoreCase = true) ||
                    (event.userId.isNotBlank() && blocked.equals(event.userId, ignoreCase = true))
        }
        if (isBlocked) return

        // Anti-Spam Check 3: Rate Limiting (max 1 message per 350ms per user)
        val now = System.currentTimeMillis()
        val lastTime = recentUserMessageTimes[event.username] ?: 0L
        if (now - lastTime < 350L) return
        recentUserMessageTimes[event.username] = now

        // Calculate points based on event type and admin configurations
        val effectivePoints = when (event.type) {
            LiveEventType.SUPER_CHAT -> {
                // 1000 points per dollar!
                val dollars = if (event.dollarAmount > 0.0) event.dollarAmount else 1.0
                (dollars * _superChatPointsPerDollar.value).toLong().coerceAtLeast(_superChatPointsPerDollar.value)
            }
            LiveEventType.GIFT -> {
                // 50 points on gift!
                _giftPoints.value
            }
            LiveEventType.LIKE_SUBSCRIBE -> 400L
            LiveEventType.COMMENT -> {
                // Configurable points per comment (default 1 pt * level)
                _pointsPerComment.value * event.level.coerceAtLeast(1)
            }
        }

        val finalizedEvent = event.copy(points = effectivePoints)
        _latestEvent.value = finalizedEvent

        // Record in comment history (last 50 events)
        _eventHistory.update { (listOf(finalizedEvent) + it).take(50) }

        // Update country score
        _countries.update { currentList ->
            val updated = currentList.map { country ->
                if (country.id == finalizedEvent.countryId) {
                    val newScore = country.score + finalizedEvent.points
                    persistScore(country.id, newScore)
                    country.copy(score = newScore)
                } else country
            }
            val sorted = updated.sortedWith(
                compareByDescending<CountryItem> { it.score }
                    .thenBy { it.name }
            )
            sorted.mapIndexed { index, country ->
                country.copy(
                    previousRank = country.rank,
                    rank = index + 1
                )
            }
        }

        // Update Top Chatter statistics
        updateChatter(finalizedEvent.username, finalizedEvent.countryId, finalizedEvent.countryFlag, finalizedEvent.level, finalizedEvent.points)

        // Trigger CoinBurst animation from the country flag
        spawnCoinBurst(
            countryId = finalizedEvent.countryId,
            countryFlag = finalizedEvent.countryFlag,
            username = finalizedEvent.username,
            points = finalizedEvent.points,
            isSuperChat = finalizedEvent.type == LiveEventType.SUPER_CHAT
        )

        // Show live comment banner for 2 seconds (with User ID on top for Super Chat & Gift)
        showCommentBanner(finalizedEvent)
    }

    private fun showCommentBanner(event: LiveEvent) {
        commentBannerJob?.cancel()
        val banner = CommentBannerData(
            id = ++bannerIdCounter,
            username = event.username,
            userId = event.userId.ifBlank { "@${event.username}" },
            userAvatarUrl = event.userAvatarUrl,
            message = event.message.ifBlank { "Boosted ${event.countryName}! +${event.points}" },
            countryFlag = event.countryFlag,
            countryName = event.countryName,
            points = event.points,
            level = event.level,
            type = event.type,
            dollarAmount = event.dollarAmount,
            isLikeSub = event.type == LiveEventType.LIKE_SUBSCRIBE
        )
        _activeCommentBanner.value = banner
        commentBannerJob = viewModelScope.launch {
            delay(2000L) // 2-second duration
            if (_activeCommentBanner.value?.id == banner.id) {
                _activeCommentBanner.value = null
            }
        }
    }

    private fun updateChatter(
        username: String,
        countryId: String,
        countryFlag: String,
        level: Int,
        points: Long
    ) {
        _topChatters.update { currentList ->
            val existing = currentList.find { it.username.equals(username, ignoreCase = true) }
            val updated = if (existing != null) {
                currentList.map {
                    if (it.username.equals(username, ignoreCase = true)) {
                        it.copy(
                            chatCount = it.chatCount + 1,
                            pointsEarned = it.pointsEarned + points,
                            level = maxOf(it.level, level)
                        )
                    } else it
                }
            } else {
                val initials = username.take(2).uppercase()
                val newChatter = Chatter(
                    username = username,
                    initials = initials,
                    countryId = countryId,
                    countryFlag = countryFlag,
                    level = level,
                    chatCount = 1,
                    pointsEarned = points
                )
                currentList + newChatter
            }
            // Keep top 5 sorted by points earned descending
            updated.sortedByDescending { it.pointsEarned }.take(5)
        }
    }

    private fun spawnCoinBurst(countryId: String, countryFlag: String, username: String, points: Long, isSuperChat: Boolean = false) {
        val index = _countries.value.indexOfFirst { it.id == countryId }.coerceAtLeast(0)
        val col = index % 9
        val row = index / 9

        val xPercent = (col.toFloat() + 0.5f) / 9f
        val yPercent = 0.40f + (row.toFloat() * 0.05f).coerceAtMost(0.48f)

        val burst = CoinBurstData(
            id = ++burstIdCounter,
            countryId = countryId,
            countryFlag = countryFlag,
            text = "+$points",
            username = username,
            xPercent = xPercent,
            yPercent = yPercent,
            coinCount = if (isSuperChat) 24 else if (points >= 400) 14 else 6
        )

        _coinBursts.update { (it + burst).takeLast(12) }

        viewModelScope.launch {
            delay(1200)
            _coinBursts.update { current -> current.filter { it.id != burst.id } }
        }
    }

    // Trigger Actions
    fun triggerCommentAction() {
        val target = _countries.value.firstOrNull() ?: return
        viewModelScope.launch {
            simulator.triggerManualEvent(
                type = LiveEventType.COMMENT,
                countryId = target.id,
                username = "You",
                level = 5
            )
        }
    }

    fun triggerLikeSubscribeAction() {
        val target = _countries.value.firstOrNull() ?: return
        viewModelScope.launch {
            simulator.triggerManualEvent(
                type = LiveEventType.LIKE_SUBSCRIBE,
                countryId = target.id,
                username = "You",
                level = 1
            )
        }
    }

    fun triggerSuperChatAction(dollarAmount: Double = 5.0) {
        val target = _countries.value.firstOrNull() ?: return
        viewModelScope.launch {
            simulator.triggerManualSuperChat(
                countryId = target.id,
                username = "TopSupporter",
                dollarAmount = dollarAmount,
                message = "Super Chat $$dollarAmount for ${target.name}! 🔥"
            )
        }
    }

    fun triggerGiftAction() {
        val target = _countries.value.firstOrNull() ?: return
        viewModelScope.launch {
            simulator.triggerManualGift(
                countryId = target.id,
                username = "GiftSender",
                message = "Sent a Gift for ${target.name}! 🎁"
            )
        }
    }

    fun onCountryClick(country: CountryItem) {
        viewModelScope.launch {
            simulator.triggerManualEvent(
                type = LiveEventType.COMMENT,
                countryId = country.id,
                username = "You",
                level = 5
            )
        }
    }

    fun toggleSimulation() {
        _isSimulationRunning.update { current ->
            val newState = !current
            if (newState) {
                simulator.start()
            } else {
                simulator.stop()
            }
            newState
        }
    }

    fun setSimulationSpeed(speedMillis: Long) {
        _simulationSpeed.value = speedMillis
        simulator.intervalMillis = speedMillis
    }

    fun toggleTransparentBackground(isTransparent: Boolean) {
        _isTransparentBackground.value = isTransparent
    }

    // Admin & Scoring Controls
    fun toggleScoring(active: Boolean) {
        _isScoringActive.value = active
    }

    fun setPointsPerComment(points: Long) {
        _pointsPerComment.value = points
        prefs.edit().putLong("points_per_comment", points).apply()
    }

    fun setSuperChatPointsPerDollar(multiplier: Long) {
        _superChatPointsPerDollar.value = multiplier
        prefs.edit().putLong("sc_points_per_dollar", multiplier).apply()
    }

    fun setGiftPoints(points: Long) {
        _giftPoints.value = points
        prefs.edit().putLong("gift_points", points).apply()
    }

    fun manuallyAdjustScore(countryId: String, delta: Long) {
        _countries.update { currentList ->
            val updated = currentList.map { country ->
                if (country.id == countryId) {
                    val newScore = maxOf(0L, country.score + delta)
                    persistScore(country.id, newScore)
                    country.copy(score = newScore)
                } else country
            }
            val sorted = updated.sortedWith(
                compareByDescending<CountryItem> { it.score }
                    .thenBy { it.name }
            )
            sorted.mapIndexed { index, country ->
                country.copy(
                    previousRank = country.rank,
                    rank = index + 1
                )
            }
        }
    }

    fun changeViewerCountry(username: String, newCountryId: String) {
        val targetCountry = _countries.value.find { it.id == newCountryId } ?: return
        _topChatters.update { list ->
            list.map {
                if (it.username.equals(username, ignoreCase = true)) {
                    it.copy(countryId = targetCountry.id, countryFlag = targetCountry.flag)
                } else it
            }
        }
    }

    fun blockViewer(identifier: String) {
        val trimmed = identifier.trim()
        if (trimmed.isBlank()) return
        val updated = _blockedViewers.value + trimmed
        _blockedViewers.value = updated
        prefs.edit().putStringSet("blocked_viewers", updated).apply()
    }

    fun unblockViewer(identifier: String) {
        val updated = _blockedViewers.value - identifier
        _blockedViewers.value = updated
        prefs.edit().putStringSet("blocked_viewers", updated).apply()
    }

    // Round System
    fun setRoundTimerMinutes(minutes: Int?) {
        if (minutes == null) {
            roundTimerJob?.cancel()
            _isTimerRunning.value = false
            return
        }
        startCountdownTimer(minutes * 60)
    }

    fun startCountdownTimer(seconds: Int = 300) {
        roundTimerJob?.cancel()
        _totalRoundDuration.value = seconds
        _roundTimeRemaining.value = seconds
        _isTimerRunning.value = true
        _isRoundFinished.value = false
        runTimerLoop()
    }

    fun toggleTimerPause() {
        val willRun = !_isTimerRunning.value
        _isTimerRunning.value = willRun
        if (willRun) {
            runTimerLoop()
        } else {
            roundTimerJob?.cancel()
        }
    }

    private fun runTimerLoop() {
        roundTimerJob?.cancel()
        roundTimerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _roundTimeRemaining.value > 0) {
                delay(1000)
                if (_roundTimeRemaining.value > 0) {
                    _roundTimeRemaining.update { it - 1 }
                }
            }
            if (_roundTimeRemaining.value <= 0) {
                _isRoundFinished.value = true
                _isTimerRunning.value = false
                saveCurrentRoundResult()
            }
        }
    }

    fun addExtraTime(seconds: Int = 60) {
        _totalRoundDuration.update { it + seconds }
        _roundTimeRemaining.update { it + seconds }
        _isRoundFinished.value = false
        if (!_isTimerRunning.value) {
            toggleTimerPause()
        }
    }

    fun startNewRound() {
        saveCurrentRoundResult()
        _roundNumber.update { it + 1 }
        prefs.edit().putInt("round_number", _roundNumber.value).apply()
        resetAllScores()
        startCountdownTimer(300)
    }

    private fun saveCurrentRoundResult() {
        val winner = _countries.value.firstOrNull() ?: return
        val record = RoundRecord(
            roundNumber = _roundNumber.value,
            winnerName = winner.name,
            winnerFlag = winner.flag,
            winningScore = winner.score
        )
        _savedRounds.update { (listOf(record) + it).take(20) }
    }

    fun signInAndConnectYouTube(
        activity: Activity,
        videoIdOrUrl: String,
        apiKey: String = googleAuthManager.defaultApiKey
    ) {
        viewModelScope.launch {
            val result = googleAuthManager.signInWithGoogle(activity)
            if (result.isSuccess) {
                val effectiveKey = apiKey.ifBlank { googleAuthManager.defaultApiKey }
                val videoId = youTubeClient.extractVideoId(videoIdOrUrl)
                if (videoId != null) {
                    val streamResult = googleAuthManager.retrieveLiveChatStreamId(videoId, effectiveKey)
                    if (streamResult.isSuccess) {
                        connectYouTube(videoId, effectiveKey)
                    } else {
                        connectYouTube(videoId, effectiveKey)
                    }
                } else if (_savedYouTubeUrl.value.isNotBlank()) {
                    connectYouTube(_savedYouTubeUrl.value, effectiveKey)
                }
            }
        }
    }

    fun signOutGoogle() {
        googleAuthManager.signOut()
    }

    fun connectYouTube(urlOrId: String, apiKey: String) {
        prefs.edit()
            .putString("youtube_url", urlOrId)
            .putString("youtube_api_key", apiKey)
            .apply()
        _savedYouTubeUrl.value = urlOrId
        _savedYouTubeApiKey.value = apiKey
        youTubeClient.connect(urlOrId, apiKey)
    }

    fun disconnectYouTube() {
        youTubeClient.disconnect()
    }

    fun testCommentFromYouTube(author: String, comment: String) {
        youTubeClient.processComment(author, comment)
    }

    fun simulateTestComment(author: String, comment: String) {
        testCommentFromYouTube(author, comment)
    }

    fun resetAllScores() {
        val resetList = CountryRepository.allCountries.map {
            persistScore(it.id, 0L)
            it.copy(score = 0L, rank = 1, previousRank = 1)
        }
        reRankAndPublish(resetList)
        _topChatters.value = CountryRepository.initialTopChatters
    }

    override fun onCleared() {
        super.onCleared()
        simulator.stop()
        youTubeClient.disconnect()
        roundTimerJob?.cancel()
        commentBannerJob?.cancel()
    }
}
