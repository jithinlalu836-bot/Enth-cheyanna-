package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.InitialData
import com.example.model.Country
import com.example.model.FloatingParticle
import com.example.model.LiveChatMessage
import com.example.model.UserProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class BoostViewModel : ViewModel() {

    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries: StateFlow<List<Country>> = _countries.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _liveMessages = MutableStateFlow<List<LiveChatMessage>>(emptyList())
    val liveMessages: StateFlow<List<LiveChatMessage>> = _liveMessages.asStateFlow()

    private val _particles = MutableStateFlow<List<FloatingParticle>>(emptyList())
    val particles: StateFlow<List<FloatingParticle>> = _particles.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedContinent = MutableStateFlow("All")
    val selectedContinent: StateFlow<String> = _selectedContinent.asStateFlow()

    private val _isGridView = MutableStateFlow(true)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _isLiveSimulationEnabled = MutableStateFlow(true)
    val isLiveSimulationEnabled: StateFlow<Boolean> = _isLiveSimulationEnabled.asStateFlow()

    private var comboDecayJob: Job? = null
    private var simulationJob: Job? = null
    private var particleCounter = 0L

    init {
        // Initialize and rank countries
        val initialSorted = InitialData.initialCountries.sortedByDescending { it.score }
            .mapIndexed { index, country ->
                country.copy(
                    previousRank = index + 1,
                    currentRank = index + 1
                )
            }
        _countries.value = initialSorted
        _liveMessages.value = InitialData.sampleLiveMessages

        startLiveSimulation()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onContinentSelected(continent: String) {
        _selectedContinent.value = continent
    }

    fun toggleViewMode() {
        _isGridView.update { !it }
    }

    fun toggleLiveSimulation() {
        _isLiveSimulationEnabled.update { enabled ->
            val newState = !enabled
            if (newState) {
                startLiveSimulation()
            } else {
                simulationJob?.cancel()
            }
            newState
        }
    }

    fun setActiveCountry(countryId: String) {
        _userProfile.update { it.copy(activeCountryId = countryId) }
    }

    fun boostActiveCountry(basePoints: Long = 100L) {
        val activeId = _userProfile.value.activeCountryId
        boostCountry(countryId = activeId, basePoints = basePoints, isUserAction = true)
    }

    fun likeAndSubscribeAction() {
        val activeId = _userProfile.value.activeCountryId
        val basePoints = 400L

        // Trigger particles
        spawnParticle(text = "+400 LIKE & SUB! ❤️", colorHex = 0xFFFF007A)
        spawnParticle(text = "🔥 BONUS x${_userProfile.value.comboMultiplier}", colorHex = 0xFFFFD700)

        boostCountry(
            countryId = activeId,
            basePoints = basePoints,
            isUserAction = true,
            customMessage = "Liked & Subscribed! Massive love to my nation! ❤️🔔 (+${basePoints * _userProfile.value.comboMultiplier} pts)",
            xpBonus = 120
        )
    }

    fun commentAction(commentText: String) {
        val activeId = _userProfile.value.activeCountryId
        val activeCountry = _countries.value.find { it.id == activeId }
        val flag = activeCountry?.flag ?: "🌐"
        val countryName = activeCountry?.name ?: "Global"
        val boost = 350L * _userProfile.value.comboMultiplier

        spawnParticle(text = "+1 LEVEL BOOST! 🚀", colorHex = 0xFF00E5FF)
        spawnParticle(text = "+$boost PTS 💬", colorHex = 0xFF10B981)

        val message = LiveChatMessage(
            id = "user-${System.currentTimeMillis()}",
            userName = _userProfile.value.username,
            userLevel = _userProfile.value.level,
            countryFlag = flag,
            countryName = countryName,
            message = commentText,
            boostAmount = boost,
            isUser = true,
            badge = "VIP"
        )

        _liveMessages.update { listOf(message) + it.take(40) }

        boostCountry(
            countryId = activeId,
            basePoints = 350L,
            isUserAction = true,
            xpBonus = 180
        )
    }

    fun boostCountry(
        countryId: String,
        basePoints: Long,
        isUserAction: Boolean,
        customMessage: String? = null,
        xpBonus: Int = 20
    ) {
        val multiplier = if (isUserAction) _userProfile.value.comboMultiplier else 1
        val pointsToAdd = basePoints * multiplier

        if (isUserAction) {
            handleUserComboAndXp(pointsToAdd, xpBonus)
            spawnParticle(
                text = "+$pointsToAdd ⚡",
                colorHex = if (multiplier > 1) 0xFFFFD700 else 0xFF00E5FF
            )
        }

        _countries.update { currentList ->
            val updated = currentList.map { country ->
                if (country.id == countryId) {
                    country.copy(
                        score = country.score + pointsToAdd,
                        boostCount = country.boostCount + 1,
                        topChatterChats = if (isUserAction && countryId == _userProfile.value.activeCountryId) {
                            country.topChatterChats + 1
                        } else country.topChatterChats
                    )
                } else country
            }

            // Re-rank based on score descending
            val sorted = updated.sortedByDescending { it.score }
            sorted.mapIndexed { index, country ->
                val newRank = index + 1
                country.copy(
                    previousRank = country.currentRank,
                    currentRank = newRank
                )
            }
        }

        if (customMessage != null && isUserAction) {
            val targetCountry = _countries.value.find { it.id == countryId }
            val flag = targetCountry?.flag ?: "🌐"
            val cName = targetCountry?.name ?: "Global"
            val msg = LiveChatMessage(
                id = "msg-${System.currentTimeMillis()}",
                userName = _userProfile.value.username,
                userLevel = _userProfile.value.level,
                countryFlag = flag,
                countryName = cName,
                message = customMessage,
                boostAmount = pointsToAdd,
                isUser = true,
                badge = "VIP"
            )
            _liveMessages.update { listOf(msg) + it.take(40) }
        }
    }

    private fun handleUserComboAndXp(pointsGiven: Long, xpEarned: Int) {
        _userProfile.update { current ->
            val newTotalBoosts = current.totalBoostsGiven + pointsGiven
            val newCoins = current.coins + (pointsGiven / 10).coerceAtLeast(1)

            // Combo logic: Tapping within window increases combo
            val newTaps = current.comboTaps + 1
            val newMultiplier = when {
                newTaps >= 30 -> 10
                newTaps >= 18 -> 5
                newTaps >= 8 -> 3
                newTaps >= 3 -> 2
                else -> 1
            }

            // XP and Level Up logic
            var newXp = current.xp + xpEarned
            var newLevel = current.level
            var neededXp = current.xpForNextLevel
            var title = current.title

            while (newXp >= neededXp) {
                newXp -= neededXp
                newLevel += 1
                neededXp = (neededXp * 1.35f).toInt()
                title = when {
                    newLevel >= 15 -> "Cosmic Grandmaster 👑"
                    newLevel >= 10 -> "National Hero 🎖️"
                    newLevel >= 8 -> "Supreme Booster ⚡"
                    newLevel >= 6 -> "Elite Commander 🚀"
                    else -> "Master Supporter 🌟"
                }
                spawnParticle("🎉 LEVEL UP! LVL $newLevel", colorHex = 0xFFFFD700)
            }

            current.copy(
                totalBoostsGiven = newTotalBoosts,
                coins = newCoins,
                comboTaps = newTaps,
                comboMultiplier = newMultiplier,
                comboProgress = 1f,
                xp = newXp,
                level = newLevel,
                xpForNextLevel = neededXp,
                title = title
            )
        }

        // Reset combo decay timer
        comboDecayJob?.cancel()
        comboDecayJob = viewModelScope.launch {
            // Decay over 4 seconds
            val steps = 20
            val delayPerStep = 200L
            for (i in steps downTo 0) {
                delay(delayPerStep)
                _userProfile.update { it.copy(comboProgress = i.toFloat() / steps.toFloat()) }
            }
            // Reset combo
            _userProfile.update {
                it.copy(
                    comboTaps = 0,
                    comboMultiplier = 1,
                    comboProgress = 0f
                )
            }
        }
    }

    private fun spawnParticle(text: String, colorHex: Long) {
        val particle = FloatingParticle(
            id = ++particleCounter,
            text = text,
            xPercent = Random.nextFloat() * 0.6f + 0.2f,
            startYPercent = Random.nextFloat() * 0.2f + 0.5f,
            colorHex = colorHex
        )
        _particles.update { current ->
            (current + particle).takeLast(12)
        }

        // Remove after animation time
        viewModelScope.launch {
            delay(1600)
            _particles.update { current -> current.filter { it.id != particle.id } }
        }
    }

    private fun startLiveSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            val randomChatters = listOf(
                Triple("Fatima_TR", "🇹🇷", "TURKEY NUMBER ONE! Hep Beraber! 🇹🇷"),
                Triple("Rohan_IND", "🇮🇳", "Boost for India!! 🚀🇮🇳"),
                Triple("Sam_USA", "🇺🇸", "USA surge!! Keep tapping! 🇺🇸🔥"),
                Triple("Agus_IDN", "🇮🇩", "Semangat Indonesia! Juara! 🇮🇩"),
                Triple("Gabriel_BRA", "🇧🇷", "Brasil no topo sempre! 🇧🇷⚽"),
                Triple("Yuki_JPN", "🇯🇵", "Ganbare Nippon! 🇯🇵⚡"),
                Triple("Hans_GER", "🇩🇪", "Auf gehts Deutschland! 🇩🇪💪"),
                Triple("George_UK", "🇬🇧", "Britain holding strong! 🇬🇧"),
                Triple("Leo_ARG", "🇦🇷", "Argentina campeon! 🇦🇷🏆"),
                Triple("Jin_KOR", "🇰🇷", "K-Power boost! Let's go! 🇰🇷✨")
            )

            while (true) {
                delay(Random.nextLong(2200, 4200))
                if (!_isLiveSimulationEnabled.value) continue

                val chatter = randomChatters.random()
                val targetCountry = _countries.value.find { it.flag == chatter.second }
                    ?: _countries.value.random()
                val randomBoost = listOf(250L, 500L, 750L, 1200L, 2000L).random()

                val newMsg = LiveChatMessage(
                    id = "sim-${System.currentTimeMillis()}",
                    userName = chatter.first,
                    userLevel = Random.nextInt(3, 10),
                    countryFlag = targetCountry.flag,
                    countryName = targetCountry.name,
                    message = chatter.third,
                    boostAmount = randomBoost,
                    isUser = false,
                    badge = if (randomBoost >= 1200) "MVP" else "FAN"
                )

                _liveMessages.update { listOf(newMsg) + it.take(30) }

                // Boost that country's score
                _countries.update { currentList ->
                    val updated = currentList.map { country ->
                        if (country.id == targetCountry.id) {
                            country.copy(
                                score = country.score + randomBoost,
                                boostCount = country.boostCount + 1
                            )
                        } else country
                    }
                    val sorted = updated.sortedByDescending { it.score }
                    sorted.mapIndexed { index, country ->
                        country.copy(
                            previousRank = country.currentRank,
                            currentRank = index + 1
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        comboDecayJob?.cancel()
        simulationJob?.cancel()
    }
}
