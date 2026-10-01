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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryItem
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SilverMedal
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun FlagGrid(
    countries: List<CountryItem>,
    onCountryClick: (CountryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = 9
    val top1 = countries.getOrNull(0)
    val top2 = countries.getOrNull(1)
    val top3 = countries.getOrNull(2)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("country_leaderboard_grid")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. TOP 3 BIG FLAGS PODIUM SHOWCASE
            if (top1 != null && top2 != null && top3 != null) {
                TopThreeBigFlagsPodium(
                    top1 = top1,
                    top2 = top2,
                    top3 = top3,
                    onCountryClick = onCountryClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. 9-COLUMN ALL COUNTRIES GRID
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.5.dp)
            ) {
                val chunkedCountries = countries.chunked(columns)
                chunkedCountries.forEach { rowCountries ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
                    ) {
                        rowCountries.forEach { country ->
                            FlagTile(
                                country = country,
                                onClick = { onCountryClick(country) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                            )
                        }

                        // Fill remaining empty columns in last row
                        if (rowCountries.size < columns) {
                            repeat(columns - rowCountries.size) {
                                Spacer(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopThreeBigFlagsPodium(
    top1: CountryItem,
    top2: CountryItem,
    top3: CountryItem,
    onCountryClick: (CountryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "champion_gold_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "podium_gold_glow"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 1.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // #2 Second Place (Silver)
        BigPodiumTile(
            country = top2,
            rankLabel = "#2",
            flagFontSize = 32.sp,
            badgeColor = SilverMedal,
            backgroundColor = Color(0xFF131826),
            borderColor = SilverMedal.copy(alpha = 0.8f),
            heightDp = 58,
            onClick = { onCountryClick(top2) },
            modifier = Modifier.weight(1f)
        )

        // #1 First Place Champion (Gold) - Prominent and Taller
        BigPodiumTile(
            country = top1,
            rankLabel = "#1 CHAMPION",
            flagFontSize = 42.sp,
            badgeColor = GoldCrown,
            backgroundColor = Color(0xFF231908),
            borderColor = GoldCrown.copy(alpha = pulseGlow),
            borderWidth = 1.5.dp,
            heightDp = 68,
            isCenterLeader = true,
            onClick = { onCountryClick(top1) },
            modifier = Modifier.weight(1.25f)
        )

        // #3 Third Place (Bronze)
        BigPodiumTile(
            country = top3,
            rankLabel = "#3",
            flagFontSize = 30.sp,
            badgeColor = BronzeMedal,
            backgroundColor = Color(0xFF1F140F),
            borderColor = BronzeMedal.copy(alpha = 0.8f),
            heightDp = 56,
            onClick = { onCountryClick(top3) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BigPodiumTile(
    country: CountryItem,
    rankLabel: String,
    flagFontSize: androidx.compose.ui.unit.TextUnit,
    badgeColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    heightDp: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    isCenterLeader: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(heightDp.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("big_flag_top_${country.rank}"),
        shape = RoundedCornerShape(8.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank badge label
            Text(
                text = rankLabel,
                fontSize = if (isCenterLeader) 8.5.sp else 7.5.sp,
                fontWeight = FontWeight.Black,
                color = badgeColor,
                maxLines = 1
            )

            // HUGE Flag Emoji
            Text(
                text = country.flag,
                fontSize = flagFontSize,
                lineHeight = (flagFontSize.value + 2).sp,
                textAlign = TextAlign.Center
            )

            // Country Name & Score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = country.name,
                    fontSize = if (isCenterLeader) 8.5.sp else 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = Formatters.formatCompactPoints(country.score),
                    fontSize = if (isCenterLeader) 9.sp else 8.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor
                )
            }
        }
    }
}
