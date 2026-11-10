package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.visualizer.AudioReactiveFrame
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlin.math.cos
import kotlin.math.sin

val VisualizerStylesList = listOf(
    "1. Circular Spectrum",
    "2. Spectrum Bars",
    "3. Waveform",
    "4. 3D Particles",
    "5. Radial Pulse",
    "6. Liquid Flow",
    "7. Mirror Spectrum",
    "8. Oscilloscope",
    "9. Audio Landscape",
    "10. 3D Geometry"
)

@Composable
fun VisualizerStudioScreen(
    selectedIndex: Int,
    isPlaying: Boolean,
    reactiveFrame: AudioReactiveFrame,
    onSelectVisualizer: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    var sensitivity by remember { mutableFloatStateOf(1.0f) }
    var speed by remember { mutableFloatStateOf(1.0f) }
    var batterySaver by remember { mutableStateOf(false) }

    val animTime = remember { Animatable(0f) }

    LaunchedEffect(isPlaying, speed) {
        if (isPlaying) {
            animTime.animateTo(
                targetValue = 1000f,
                animationSpec = infiniteRepeatable(
                    animation = tween((50000 / speed).toInt(), easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .testTag("visualizer_studio_screen")
    ) {
        MatrixTopAppBar(
            title = "Visualizer Studio",
            onBackClick = onBack,
            actions = {
                // Battery Saver toggle
                IconButton(onClick = { batterySaver = !batterySaver }) {
                    Icon(
                        imageVector = Icons.Default.BatterySaver,
                        contentDescription = "Battery Saver",
                        tint = if (batterySaver) colors.accentGlow else colors.textSecondary
                    )
                }
            }
        )

        // Visualizer Canvas Display Box
        CrystalGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = colors.backgroundDeep.copy(alpha = 0.95f),
            borderColor = colors.glassBorder,
            borderWidth = 1.5.dp,
            testTag = "visualizer_canvas_card"
        ) {
            val t = animTime.value * if (batterySaver) 0.5f else 1.0f
            val glowColor = colors.accentGlow
            val secondaryColor = colors.progressRingGlow
            val waveData = reactiveFrame.waveform
            val fftData = reactiveFrame.fftMagnitudes

            Canvas(modifier = Modifier.fillMaxSize()) {
                if (size.width <= 0f || size.height <= 0f) return@Canvas

                val cx = size.width / 2
                val cy = size.height / 2

                when (selectedIndex) {
                    0 -> { // 1. Circular Spectrum
                        val barCount = 48
                        val baseRadius = size.minDimension * 0.28f
                        for (i in 0 until barCount) {
                            val angle = (i.toFloat() / barCount) * (2 * Math.PI)
                            val fftMag = fftData.getOrElse(i % fftData.size) { 0.3f }
                            val amplitude = (fftMag * 80f + sin(angle * 3 + t * 0.1) * 20f) * sensitivity
                            val r1 = baseRadius
                            val r2 = baseRadius + amplitude.toFloat()
                            val startX = (cx + r1 * cos(angle)).toFloat()
                            val startY = (cy + r1 * sin(angle)).toFloat()
                            val endX = (cx + r2 * cos(angle)).toFloat()
                            val endY = (cy + r2 * sin(angle)).toFloat()
                            drawLine(
                                color = if (i % 2 == 0) glowColor else secondaryColor,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 4f
                            )
                        }
                    }
                    1 -> { // 2. Spectrum Bars
                        val bars = 32
                        val barWidth = size.width / (bars * 1.5f)
                        val maxBarH = (size.height * 0.8f).coerceAtLeast(1f)
                        val minBarH = 4f.coerceAtMost(maxBarH)
                        for (i in 0 until bars) {
                            val mag = fftData.getOrElse(i) { 0.2f }
                            val barHeight = (size.height * 0.65f * mag * sensitivity).coerceIn(minBarH, maxBarH)
                            val x = i * (barWidth * 1.5f) + barWidth * 0.5f
                            val y = size.height - barHeight
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(glowColor, secondaryColor),
                                    startY = y,
                                    endY = size.height
                                ),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight)
                            )
                        }
                    }
                    2 -> { // 3. Waveform
                        val path = Path()
                        val points = waveData.size
                        for (i in 0 until points) {
                            val x = (i.toFloat() / (points - 1)) * size.width
                            val waveSample = waveData[i]
                            val y = cy + (waveSample * 120f * sensitivity)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, glowColor, style = Stroke(width = 5f))
                    }
                    3 -> { // 4. 3D Particles
                        val count = 48
                        for (i in 0 until count) {
                            val mag = fftData.getOrElse(i % fftData.size) { 0.3f }
                            val px = (cx + cos(i.toDouble() + t * 0.05) * (i * 4f * mag) * sensitivity).toFloat()
                            val py = (cy + sin(i.toDouble() + t * 0.05) * (i * 3.5f * mag) * sensitivity).toFloat()
                            drawCircle(
                                color = if (i % 3 == 0) glowColor else secondaryColor,
                                radius = (mag * 12f * sensitivity).coerceAtLeast(3f),
                                center = Offset(px % size.width, py % size.height)
                            )
                        }
                    }
                    4 -> { // 5. Radial Pulse
                        val rings = 6
                        for (i in 0 until rings) {
                            val pulseRadius = ((t * 22f + i * 45f + reactiveFrame.bassEnergy * 30f) % (size.minDimension * 0.45f)) * sensitivity
                            drawCircle(
                                color = glowColor.copy(alpha = (1f - pulseRadius / (size.minDimension * 0.45f)).coerceIn(0f, 1f)),
                                radius = pulseRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 4f + reactiveFrame.bassEnergy * 4f)
                            )
                        }
                    }
                    5 -> { // 6. Liquid Flow
                        val path = Path()
                        val steps = 80
                        for (i in 0..steps) {
                            val x = (i.toFloat() / steps) * size.width
                            val mag = fftData.getOrElse(i % fftData.size) { 0.3f }
                            val y = (cy + sin(x * 0.015 + t * 0.08) * cos(x * 0.008 - t * 0.04) * (70f + mag * 50f) * sensitivity).toFloat()
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path, secondaryColor, style = Stroke(width = 6f))
                    }
                    6 -> { // 7. Mirror Spectrum
                        val bars = 24
                        val barWidth = size.width / (bars * 2.2f)
                        val maxH = (size.height * 0.45f).coerceAtLeast(1f)
                        val minH = 4f.coerceAtMost(maxH)
                        for (i in 0 until bars) {
                            val mag = fftData.getOrElse(i) { 0.25f }
                            val h = (mag * 140f * sensitivity).coerceIn(minH, maxH)
                            val xLeft = cx - (i * barWidth * 1.5f)
                            val xRight = cx + (i * barWidth * 1.5f)
                            drawLine(glowColor, Offset(xLeft, cy - h), Offset(xLeft, cy + h), strokeWidth = barWidth)
                            drawLine(secondaryColor, Offset(xRight, cy - h), Offset(xRight, cy + h), strokeWidth = barWidth)
                        }
                    }
                    7 -> { // 8. Oscilloscope
                        val path = Path()
                        for (deg in 0..360 step 4) {
                            val rad = Math.toRadians(deg.toDouble())
                            val idx = (deg / 6).coerceIn(0, waveData.size - 1)
                            val r = (size.minDimension * 0.28f + (waveData[idx] * 50f + sin(rad * 8 + t * 0.2) * 20f) * sensitivity).toFloat()
                            val x = (cx + r * cos(rad)).toFloat()
                            val y = (cy + r * sin(rad)).toFloat()
                            if (deg == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        path.close()
                        drawPath(path, glowColor, style = Stroke(width = 4f))
                    }
                    8 -> { // 9. Audio Landscape
                        for (layer in 0..3) {
                            val path = Path()
                            val pts = 40
                            val baseOffsetY = cy + (layer * 35f)
                            for (i in 0..pts) {
                                val x = (i.toFloat() / pts) * size.width
                                val mag = fftData.getOrElse((i + layer) % fftData.size) { 0.2f }
                                val y = (baseOffsetY - (mag * 60f + sin(i * 0.3 + layer + t * 0.08) * 35f) * sensitivity).toFloat()
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(path, if (layer % 2 == 0) glowColor else secondaryColor, style = Stroke(width = 3.5f))
                        }
                    }
                    9 -> { // 10. 3D Geometry
                        val points = 8
                        for (i in 0 until points) {
                            val angle1 = (i.toFloat() / points) * (2 * Math.PI) + t * 0.03
                            val angle2 = ((i + 1).toFloat() / points) * (2 * Math.PI) + t * 0.03
                            val mag = fftData.getOrElse(i % fftData.size) { 0.3f }
                            val r = (size.minDimension * 0.28f + mag * 40f) * sensitivity
                            val p1 = Offset((cx + r * cos(angle1)).toFloat(), (cy + r * sin(angle1)).toFloat())
                            val p2 = Offset((cx + r * cos(angle2)).toFloat(), (cy + r * sin(angle2)).toFloat())
                            drawLine(glowColor, p1, p2, strokeWidth = 4f)
                            drawLine(secondaryColor, Offset(cx, cy), p1, strokeWidth = 2f)
                        }
                    }
                }
            }

            // Real audio data indicator badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(14.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (reactiveFrame.isHardwareDriven) "AUDIO-REACTIVE HARDWARE FFT" else "AUDIO-REACTIVE DSP ENGINE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentGlow
                )
            }
        }

        // Style Selector Horizontal Pills
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = "STYLES (10 REAL MODES)",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(VisualizerStylesList) { index, styleName ->
                    val isSelected = index == selectedIndex
                    CrystalGlassCard(
                        modifier = Modifier.clickable { onSelectVisualizer(index) },
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isSelected) colors.accentGlow.copy(alpha = 0.25f) else colors.backgroundSurface.copy(alpha = 0.7f),
                        borderColor = if (isSelected) colors.accentGlow else colors.glassBorder,
                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                        testTag = "vis_style_$index"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = colors.accentGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = styleName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) colors.accentGlow else colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Sensitivity & Speed Controls Panel
        CrystalGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
            borderColor = colors.glassBorder
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Sensitivity: ${(sensitivity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                    Text("Speed: ${(speed * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = colors.accentGlow)
                }

                Slider(
                    value = sensitivity,
                    onValueChange = { sensitivity = it },
                    valueRange = 0.2f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = colors.accentGlow, activeTrackColor = colors.accentGlow)
                )
            }
        }
    }
}
