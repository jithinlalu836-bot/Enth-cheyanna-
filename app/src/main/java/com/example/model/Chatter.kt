package com.example.model

data class Chatter(
    val username: String,
    val initials: String,
    val countryId: String,
    val countryFlag: String,
    val level: Int = 1,
    val chatCount: Int = 0,
    val pointsEarned: Long = 0L,
    val avatarColorHex: Long = 0xFF8B5CF6
)
