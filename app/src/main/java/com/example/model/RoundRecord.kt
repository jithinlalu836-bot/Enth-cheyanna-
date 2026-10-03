package com.example.model

data class RoundRecord(
    val roundNumber: Int,
    val winnerName: String,
    val winnerFlag: String,
    val winningScore: Long,
    val timestamp: Long = System.currentTimeMillis()
)
