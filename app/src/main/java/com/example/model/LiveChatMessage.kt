package com.example.model

data class LiveChatMessage(
    val id: String,
    val userName: String,
    val userLevel: Int,
    val countryFlag: String,
    val countryName: String,
    val message: String,
    val boostAmount: Long,
    val isUser: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val badge: String = "FAN"
)
