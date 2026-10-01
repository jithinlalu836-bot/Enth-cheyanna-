package com.example.model

data class UserProfile(
    val username: String = "TurboBooster",
    val title: String = "Master Supporter",
    val level: Int = 5,
    val xp: Int = 680,
    val xpForNextLevel: Int = 1000,
    val activeCountryId: String = "TR",
    val totalBoostsGiven: Long = 18450,
    val comboMultiplier: Int = 1,
    val comboTaps: Int = 0,
    val comboProgress: Float = 0f,
    val coins: Long = 3400
) {
    val xpProgress: Float
        get() = (xp.toFloat() / xpForNextLevel.toFloat()).coerceIn(0f, 1f)
}
