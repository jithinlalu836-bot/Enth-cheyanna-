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

    private val superChatAmounts = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0)

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

        val roll = Random.nextInt(100)
        val user = sampleUsers.random()
        val countryMatch = countries.find { it.first == user.second } ?: countries.random()
        val userId = "UC${kotlin.math.abs(user.first.hashCode()).toString().take(8)}"

        when {
            // 1. Super Chat (10% chance): 1000 points per dollar
            roll < 10 -> {
                val dollar = superChatAmounts.random()
                val points = (dollar * 1000).toLong()
                val event = LiveEvent(
                    type = LiveEventType.SUPER_CHAT,
                    username = user.first,
                    userId = userId,
                    countryId = countryMatch.first,
                    countryName = countryMatch.second.first,
                    countryFlag = countryMatch.second.second,
                    level = 5,
                    points = points,
                    dollarAmount = dollar,
                    message = "Massive $${dollar.toInt()} Super Chat for ${countryMatch.second.first}! 🔥"
                )
                _eventsFlow.emit(event)
            }
            // 2. Gift (12% chance): 50 points per gift
            roll < 22 -> {
                val event = LiveEvent(
                    type = LiveEventType.GIFT,
                    username = user.first,
                    userId = userId,
                    countryId = countryMatch.first,
                    countryName = countryMatch.second.first,
                    countryFlag = countryMatch.second.second,
                    level = 3,
                    points = 50L,
                    message = "Sent a Community Gift for ${countryMatch.second.first}! 🎁"
                )
                _eventsFlow.emit(event)
            }
            // 3. Like & Subscribe (15% chance): 400 points
            roll < 37 -> {
                val event = LiveEvent(
                    type = LiveEventType.LIKE_SUBSCRIBE,
                    username = user.first,
                    userId = userId,
                    countryId = countryMatch.first,
                    countryName = countryMatch.second.first,
                    countryFlag = countryMatch.second.second,
                    level = 1,
                    points = 400L,
                    message = "Liked & Subscribed! (+400 pts)"
                )
                _eventsFlow.emit(event)
            }
            // 4. Regular comment (63% chance): default +1 point (or level 1..5)
            else -> {
                val level = Random.nextInt(1, 6)
                val points = level.toLong()
                val event = LiveEvent(
                    type = LiveEventType.COMMENT,
                    username = user.first,
                    userId = userId,
                    countryId = countryMatch.first,
                    countryName = countryMatch.second.first,
                    countryFlag = countryMatch.second.second,
                    level = level,
                    points = points,
                    message = cheers.random()
                )
                _eventsFlow.emit(event)
            }
        }
    }

    suspend fun triggerManualSuperChat(
        countryId: String,
        username: String,
        dollarAmount: Double,
        message: String? = null
    ) {
        val countries = countryProvider()
        val countryMatch = countries.find { it.first == countryId } ?: countries.firstOrNull() ?: return
        val points = (dollarAmount * 1000).toLong()
        val userId = "UC${kotlin.math.abs(username.hashCode()).toString().take(8)}"

        val event = LiveEvent(
            type = LiveEventType.SUPER_CHAT,
            username = username,
            userId = userId,
            countryId = countryMatch.first,
            countryName = countryMatch.second.first,
            countryFlag = countryMatch.second.second,
            level = 5,
            points = points,
            dollarAmount = dollarAmount,
            message = message ?: "Super Chat $${dollarAmount.toInt()}! 🚀"
        )
        _eventsFlow.emit(event)
    }

    suspend fun triggerManualGift(
        countryId: String,
        username: String,
        message: String? = null
    ) {
        val countries = countryProvider()
        val countryMatch = countries.find { it.first == countryId } ?: countries.firstOrNull() ?: return
        val userId = "UC${kotlin.math.abs(username.hashCode()).toString().take(8)}"

        val event = LiveEvent(
            type = LiveEventType.GIFT,
            username = username,
            userId = userId,
            countryId = countryMatch.first,
            countryName = countryMatch.second.first,
            countryFlag = countryMatch.second.second,
            level = 3,
            points = 50L,
            message = message ?: "Sent a Gift! 🎁"
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
        val userId = "UC${kotlin.math.abs(username.hashCode()).toString().take(8)}"

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
            userId = userId,
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
