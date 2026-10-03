package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CommentBannerData
import com.example.model.LiveEventType
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun CommentBanner(
    banner: CommentBannerData?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 220)
        ) + fadeIn(animationSpec = tween(180)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 260)
        ) + fadeOut(animationSpec = tween(200)),
        modifier = modifier
    ) {
        if (banner != null) {
            CommentBannerCard(banner = banner)
        }
    }
}

@Composable
private fun CommentBannerCard(
    banner: CommentBannerData
) {
    val isSuperChat = banner.type == LiveEventType.SUPER_CHAT
    val isGift = banner.type == LiveEventType.GIFT

    val borderColor = when {
        isSuperChat -> GoldCrown
        isGift -> NeonMagenta
        banner.isLikeSub -> GoldCrown
        banner.points >= 5 -> NeonGreen
        else -> NeonCyan
    }

    val cardBackground = when {
        isSuperChat -> Brush.horizontalGradient(
            listOf(Color(0xF02B1D05), Color(0xF0150F04), Color(0xF0221606))
        )
        isGift -> Brush.horizontalGradient(
            listOf(Color(0xF0280820), Color(0xF0160718), Color(0xF023061C))
        )
        else -> Brush.horizontalGradient(
            listOf(Color(0xF20B101E), Color(0xF20F162A), Color(0xF20B101E))
        )
    }

    var progressTarget by remember(banner.id) { mutableFloatStateOf(1f) }
    LaunchedEffect(banner.id) {
        progressTarget = 0f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
        label = "banner_progress"
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(12.dp), spotColor = borderColor)
            .testTag("comment_alert_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar / Event Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isSuperChat) listOf(GoldCrown, FlameOrange)
                                else if (isGift) listOf(NeonMagenta, Color(0xFF9333EA))
                                else listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        )
                        .border(1.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            isSuperChat -> "⚡"
                            isGift -> "🎁"
                            banner.isLikeSub -> "🔔"
                            else -> banner.username.take(2).uppercase()
                        },
                        fontSize = if (isSuperChat || isGift || banner.isLikeSub) 16.sp else 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Comment Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Header line: Show ID on top prominently for Super Chat & Gift, or Username
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSuperChat) {
                            Text(
                                text = "⚡ SUPER CHAT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldCrown,
                                modifier = Modifier
                                    .background(GoldCrown.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        } else if (isGift) {
                            Text(
                                text = "🎁 GIFT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonMagenta,
                                modifier = Modifier
                                    .background(NeonMagenta.copy(alpha = 0.2f), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        // ID / Username
                        val displayId = if (banner.userId.isNotBlank()) "ID: ${banner.userId}" else "@${banner.username}"
                        Text(
                            text = displayId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${banner.countryFlag} ${banner.countryName}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = borderColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(1.dp))

                    // Message text
                    val messagePrefix = when {
                        isSuperChat -> "$${banner.dollarAmount.toInt()} Super Chat: "
                        isGift -> "Sent a Gift: "
                        else -> "💬 "
                    }
                    Text(
                        text = "$messagePrefix\"${banner.message}\"",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        color = if (isSuperChat) Color(0xFFFFECC0) else if (isGift) Color(0xFFFFD1F1) else Color(0xFFE2E8F0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Points Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(borderColor.copy(alpha = 0.25f))
                        .border(1.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+${banner.points} pts",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Black,
                        color = borderColor
                    )
                }
            }

            // 2-second animated countdown timer bar at bottom edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(borderColor, borderColor.copy(alpha = 0.4f))
                            )
                        )
                )
            }
        }
    }
}
