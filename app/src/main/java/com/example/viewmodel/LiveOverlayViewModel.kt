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

    private val _coinBursts = MutableStateFlow<List<CoinBurstData>>(emptyList())
    val coinBursts: StateFlow<List<CoinBurstData>> = _coinBursts.asStateFlow()

    private val _isSimulationRunning = MutableStateFlow(true)
    val isSimulationRunning: StateFlow<Boolean> = _isSimulationRunning.asStateFlow()

    private val _simulationSpeed = MutableStateFlow(1400L)
    val simulationSpeed: StateFlow<Long> = _simulationSpeed.asStateFlow()

    private val _isTransparentBackground = MutableStateFlow(false)
    val isTransparentBackground: StateFlow<Boolean> = _isTransparentBackground.asStateFlow()

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

        simulator.intervalMillis = _simulationSpeed.value
        simulator.start()
        _isSimulationRunning.value = true
    }

    private fun loadPersistedState() {
        // Load countries from repo and check if persisted scores exist
        val initialList = CountryRepository.allCountries.map { base ->
            val savedScore = prefs.getLong("score_${base.id}", -1L)
            if (savedScore != -1L) {
                base.copy(score = savedScore)
            } else base
        }

        _topChatters.value = CountryRepository.initialTopChatters
        reRankAndPublish(initialList)
    }

    private fun persistScore(countryId: String, score: Long) {
        prefs.edit().putLong("score_$countryId", score).apply()
    }

    private fun reRankAndPublish(list: List<CountryItem>) {
        // Sort by score descending (countries with 0 points naturally appear at the bottom)
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
        _latestEvent.value = event

        // Update country score
        _countries.update { currentList ->
            val updated = currentList.map { country ->
                if (country.id == event.countryId) {
                    val newScore = country.score + event.points
                    persistScore(country.id, newScore)
                    country.copy(score = newScore)
                } else country
            }
            // Sort & re-rank
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
        updateChatter(event.username, event.countryId, event.countryFlag, event.level, event.points)

        // Trigger CoinBurst animation from the country flag
        spawnCoinBurst(
            countryId = event.countryId,
            countryFlag = event.countryFlag,
            username = event.username,
            points = event.points
        )

        // Show live comment banner for 2 seconds
        showCommentBanner(event)
    }

    private fun showCommentBanner(event: LiveEvent) {
        commentBannerJob?.cancel()
        val banner = CommentBannerData(
            id = ++bannerIdCounter,
            username = event.username,
            message = event.message.ifBlank { "Boosted ${event.countryName}! +${event.points}" },
            countryFlag = event.countryFlag,
            countryName = event.countryName,
            points = event.points,
            level = event.level,
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

    private fun spawnCoinBurst(countryId: String, countryFlag: String, username: String, points: Long) {
        // Find index of country in 9 columns grid to calculate approximate screen coordinate
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
            coinCount = if (points >= 400) 12 else 6
        )

        _coinBursts.update { (it + burst).takeLast(10) }

        // Remove after animation time
        viewModelScope.launch {
            delay(1200)
            _coinBursts.update { current -> current.filter { it.id != burst.id } }
        }
    }

    fun triggerCommentAction() {
        val target = _countries.value.firstOrNull() ?: return
        viewModelScope.launch {
            simulator.triggerManualEvent(
                type = LiveEventType.COMMENT,
                countryId = target.id,
                username = "You",
                level = 5 // +5 points
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
                        // Still connect with video ID
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
