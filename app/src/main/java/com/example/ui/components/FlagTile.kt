package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CountryItem
import com.example.ui.theme.BronzeMedal
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SilverMedal
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.util.Formatters

@Composable
fun FlagTile(
    country: CountryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTapped by remember { mutableStateOf(false) }
    val scaleAnim by animateFloatAsState(
        targetValue = if (isTapped) 0.85f else 1f,
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

    // Top 1 pulse glow
    val infiniteTransition = rememberInfiniteTransition(label = "top_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val isTop1 = country.rank == 1 && country.score > 0
    val isTop2 = country.rank == 2 && country.score > 0
    val isTop3 = country.rank == 3 && country.score > 0
    val isZeroPoints = country.score == 0L

    val borderColor = when {
        isTop1 -> GoldCrown.copy(alpha = pulseGlow)
        isTop2 -> SilverMedal
        isTop3 -> BronzeMedal
        else -> SurfaceCardBorder.copy(alpha = 0.6f)
    }

    Surface(
        modifier = modifier
            .scale(scaleAnim)
            .alpha(if (isZeroPoints) 0.35f else 1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                isTapped = true
                onClick()
            }
            .testTag("flag_tile_${country.id}"),
        shape = RoundedCornerShape(6.dp),
        color = when {
            isTop1 -> Color(0xFF241C0A)
            isTop2 -> Color(0xFF1B202E)
            isTop3 -> Color(0xFF221812)
            else -> SurfaceCard
        },
        border = androidx.compose.foundation.BorderStroke(
            if (isTop1) 1.5.dp else 0.8.dp,
            borderColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 1.dp, vertical = 2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Flag emoji (top 3 country flags are significantly bigger)
                val flagFontSize = when {
                    isTop1 -> 22.sp
                    isTop2 -> 19.sp
                    isTop3 -> 18.sp
                    else -> 13.5.sp
                }

                Text(
                    text = country.flag,
                    fontSize = flagFontSize,
                    lineHeight = (flagFontSize.value + 1).sp,
                    textAlign = TextAlign.Center
                )

                // Score under flag
                Text(
                    text = Formatters.formatCompactPoints(country.score),
                    fontSize = if (country.rank <= 3) 8.sp else 7.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = when {
                        isTop1 -> GoldCrown
                        isTop2 -> SilverMedal
                        isTop3 -> BronzeMedal
                        isZeroPoints -> Color.Gray
                        else -> NeonCyan
                    },
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
