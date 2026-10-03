package com.example.util

import java.text.NumberFormat
import java.util.Locale

object Formatters {
    private val numberFormatter = NumberFormat.getNumberInstance(Locale.US)

    fun formatPoints(points: Long): String {
        return numberFormatter.format(points)
    }

    fun formatScore(score: Long): String {
        return numberFormatter.format(score)
    }

    fun formatCompactPoints(points: Long): String {
        return when {
            points >= 1_000_000 -> String.format(Locale.US, "%.2fM", points / 1_000_000.0)
            points >= 1_000 -> String.format(Locale.US, "%.1fK", points / 1_000.0)
            else -> points.toString()
        }
    }
}
