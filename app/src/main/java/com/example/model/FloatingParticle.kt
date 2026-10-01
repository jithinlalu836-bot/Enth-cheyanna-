package com.example.model

data class FloatingParticle(
    val id: Long,
    val text: String,
    val xPercent: Float, // 0.1f to 0.9f
    val startYPercent: Float, // around button
    val colorHex: Long = 0xFFFFD700
)
