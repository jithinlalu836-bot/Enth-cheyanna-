package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinBurstData
import com.example.ui.theme.GoldCrown
import com.example.ui.theme.NeonCyan
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CoinBurst(
    bursts: List<CoinBurstData>,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        bursts.forEach { burst ->
            SingleCoinBurstItem(
                burst = burst,
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx
            )
        }
    }
}

@Composable
private fun SingleCoinBurstItem(
    burst: CoinBurstData,
    screenWidthPx: Float,
    screenHeightPx: Float
) {
    val animProgress = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(burst.id) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing)
        )
    }

    LaunchedEffect(burst.id) {
        animAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1100, delayMillis = 400)
        )
    }

    val originX = burst.xPercent * screenWidthPx
    val originY = burst.yPercent * screenHeightPx

    Box(modifier = Modifier.fillMaxSize()) {
        // Floating pill with @username and points
        val pillYOffset = originY - (animProgress.value * 90f)
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (originX - 60f).roundToInt(),
                        y = pillYOffset.roundToInt()
                    )
                }
                .alpha(animAlpha.value)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(1.dp, GoldCrown, RoundedCornerShape(8.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = burst.countryFlag, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "@${burst.username}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = burst.text,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldCrown
                    )
                }
            }
        }

        // Bursting Gold Coins (radiating outward)
        val coinCount = burst.coinCount
        for (i in 0 until coinCount) {
            val angle = (i.toFloat() / coinCount.toFloat()) * 2f * Math.PI.toFloat()
            val distance = animProgress.value * 50f
            val coinX = originX + (cos(angle) * distance)
            val coinY = originY + (sin(angle) * distance) - (animProgress.value * 25f)

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(coinX.roundToInt(), coinY.roundToInt())
                    }
                    .alpha(animAlpha.value)
                    .scale(1f - (animProgress.value * 0.3f))
            ) {
                Text(
                    text = "🪙",
                    fontSize = 11.sp
                )
            }
        }
    }
}
