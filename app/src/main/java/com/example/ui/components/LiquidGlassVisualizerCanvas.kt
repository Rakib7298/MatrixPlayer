package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.visualizer.AudioReactiveFrame
import com.example.ui.theme.LocalMatrixColors
import kotlin.math.cos
import kotlin.math.sin

enum class LiquidVisualizerType(val label: String) {
    NEON_SPECTRUM("Neon Spectrum Bars"),
    LIQUID_WAVE("Liquid Chromatic Wave"),
    CIRCULAR_HALO("Circular Halo Pulse"),
    MIRROR_PEAK("Mirror Symmetric Peak EQ")
}

/**
 * Liquid Glass Visualizer Canvas component.
 * Uses Media3's audio session ID / AudioReactiveFrame to render real-time frequency data,
 * perfectly aligning with the Liquid Glass UI theme (specular borders, reflections, and glow).
 */
@Composable
fun LiquidGlassVisualizerCanvas(
    reactiveFrame: AudioReactiveFrame,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    heightDp: Dp = 140.dp,
    showControlsHeader: Boolean = true,
    initialType: LiquidVisualizerType = LiquidVisualizerType.NEON_SPECTRUM,
    accentColor: Color = Color(0xFF00F0FF),
    secondaryColor: Color = Color(0xFFB026FF)
) {
    val colors = LocalMatrixColors.current
    var currentType by remember { mutableStateOf(initialType) }

    val animPhase = remember { Animatable(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animPhase.animateTo(
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween(30000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    CrystalGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("liquid_glass_visualizer_canvas"),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
        borderColor = accentColor.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (showControlsHeader) {
                // Top telemetry & mode switcher row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF94A3B8))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (reactiveFrame.isHardwareDriven) "HARDWARE FFT (Media3 Session)" else "DSP REALTIME SPECTRUM",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = if (reactiveFrame.isHardwareDriven) Color(0xFF10B981) else colors.accentGlow
                        )
                    }

                    // Interactive Mode Switcher Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.backgroundDeep.copy(alpha = 0.7f))
                            .border(0.6.dp, colors.glassBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                val nextIndex = (currentType.ordinal + 1) % LiquidVisualizerType.values().size
                                currentType = LiquidVisualizerType.values()[nextIndex]
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = accentColor, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentType.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Real-Time Canvas Rendering Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heightDp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .border(
                        0.8.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent,
                                accentColor.copy(alpha = 0.2f)
                            )
                        ),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        val nextIndex = (currentType.ordinal + 1) % LiquidVisualizerType.values().size
                        currentType = LiquidVisualizerType.values()[nextIndex]
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (size.width <= 0f || size.height <= 0f) return@Canvas

                    val t = animPhase.value
                    val wave = reactiveFrame.waveform
                    val fft = reactiveFrame.fftMagnitudes
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    when (currentType) {
                        LiquidVisualizerType.NEON_SPECTRUM -> {
                            val barCount = 38
                            val spacing = w / barCount
                            val barWidth = (spacing * 0.65f).coerceAtLeast(3f)
                            val maxBarH = (h * 0.92f).coerceAtLeast(1f)
                            val minBarH = 4f.coerceAtMost(maxBarH)

                            for (i in 0 until barCount) {
                                val fftVal = fft.getOrElse(i % fft.size) { 0.25f }
                                val simWave = if (isPlaying) (sin(i * 0.22 + t * 0.12) * 0.35 + 0.45).toFloat() else 0.1f
                                val magnitude = if (reactiveFrame.isHardwareDriven) fftVal else simWave
                                val barHeight = (h * 0.85f * magnitude).coerceIn(minBarH, maxBarH)

                                val x = i * spacing + (spacing - barWidth) / 2f
                                val y = h - barHeight - 4.dp.toPx()

                                // Liquid vertical gradient with neon glow
                                val barBrush = Brush.verticalGradient(
                                    listOf(accentColor, secondaryColor),
                                    startY = y,
                                    endY = y + barHeight
                                )

                                // Main Frequency Bar
                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(x, y),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )

                                // Glass Floor Reflection (Subtle mirror sheen below bar)
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(accentColor.copy(alpha = 0.2f), Color.Transparent),
                                        startY = h - 3.dp.toPx(),
                                        endY = h
                                    ),
                                    topLeft = Offset(x, h - 3.dp.toPx()),
                                    size = Size(barWidth, (barHeight * 0.25f).coerceAtMost(6.dp.toPx())),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            }
                        }

                        LiquidVisualizerType.LIQUID_WAVE -> {
                            val path = Path()
                            val fillPath = Path()
                            val points = 48

                            for (i in 0 until points) {
                                val x = (i.toFloat() / (points - 1)) * w
                                val waveSample = wave.getOrElse(i % wave.size) { 0f }
                                val simWave = (sin(i * 0.25 + t * 0.15) * 0.5 + sin(i * 0.1 - t * 0.08) * 0.3).toFloat()
                                val sample = if (reactiveFrame.isHardwareDriven) waveSample else simWave
                                val y = cy + (sample * (h * 0.38f))

                                if (i == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, h)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }
                            }

                            fillPath.lineTo(w, h)
                            fillPath.close()

                            // Liquid gradient glow fill underneath curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    listOf(accentColor.copy(alpha = 0.35f), secondaryColor.copy(alpha = 0.08f), Color.Transparent),
                                    startY = cy - h * 0.3f,
                                    endY = h
                                )
                            )

                            // High-definition Neon Ribbon Stroke
                            drawPath(
                                path = path,
                                brush = Brush.horizontalGradient(listOf(accentColor, secondaryColor, accentColor)),
                                style = Stroke(width = 3.5.dp.toPx())
                            )
                        }

                        LiquidVisualizerType.CIRCULAR_HALO -> {
                            val bassEnergy = if (reactiveFrame.isHardwareDriven) reactiveFrame.bassEnergy else (0.5f + 0.35f * sin(t * 0.2)).toFloat()
                            val midEnergy = if (reactiveFrame.isHardwareDriven) reactiveFrame.midEnergy else (0.4f + 0.25f * cos(t * 0.15)).toFloat()

                            val baseRadius = (h * 0.3f).coerceAtLeast(20.dp.toPx())
                            val outerRadius = baseRadius * (1f + bassEnergy * 0.45f)
                            val innerRadius = baseRadius * (0.85f + midEnergy * 0.3f)

                            // Outer glowing halo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(accentColor.copy(alpha = 0.6f), secondaryColor.copy(alpha = 0.2f), Color.Transparent),
                                    center = Offset(cx, cy),
                                    radius = outerRadius
                                ),
                                radius = outerRadius,
                                center = Offset(cx, cy)
                            )

                            // Inner specular glass ring
                            drawCircle(
                                color = accentColor,
                                radius = innerRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Frequency orbit particles
                            val particleCount = 16
                            for (p in 0 until particleCount) {
                                val angle = (p.toFloat() / particleCount) * 2 * Math.PI + (t * 0.05)
                                val orbitDist = innerRadius + (fft.getOrElse(p % fft.size) { 0.3f } * 20.dp.toPx())
                                val px = cx + (cos(angle) * orbitDist).toFloat()
                                val py = cy + (sin(angle) * orbitDist).toFloat()

                                drawCircle(
                                    color = if (p % 2 == 0) accentColor else secondaryColor,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(px, py)
                                )
                            }
                        }

                        LiquidVisualizerType.MIRROR_PEAK -> {
                            val bars = 22
                            val halfW = w / 2f
                            val spacing = halfW / bars
                            val barWidth = spacing * 0.7f
                            val maxBarH = (h * 0.46f).coerceAtLeast(1f)
                            val minBarH = (3.dp.toPx()).coerceAtMost(maxBarH)

                            for (i in 0 until bars) {
                                val mag = fft.getOrElse(i) { 0.2f }
                                val barHeight = (mag * (h * 0.42f)).coerceIn(minBarH, maxBarH)

                                val leftX = halfW - (i * spacing) - barWidth
                                val rightX = halfW + (i * spacing)

                                // Left channel (Cyan to Blue)
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(accentColor, Color(0xFF0051FF)),
                                        startY = cy - barHeight,
                                        endY = cy + barHeight
                                    ),
                                    topLeft = Offset(leftX, cy - barHeight),
                                    size = Size(barWidth, barHeight * 2f),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )

                                // Right channel (Magenta to Violet)
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(Color(0xFFFF2A85), secondaryColor),
                                        startY = cy - barHeight,
                                        endY = cy + barHeight
                                    ),
                                    topLeft = Offset(rightX, cy - barHeight),
                                    size = Size(barWidth, barHeight * 2f),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            }
                        }
                    }
                }
            }

            if (showControlsHeader) {
                Spacer(modifier = Modifier.height(6.dp))

                // Bottom frequency band energy metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BASS: ${(reactiveFrame.bassEnergy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontFamily = FontFamily.Monospace),
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "MID: ${(reactiveFrame.midEnergy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontFamily = FontFamily.Monospace),
                        color = accentColor
                    )
                    Text(
                        text = "TREBLE: ${(reactiveFrame.trebleEnergy * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontFamily = FontFamily.Monospace),
                        color = secondaryColor
                    )
                }
            }
        }
    }
}
