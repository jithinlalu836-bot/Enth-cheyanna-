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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Country
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.BronzePodium
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.GoldPodium
import com.example.ui.theme.SilverMedal
import com.example.ui.theme.SilverPodium
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun PodiumSection(
    topCountries: List<Country>,
    onCountryBoostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    if (topCountries.size < 3) return

    val first = topCountries.getOrNull(0) ?: return
    val second = topCountries.getOrNull(1) ?: return
    val third = topCountries.getOrNull(2) ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "gold_glow")
    val crownBounce by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crown_bounce"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isCompact) 10.dp else 16.dp, vertical = if (isCompact) 2.dp else 6.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = GoldCrown,
                    modifier = Modifier.size(if (isCompact) 16.dp else 20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "TOP PODIUM CHAMPIONS",
                    fontSize = if (isCompact) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }
            Text(
                text = "Tap to boost",
                fontSize = if (isCompact) 9.sp else 11.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 8.dp))

        // Podium Pillars Container
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 6.dp else 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Rank 2: Silver (Left)
            PodiumPillarItem(
                rank = 2,
                country = second,
                pillarHeight = if (isCompact) 36.dp else 105.dp,
                accentColor = SilverMedal,
                badgeGradient = Brush.verticalGradient(listOf(Color(0xFFE0E0E0), Color(0xFF9E9E9E))),
                podiumColor = Color(0xFF1E2638),
                borderColor = SilverMedal.copy(alpha = 0.5f),
                onBoostClick = { onCountryBoostClick(second.id) },
                testTag = "podium_rank_2",
                isCompact = isCompact,
                modifier = Modifier.weight(1f)
            )

            // Rank 1: Gold (Center - Tallest)
            PodiumPillarItem(
                rank = 1,
                country = first,
                pillarHeight = if (isCompact) 48.dp else 135.dp,
                accentColor = GoldCrown,
                badgeGradient = Brush.verticalGradient(listOf(GoldGlow, GoldCrown)),
                podiumColor = Color(0xFF282312),
                borderColor = GoldCrown,
                offsetY = crownBounce.dp,
                isCenterRank = true,
                isCompact = isCompact,
                onBoostClick = { onCountryBoostClick(first.id) },
                testTag = "podium_rank_1",
                modifier = Modifier.weight(if (isCompact) 1.1f else 1.15f)
            )

            // Rank 3: Bronze (Right)
            PodiumPillarItem(
                rank = 3,
                country = third,
                pillarHeight = if (isCompact) 28.dp else 85.dp,
                accentColor = BronzeMedal,
                badgeGradient = Brush.verticalGradient(listOf(Color(0xFFE29B58), BronzeMedal)),
                podiumColor = Color(0xFF221A15),
                borderColor = BronzeMedal.copy(alpha = 0.5f),
                onBoostClick = { onCountryBoostClick(third.id) },
                testTag = "podium_rank_3",
                isCompact = isCompact,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PodiumPillarItem(
    rank: Int,
    country: Country,
    pillarHeight: Dp,
    accentColor: Color,
    badgeGradient: Brush,
    podiumColor: Color,
    borderColor: Color,
    onBoostClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    offsetY: Dp = 0.dp,
    isCenterRank: Boolean = false,
    isCompact: Boolean = false
) {
    val avatarSize = if (isCompact) {
        if (isCenterRank) 38.dp else 30.dp
    } else {
        if (isCenterRank) 56.dp else 46.dp
    }

    val flagFontSize = if (isCompact) {
        if (isCenterRank) 20.sp else 16.sp
    } else {
        if (isCenterRank) 28.sp else 22.sp
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            .clickable(onClick = onBoostClick)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar / Flag Stack with Crown/Badge
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.offset(y = offsetY)
        ) {
            // Crown or Medal indicator
            Text(
                text = when (rank) {
                    1 -> "👑"
                    2 -> "🥈"
                    else -> "🥉"
                },
                fontSize = if (isCompact) (if (isCenterRank) 16.sp else 13.sp) else (if (isCenterRank) 24.sp else 18.sp),
                modifier = Modifier.offset(y = if (isCompact) (-6).dp else (-10).dp)
            )

            // Flag Avatar Circle
            Box(
                modifier = Modifier
                    .padding(top = if (isCompact) 6.dp else 8.dp)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(SurfaceCard)
                    .border(if (isCompact) 1.2.dp else 1.8.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = country.flag,
                    fontSize = flagFontSize
                )
            }

            // Rank Pill overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = if (isCompact) 4.dp else 6.dp)
                    .clip(CircleShape)
                    .background(badgeGradient)
                    .padding(horizontal = if (isCompact) 5.dp else 7.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "#$rank",
                    fontSize = if (isCompact) 8.sp else 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isCompact) 6.dp else 8.dp))

        // Country Name & Score Header
        Text(
            text = country.name,
            fontWeight = FontWeight.Bold,
            fontSize = if (isCompact) (if (isCenterRank) 10.sp else 9.sp) else (if (isCenterRank) 13.sp else 12.sp),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Text(
            text = Formatters.formatCompactPoints(country.score),
            fontWeight = FontWeight.Black,
            fontSize = if (isCompact) (if (isCenterRank) 11.sp else 10.sp) else (if (isCenterRank) 13.sp else 12.sp),
            color = accentColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isCompact) 3.dp else 5.dp))

        // Podium Block Pillar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight),
            shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp),
            color = podiumColor,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = if (isCompact) 3.dp else 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isCompact) {
                    Text(
                        text = "TOP CHATTER",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = country.topChatter,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${country.topChatterChats} chats",
                            fontSize = 9.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Quick Boost Button inside pillar
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.2f))
                        .padding(horizontal = if (isCompact) 4.dp else 6.dp, vertical = if (isCompact) 1.dp else 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Boost",
                            tint = accentColor,
                            modifier = Modifier.size(if (isCompact) 9.dp else 11.dp)
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                        Text(
                            text = "+50",
                            fontSize = if (isCompact) 8.sp else 9.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}
