package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMatrixColors

@Composable
fun CrystalGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    backgroundColor: Color = LocalMatrixColors.current.glassCardBackground,
    borderColor: Color = LocalMatrixColors.current.glassBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, shape)
            .then(clickModifier),
        content = content
    )
}

@Composable
fun MatrixBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val matrixColors = LocalMatrixColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "background_glow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(matrixColors.backgroundDeep)
            .drawBehind {
                // Ambient glow orbs in the background
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            matrixColors.accentGlow.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.2f, size.height * 0.15f + glowOffset),
                        radius = size.width * 0.6f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            matrixColors.progressRingGlow.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.75f - glowOffset),
                        radius = size.width * 0.55f
                    )
                )
            },
        content = content
    )
}

@Composable
fun GlowingProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp,
    color: Color = LocalMatrixColors.current.progressRingGlow,
    trackColor: Color = Color(0x22FFFFFF)
) {
    Box(
        modifier = modifier
            .drawBehind {
                val strokePx = strokeWidth.toPx()
                // Track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
                // Active glow ring
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (progress.coerceIn(0f, 1f)) * 360f,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
    )
}

@Composable
fun AnimatedWaveformBadge(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    activeColor: Color = LocalMatrixColors.current.waveformActive
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val heights = (0 until barCount).map { index ->
        if (isPlaying) {
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (index * 120),
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$index"
            ).value
        } else {
            0.25f
        }
    }

    Box(
        modifier = modifier.drawBehind {
            val totalWidth = size.width
            val barWidth = totalWidth / (barCount * 2 - 1)
            heights.forEachIndexed { i, factor ->
                val x = i * barWidth * 2
                val h = size.height * factor
                val y = (size.height - h) / 2
                drawRoundRect(
                    color = activeColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    )
}
