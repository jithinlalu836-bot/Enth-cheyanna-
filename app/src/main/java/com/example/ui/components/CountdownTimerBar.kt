package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryItem
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CountdownTimerBar(
    remainingSeconds: Int,
    totalDurationSeconds: Int,
    isTimerRunning: Boolean,
    isRoundFinished: Boolean,
    winningCountry: CountryItem?,
    onTogglePause: () -> Unit,
    onAddExtraTime: () -> Unit,
    onRestartRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinalCountdown = remainingSeconds in 1..20
    val isWarning = remainingSeconds in 21..60

    // Pulse animation for warning & final seconds
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isFinalCountdown) 0.3f else 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isFinalCountdown) 400 else 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timer_pulse_alpha"
    )

    val progressFraction = if (totalDurationSeconds > 0) {
        (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val digitsColor = when {
        isRoundFinished -> GoldCrown
        isFinalCountdown -> LiveRed.copy(alpha = pulseAlpha)
        isWarning -> FlameOrange
        else -> NeonCyan
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formattedTime = "%02d:%02d".format(minutes, seconds)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .testTag("countdown_timer_bar"),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF0D1222),
        border = androidx.compose.foundation.BorderStroke(
            if (isFinalCountdown || isRoundFinished) 1.5.dp else 1.dp,
            if (isRoundFinished) GoldCrown else if (isFinalCountdown) LiveRed else SurfaceCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (isRoundFinished) {
                // Victory Banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ROUND OVER! WINNER: ${winningCountry?.flag ?: "🏆"} ${winningCountry?.name ?: "Champion"}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldCrown
                        )
                    }

                    // Restart button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldCrown,
                        modifier = Modifier
                            .clickable(onClick = onRestartRound)
                            .testTag("restart_round_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Restart",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "NEW ROUND",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }
            } else {
                // Standard countdown clock row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Timer label and live icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isTimerRunning) NeonGreen else FlameOrange)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = if (isFinalCountdown) LiveRed else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isFinalCountdown) "FINAL SPRINT!" else "ROUND COUNTDOWN",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFinalCountdown) LiveRed else TextSecondary,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Center: Prominent Glowing Countdown Digits
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onTogglePause)
                    ) {
                        Text(
                            text = formattedTime,
                            style = TextStyle(
                                color = digitsColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                shadow = Shadow(
                                    color = digitsColor.copy(alpha = 0.8f),
                                    blurRadius = if (isFinalCountdown) 14f else 8f
                                )
                            )
                        )
                    }

                    // Right: Quick Controls (+1 min & Pause/Play)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // +1 Minute Pill
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = SurfaceVariantDark,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                            modifier = Modifier
                                .clickable(onClick = onAddExtraTime)
                                .testTag("add_minute_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "+1m",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "1M",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        }

                        // Play/Pause Pill
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = SurfaceVariantDark,
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                            modifier = Modifier
                                .clickable(onClick = onTogglePause)
                                .testTag("timer_play_pause_button")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = if (isTimerRunning) FlameOrange else NeonGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Progress Indicator Bar
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .clip(RoundedCornerShape(1.dp)),
                    color = when {
                        isFinalCountdown -> LiveRed
                        isWarning -> FlameOrange
                        else -> NeonCyan
                    },
                    trackColor = SurfaceVariantDark
                )
            }
        }
    }
}
