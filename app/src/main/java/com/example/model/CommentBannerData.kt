package com.example.model

data class CommentBannerData(
    val id: Long,
    val username: String,
    val message: String,
    val countryFlag: String,
    val countryName: String,
    val points: Long,
    val level: Int,
    val isLikeSub: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
