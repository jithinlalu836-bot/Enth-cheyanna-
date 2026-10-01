package com.example.model

data class CountryItem(
    val id: String,
    val name: String,
    val flag: String,
    val score: Long = 0L,
    val rank: Int = 1,
    val previousRank: Int = 1
) {
    val rankDelta: Int
        get() = previousRank - rank
}
