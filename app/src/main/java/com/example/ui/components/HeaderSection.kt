package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ScreenSearchDesktop
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeaderSection(
    onCommentClick: () -> Unit,
    onLikeSubscribeClick: () -> Unit,
    onTurboClick: () -> Unit,
    isGridView: Boolean,
    onToggleView: () -> Unit,
    isSimulationActive: Boolean,
    onToggleSimulation: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    screenModeLabel: String = "Fit Screen",
    onToggleScreenMode: () -> Unit = onToggleView
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val livePulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) 10.dp else 16.dp, vertical = if (isCompact) 4.dp else 8.dp)
    ) {
        // Top App Bar Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Boost Your Country",
                        style = if (isCompact) MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.3).sp
                        ) else MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.5).sp
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "⚡", fontSize = if (isCompact) 16.sp else 22.sp)
                }

                // Live status badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 6.dp else 8.dp)
                            .clip(CircleShape)
                            .background(LiveRed.copy(alpha = livePulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE BATTLE",
                        color = LiveRed,
                        fontSize = if (isCompact) 9.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• 42.8K Boosting",
                        color = TextSecondary,
                        fontSize = if (isCompact) 9.sp else 11.sp
                    )
                }
            }

            // Top action toggles
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Live simulation toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                    modifier = Modifier
                        .clickable(onClick = onToggleSimulation)
                        .testTag("toggle_simulation_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSimulationActive) Icons.Default.PlayArrow else Icons.Default.Stop,
                            contentDescription = "Toggle Live Simulation",
                            tint = if (isSimulationActive) NeonGreen else TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSimulationActive) "LIVE" else "PAUSE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulationActive) NeonGreen else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Single-screen / Full view toggle
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariantDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .clickable(onClick = onToggleScreenMode)
                        .testTag("view_mode_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCompact) Icons.Default.FitScreen else Icons.Default.ViewList,
                            contentDescription = "Switch View Mode",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = screenModeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 12.dp))

        // Interactive Action Cards Section
        if (isCompact) {
            // High-density action buttons that fit horizontally in ~38dp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Action 1: Comment (+1 Level 5)
                CompactActionCard(
                    title = "Comment",
                    tag = "+1 Level 5",
                    tagColor = NeonCyan,
                    borderColor = NeonCyan.copy(alpha = 0.6f),
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF0F2B48), Color(0xFF163C66))),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Comment",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    onClick = onCommentClick,
                    testTag = "action_card_comment",
                    modifier = Modifier.weight(1f)
                )

                // Action 2: Like & Subscribe (+400 Points)
                CompactActionCard(
                    title = "Like & Sub",
                    tag = "+400 Pts",
                    tagColor = GoldCrown,
                    borderColor = NeonMagenta.copy(alpha = 0.6f),
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF381230), Color(0xFF5A1646))),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = NeonMagenta,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    onClick = onLikeSubscribeClick,
                    testTag = "action_card_like_subscribe",
                    modifier = Modifier.weight(1.05f)
                )

                // Action 3: Turbo Tap (+100 Points)
                CompactActionCard(
                    title = "Turbo Tap",
                    tag = "+100 Pts",
                    tagColor = FlameOrange,
                    borderColor = FlameOrange.copy(alpha = 0.6f),
                    gradient = Brush.horizontalGradient(listOf(Color(0xFF38230F), Color(0xFF5B3512))),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Turbo",
                            tint = FlameOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    onClick = onTurboClick,
                    testTag = "action_card_turbo",
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // Full expanded action cards
            Text(
                text = "BATTLE BOOST ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Action Card 1: Comment (+1 Level 5)
                ActionCard(
                    title = "Comment",
                    subtitle = "+350 Pts",
                    tagText = "+1 Level 5",
                    tagColor = NeonCyan,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF0F2B48), Color(0xFF163C66))
                    ),
                    borderColor = NeonCyan.copy(alpha = 0.6f),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Comment",
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = onCommentClick,
                    testTag = "action_card_comment",
                    modifier = Modifier.weight(1f)
                )

                // Action Card 2: Like & Subscribe (+400 Points)
                ActionCard(
                    title = "Like & Sub",
                    subtitle = "Boost Nation",
                    tagText = "+400 Points",
                    tagColor = GoldCrown,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF381230), Color(0xFF5A1646))
                    ),
                    borderColor = NeonMagenta.copy(alpha = 0.6f),
                    icon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = NeonMagenta,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Subscribe",
                                tint = GoldCrown,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    onClick = onLikeSubscribeClick,
                    testTag = "action_card_like_subscribe",
                    modifier = Modifier.weight(1f)
                )

                // Action Card 3: Turbo Tap (+100 Points Rapid)
                ActionCard(
                    title = "Turbo Tap",
                    subtitle = "Rapid Fire",
                    tagText = "+100 Pts",
                    tagColor = FlameOrange,
                    gradient = Brush.horizontalGradient(
                        listOf(Color(0xFF38230F), Color(0xFF5B3512))
                    ),
                    borderColor = FlameOrange.copy(alpha = 0.6f),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Turbo",
                            tint = FlameOrange,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    onClick = onTurboClick,
                    testTag = "action_card_turbo",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactActionCard(
    title: String,
    tag: String,
    tagColor: Color,
    borderColor: Color,
    gradient: Brush,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .background(gradient)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                icon()
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = tag,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = tagColor
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    tagText: String,
    tagColor: Color,
    gradient: Brush,
    borderColor: Color,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.2.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(10.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(tagColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tagText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = tagColor
                        )
                    }
                    icon()
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
