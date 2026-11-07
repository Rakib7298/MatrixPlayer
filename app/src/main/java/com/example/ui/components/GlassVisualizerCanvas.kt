package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.media.visualizer.AudioReactiveFrame
import kotlin.math.sin

enum class GlassVisualizerMode {
    DYNAMIC_FREQUENCY_BARS,
    CRYSTAL_WAVEFORM,
    MIRROR_SPECTRUM,
    GLASS_ORB_PULSE
}

@Composable
fun RealtimeGlassVisualizer(
    reactiveFrame: AudioReactiveFrame,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    mode: GlassVisualizerMode = GlassVisualizerMode.DYNAMIC_FREQUENCY_BARS,
    primaryColor: Color = Color(0xFF00F0FF),
    secondaryColor: Color = Color(0xFFB026FF),
    heightDp: Dp = 42.dp
) {
    val animTime = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animTime.animateTo(
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(40000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x11161A28))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .testTag("realtime_glass_visualizer"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = animTime.value
            val waveData = reactiveFrame.waveform
            val fftData = reactiveFrame.fftMagnitudes
            val cx = size.width / 2f
            val cy = size.height / 2f

            when (mode) {
                GlassVisualizerMode.DYNAMIC_FREQUENCY_BARS -> {
                    val barCount = 42
                    val spacing = size.width / barCount
                    val barWidth = (spacing * 0.55f).coerceAtLeast(2f)

                    for (i in 0 until barCount) {
                        val fftMag = fftData.getOrElse(i % fftData.size) { 0.2f }
                        val simVal = if (isPlaying) (sin(i * 0.25 + t * 0.1) * 0.35 + 0.45).toFloat() else 0.15f
                        val rawMag = if (reactiveFrame.isHardwareDriven) fftMag else simVal
                        val barHeight = (size.height * 0.85f * rawMag).coerceIn(4f, size.height * 0.95f)

                        val x = i * spacing + (spacing - barWidth) / 2f
                        val y = (size.height - barHeight) / 2f

                        val barBrush = Brush.verticalGradient(
                            listOf(primaryColor, secondaryColor),
                            startY = y,
                            endY = y + barHeight
                        )

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
                GlassVisualizerMode.CRYSTAL_WAVEFORM -> {
                    val path = Path()
                    val points = waveData.size
                    for (i in 0 until points) {
                        val x = (i.toFloat() / (points - 1)) * size.width
                        val sample = if (reactiveFrame.isHardwareDriven) waveData[i] else (sin(i * 0.15 + t * 0.08) * 0.5).toFloat()
                        val y = cy + (sample * (size.height * 0.4f))
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(listOf(primaryColor, secondaryColor, primaryColor)),
                        style = Stroke(width = 4f)
                    )
                }
                GlassVisualizerMode.MIRROR_SPECTRUM -> {
                    val bars = 24
                    val spacing = cx / bars
                    val barWidth = spacing * 0.6f

                    for (i in 0 until bars) {
                        val mag = fftData.getOrElse(i) { 0.2f }
                        val h = (mag * (size.height * 0.42f)).coerceAtLeast(3f)

                        val leftX = cx - (i * spacing) - barWidth
                        val rightX = cx + (i * spacing)

                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(leftX, cy - h),
                            size = Size(barWidth, h * 2f),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                        drawRoundRect(
                            color = secondaryColor,
                            topLeft = Offset(rightX, cy - h),
                            size = Size(barWidth, h * 2f),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
                GlassVisualizerMode.GLASS_ORB_PULSE -> {
                    val bass = if (reactiveFrame.isHardwareDriven) reactiveFrame.bassEnergy else (0.5f + 0.3f * sin(t * 0.1)).toFloat()
                    val r = (size.height * 0.35f) * (0.8f + bass * 0.4f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(primaryColor.copy(alpha = 0.8f), secondaryColor.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                }
            }
        }
    }
}
