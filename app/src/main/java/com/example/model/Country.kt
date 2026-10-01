package com.example.model

data class Country(
    val id: String,
    val name: String,
    val flag: String,
    val score: Long,
    val previousRank: Int = 1,
    val currentRank: Int = 1,
    val continent: String = "Global",
    val topChatter: String = "SuperBooster",
    val topChatterChats: Int = 120,
    val boostCount: Long = 1500,
    val colorHex: Long = 0xFF00E5FF
) {
    val rankDelta: Int
        get() = previousRank - currentRank // positive means climbed up, negative means fell down
}
