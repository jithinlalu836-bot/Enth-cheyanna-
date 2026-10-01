package com.example.service

import com.example.model.LiveEvent
import com.example.model.LiveEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

interface LiveChatEventSource {
    val eventsFlow: SharedFlow<LiveEvent>
    fun start()
    fun stop()
    val isRunning: Boolean
}

class MockLiveChatSimulator(
    private val scope: CoroutineScope,
    private val countryProvider: () -> List<Pair<String, Pair<String, String>>> // id, name, flag
) : LiveChatEventSource {

    private val _eventsFlow = MutableSharedFlow<LiveEvent>(extraBufferCapacity = 64)
    override val eventsFlow: SharedFlow<LiveEvent> = _eventsFlow.asSharedFlow()

    private var simulationJob: Job? = null
    override var isRunning: Boolean = false
        private set

    var intervalMillis: Long = 1400L

    private val sampleUsers = listOf(
        Pair("Emre_Kral", "TR"),
        Pair("Aarav_Star", "IN"),
        Pair("Budi_Ganteng", "ID"),
        Pair("Alex_USA", "US"),
        Pair("Nguyen_Pro", "VN"),
        Pair("Zhang_Wei", "CN"),
        Pair("Dmitry_Rus", "RU"),
        Pair("Ali_Pak", "PK"),
        Pair("Rahman_BD", "BD"),
        Pair("Lucas_BR", "BR"),
        Pair("Kenji_Tokyo", "JP"),
        Pair("Max_Berlin", "DE"),
        Pair("Chloe_Paris", "FR"),
        Pair("Oliver_UK", "GB"),
        Pair("Maria_Manila", "PH"),
        Pair("Somchai_Thai", "TH")
    )

    private val cheers = listOf(
        "GO TO NUMBER ONE! 🔥",
        "Keep pushing! 🚀",
        "Maximum boost! ⚡",
        "We are champions! 🏆",
        "Never give up! 💪",
        "Supercharge! ✨",
        "Best country in the world! 🌟"
    )

    override fun start() {
        if (isRunning) return
        isRunning = true
        simulationJob = scope.launch(Dispatchers.Default) {
            while (isRunning) {
                delay(intervalMillis + Random.nextLong(-200, 300))
                triggerRandomEvent()
            }
        }
    }

    override fun stop() {
        isRunning = false
        simulationJob?.cancel()
        simulationJob = null
    }

    suspend fun triggerRandomEvent() {
        val countries = countryProvider()
        if (countries.isEmpty()) return

        val isLikeSub = Random.nextInt(100) < 18 // 18% chance of Like & Subscribe event (+400 points)

        val user = sampleUsers.random()
        val countryMatch = countries.find { it.first == user.second } ?: countries.random()

        val level = if (isLikeSub) 1 else Random.nextInt(1, 6)
        val points = if (isLikeSub) 400L else (level.toLong()) // Level 5 = +5

        val event = LiveEvent(
            type = if (isLikeSub) LiveEventType.LIKE_SUBSCRIBE else LiveEventType.COMMENT,
            username = user.first,
            countryId = countryMatch.first,
            countryName = countryMatch.second.first,
            countryFlag = countryMatch.second.second,
            level = level,
            points = points,
            message = if (isLikeSub) "Liked & Subscribed! (+400 pts)" else cheers.random()
        )
        _eventsFlow.emit(event)
    }

    suspend fun triggerManualEvent(
        type: LiveEventType,
        countryId: String,
        username: String,
        level: Int,
        message: String? = null
    ) {
        val countries = countryProvider()
        val countryMatch = countries.find { it.first == countryId } ?: countries.firstOrNull() ?: return
        val points = if (type == LiveEventType.LIKE_SUBSCRIBE) 400L else level.toLong()

        val eventMessage = message ?: if (type == LiveEventType.LIKE_SUBSCRIBE) {
            "Liked & Subscribed! (+400 pts)"
        } else {
            listOf(
                "Let's go ${countryMatch.second.first}! 🔥",
                "${countryMatch.second.first} to the top! 🚀",
                "Maximum boost for ${countryMatch.second.first}! ⚡",
                "Keep pushing ${countryMatch.second.second}! 💪"
            ).random()
        }

        val event = LiveEvent(
            type = type,
            username = username,
            countryId = countryMatch.first,
            countryName = countryMatch.second.first,
            countryFlag = countryMatch.second.second,
            level = level,
            points = points,
            message = eventMessage
        )
        _eventsFlow.emit(event)
    }
}

/**
 * YouTube Live Chat API Adapter stub
 * Ready to plug in real liveChatMessages:list polling
 */
class YouTubeLiveChatAdapter(
    var apiKey: String = "",
    var liveChatId: String = ""
) : LiveChatEventSource {

    private val _eventsFlow = MutableSharedFlow<LiveEvent>()
    override val eventsFlow: SharedFlow<LiveEvent> = _eventsFlow.asSharedFlow()
    override var isRunning: Boolean = false

    override fun start() {
        isRunning = true
        // Hook for YouTube Live streaming API integration
    }

    override fun stop() {
        isRunning = false
    }
}
