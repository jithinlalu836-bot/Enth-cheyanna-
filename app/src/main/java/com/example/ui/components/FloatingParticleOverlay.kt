package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FloatingParticle
import kotlin.math.roundToInt

@Composable
fun FloatingParticleOverlay(
    particles: List<FloatingParticle>,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            SingleFloatingParticleItem(
                particle = particle,
                screenWidthPx = screenWidthPx,
                screenHeightPx = screenHeightPx
            )
        }
    }
}

@Composable
private fun SingleFloatingParticleItem(
    particle: FloatingParticle,
    screenWidthPx: Float,
    screenHeightPx: Float
) {
    val animY = remember { Animatable(0f) }
    val animAlpha = remember { Animatable(1f) }

    LaunchedEffect(particle.id) {
        animY.animateTo(
            targetValue = -180f,
            animationSpec = tween(durationMillis = 1400, easing = LinearOutSlowInEasing)
        )
    }

    LaunchedEffect(particle.id) {
        animAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 1400, delayMillis = 200)
        )
    }

    val initialX = (particle.xPercent * screenWidthPx)
    val initialY = (particle.startYPercent * screenHeightPx) + animY.value

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = initialX.roundToInt(),
                    y = initialY.roundToInt()
                )
            }
            .alpha(animAlpha.value)
    ) {
        Text(
            text = particle.text,
            style = TextStyle(
                color = Color(particle.colorHex),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.8f),
                    blurRadius = 8f
                )
            )
        )
    }
}
