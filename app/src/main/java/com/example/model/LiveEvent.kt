package com.example.model

enum class LiveEventType {
    COMMENT,
    LIKE_SUBSCRIBE,
    SUPER_CHAT,
    GIFT
}

data class LiveEvent(
    val id: String = "${System.currentTimeMillis()}-${(0..9999).random()}",
    val type: LiveEventType,
    val username: String,
    val userId: String = "",
    val userAvatarUrl: String = "",
    val countryId: String,
    val countryName: String,
    val countryFlag: String,
    val level: Int,
    val points: Long,
    val message: String = "",
    val dollarAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
