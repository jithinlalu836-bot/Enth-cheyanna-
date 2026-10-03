package com.example.model

data class CommentBannerData(
    val id: Long,
    val username: String,
    val userId: String = "",
    val userAvatarUrl: String = "",
    val message: String,
    val countryFlag: String,
    val countryName: String,
    val points: Long,
    val level: Int,
    val type: LiveEventType = LiveEventType.COMMENT,
    val dollarAmount: Double = 0.0,
    val isLikeSub: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
