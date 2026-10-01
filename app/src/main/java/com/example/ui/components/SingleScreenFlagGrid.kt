package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Country
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.LiveRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SilverMedal
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.Formatters

@Composable
fun SingleScreenFlagGrid(
    countries: List<Country>,
    activeCountryId: String,
    onCountryBoostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val totalWidth = maxWidth
        // 4 columns for mobile, 6 columns for tablets or landscape
        val columns = if (totalWidth > 550.dp) 6 else 4
        val rows = (countries.size + columns - 1) / columns

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val chunkedCountries = countries.chunked(columns)
            chunkedCountries.forEach { rowCountries ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowCountries.forEach { country ->
                        CompactCountryTile(
                            country = country,
                            isUserCountry = country.id == activeCountryId,
                            onBoostClick = { onCountryBoostClick(country.id) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        )
                    }

                    // Fill any empty columns in the last row to maintain grid alignment
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

@Composable
private fun CompactCountryTile(
    country: Country,
    isUserCountry: Boolean,
    onBoostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTapped by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isTapped) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tile_scale"
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            kotlinx.coroutines.delay(120)
            isTapped = false
        }
    }

    val rankBorder = when (country.currentRank) {
        1 -> GoldCrown
        2 -> SilverMedal
        3 -> BronzeMedal
        else -> if (isUserCountry) NeonCyan else SurfaceCardBorder
    }

    val rankBg = when (country.currentRank) {
        1 -> GoldCrown.copy(alpha = 0.25f)
        2 -> SilverMedal.copy(alpha = 0.2f)
        3 -> BronzeMedal.copy(alpha = 0.2f)
        else -> Color.Transparent
    }

    Surface(
        modifier = modifier
            .scale(scaleAnim)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                isTapped = true
                onBoostClick()
            }
            .testTag("compact_flag_tile_${country.id}"),
        shape = RoundedCornerShape(8.dp),
        color = if (isUserCountry) SurfaceVariantDark else SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            if (isUserCountry || country.currentRank <= 3) 1.2.dp else 1.dp,
            rankBorder
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(rankBg)
                .padding(horizontal = 3.dp, vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Rank number badge + Rank Delta indicator + User marker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rank badge
                    Text(
                        text = "#${country.currentRank}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = when (country.currentRank) {
                            1 -> GoldCrown
                            2 -> SilverMedal
                            3 -> BronzeMedal
                            else -> TextSecondary
                        }
                    )

                    // Rank shift or user pill
                    if (isUserCountry) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(NeonCyan)
                                .padding(horizontal = 2.dp, vertical = 0.5.dp)
                        ) {
                            Text(
                                text = "YOU",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    } else if (country.rankDelta != 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (country.rankDelta > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (country.rankDelta > 0) NeonGreen else LiveRed,
                                modifier = Modifier.size(9.dp)
                            )
                            Text(
                                text = "${Math.abs(country.rankDelta)}",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (country.rankDelta > 0) NeonGreen else LiveRed
                            )
                        }
                    } else {
                        // Empty space to balance row
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // Center: Flag Emoji (Large and crisp)
                Text(
                    text = country.flag,
                    fontSize = 20.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                // Bottom: Country Name & Live Score counter
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = country.name,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = Formatters.formatCompactPoints(country.score),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (country.currentRank) {
                            1 -> GoldCrown
                            2 -> SilverMedal
                            3 -> BronzeMedal
                            else -> NeonCyan
                        },
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
