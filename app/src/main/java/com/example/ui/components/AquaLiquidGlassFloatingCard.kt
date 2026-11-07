package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackItem
import kotlin.math.sin

/**
 * Pixel-perfect clone of UI Reference 2:
 * Super Crystal Liquid Glass Floating Player Card with realistic water ripple backdrop,
 * squared rounded album art, title, artist, audio waveform, and circular frosted buttons.
 */
@Composable
fun AquaLiquidGlassFloatingCard(
    track: TrackItem?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClickCard: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    val curSec = currentPositionMs / 1000
    val durSec = if (durationMs > 0) durationMs / 1000 else 281L
    val progress = if (durSec > 0) (curSec.toFloat() / durSec.toFloat()).coerceIn(0f, 1f) else 0.3f

    // Outer Liquid Glass Container (Frosted blue-green acrylic with rounded 32dp corners)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xD9063F61), // Aqua ocean translucent glass
                        Color(0xB3032840),
                        Color(0xE6021B2B)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x807DD3FC), // highlight crystal edge
                        Color(0x2638BDF8),
                        Color(0x400284C7)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .clickable(onClick = onClickCard)
            .padding(22.dp)
            .testTag("floating_playback_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Row: Square Album Art & Title / Artist
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Square Album Artwork with rounded corners (16dp)
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2B48), Color(0xFF1E3A8A), Color(0xFF0284C7))
                            )
                        )
                        .border(1.dp, Color(0x33BAE6FD), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Ocean swimmer / sitting on board illustration simulation
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF032644), Color(0xFF084B75))
                            )
                        )
                        // Horizon line / diving board
                        drawLine(
                            color = Color.White,
                            start = Offset(size.width * 0.35f, size.height * 0.55f),
                            end = Offset(size.width * 0.85f, size.height * 0.55f),
                            strokeWidth = 3f
                        )
                        // Figure sitting silhouette
                        drawCircle(
                            color = Color(0xFFE2E8F0),
                            radius = 5f,
                            center = Offset(size.width * 0.45f, size.height * 0.42f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Artist
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFBAE6FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Timestamps: 01:20 & 04:41
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${curSec / 60}:${String.format("%02d", curSec % 60)}",
                    fontSize = 12.sp,
                    color = Color(0xFFE0F2FE)
                )
                Text(
                    text = "${durSec / 60}:${String.format("%02d", durSec % 60)}",
                    fontSize = 12.sp,
                    color = Color(0xFFE0F2FE)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Waveform Audio Bars (White active bars, translucent inactive bars)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
            ) {
                val barCount = 42
                val spacing = size.width / barCount
                val barWidth = spacing * 0.45f

                for (i in 0 until barCount) {
                    val factor = when {
                        i in 0..12 -> 0.4f + 0.55f * sin((i.toFloat() / 12f) * Math.PI.toFloat())
                        i in 13..28 -> 0.25f + 0.25f * sin(((i - 13).toFloat() / 15f) * Math.PI.toFloat())
                        else -> 0.3f + 0.35f * sin(((i - 28).toFloat() / 14f) * Math.PI.toFloat())
                    }
                    val barHeight = size.height * factor
                    val x = i * spacing + barWidth / 2
                    val y = (size.height - barHeight) / 2

                    val isPlayed = (i.toFloat() / barCount) <= progress
                    val color = if (isPlayed) Color.White else Color(0x66FFFFFF)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3 Circular Glass Playback Buttons: [|<<] [ > ] [>>|]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prev
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0x33022B42))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable(onClick = onPrevious)
                        .testTag("mini_prev_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Play / Pause (Slightly larger crystal button)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0x4D023857))
                        .border(1.5.dp, Color(0x66FFFFFF), CircleShape)
                        .clickable(onClick = onPlayPause)
                        .testTag("mini_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                // Next
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0x33022B42))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable(onClick = onNext)
                        .testTag("mini_next_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
