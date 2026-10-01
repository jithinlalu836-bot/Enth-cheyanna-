package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun Header(
    isSimulationRunning: Boolean,
    onToggleSimulation: () -> Unit,
    isYouTubeConnected: Boolean = false,
    onOpenYouTubeConnect: () -> Unit = {},
    roundTimeRemainingSeconds: Int? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "title_glow")
    val glowBlur by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_blur"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top status bar row: Live indicator & Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live status badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(LiveRed)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LIVE OVERLAY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = LiveRed,
                    letterSpacing = 1.sp
                )
            }

            // Right Actions: YouTube Live Link button & Simulator toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // YouTube Link Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isYouTubeConnected) Color(0xFF09291E) else Color(0xFF2E0D14),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        if (isYouTubeConnected) NeonGreen else LiveRed
                    ),
                    modifier = Modifier
                        .clickable(onClick = onOpenYouTubeConnect)
                        .testTag("youtube_link_header_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartDisplay,
                            contentDescription = "YouTube Live",
                            tint = if (isYouTubeConnected) NeonGreen else LiveRed,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isYouTubeConnected) "YT CONNECTED" else "LINK YOUTUBE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isYouTubeConnected) NeonGreen else Color.White
                        )
                    }
                }

                // Quick Simulate toggle pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, SurfaceCardBorder),
                    modifier = Modifier
                        .clickable(onClick = onToggleSimulation)
                        .testTag("simulate_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSimulationRunning) Icons.Default.PlayArrow else Icons.Default.Stop,
                            contentDescription = "Simulate",
                            tint = if (isSimulationRunning) NeonGreen else TextMuted,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSimulationRunning) "SIMULATING" else "SIMULATE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationRunning) NeonGreen else TextMuted
                        )
                    }
                }
            }
        }

        // Title: "✦ BOOST YOUR COUNTRY ✦" in bold white with a soft glow
        Text(
            text = "✦ BOOST YOUR COUNTRY ✦",
            style = TextStyle(
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                shadow = Shadow(
                    color = NeonCyan.copy(alpha = 0.85f),
                    blurRadius = glowBlur
                )
            ),
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}
