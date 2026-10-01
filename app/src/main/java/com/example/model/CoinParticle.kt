package com.example.model

data class CoinBurstData(
    val id: Long,
    val countryId: String,
    val countryFlag: String,
    val text: String,
    val username: String,
    val xPercent: Float, // relative 0f..1f
    val yPercent: Float,
    val coinCount: Int = 6,
    val colorHex: Long = 0xFFFFD700
)
