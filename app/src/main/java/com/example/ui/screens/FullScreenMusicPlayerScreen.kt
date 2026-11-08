package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.visualizer.AudioReactiveFrame
import com.example.ui.components.GlassVisualizerMode
import com.example.ui.components.RealtimeGlassVisualizer
import com.example.ui.components.RealtimeLyricsView
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors
import com.example.viewmodel.PlayerUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FullScreenMusicPlayerScreen(
    uiState: PlayerUiState,
    reactiveFrame: AudioReactiveFrame = AudioReactiveFrame(),
    isGeneratingLyrics: Boolean = false,
    onGenerateLyrics: () -> Unit = {},
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val track = uiState.currentTrack
    val currentSec = uiState.currentPositionMs / 1000
    val totalSec = if (uiState.durationMs > 0) uiState.durationMs / 1000 else 288L
    val progress = if (totalSec > 0) (currentSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) else 0.54f

    var showLyrics by remember { mutableStateOf(false) }

    val diskRotation = remember { Animatable(0f) }
    LaunchedEffect(uiState.isPlaying) {
        if (uiState.isPlaying) {
            diskRotation.animateTo(
                targetValue = diskRotation.value + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(24000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundDeep)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .testTag("full_screen_music_player"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Navigation Bar (< and ...)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("player_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }

            Text(
                text = if (showLyrics) "Synchronized Lyrics" else "Now Playing",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textSecondary
            )

            IconButton(
                onClick = { onNavigate(Screen.Settings) },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("player_more_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = colors.textPrimary
                )
            }
        }

        // Center Area: either Realtime synchronized lyrics or the circular glowing crescent art disc
        if (showLyrics) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                RealtimeLyricsView(
                    track = track,
                    currentPositionMs = uiState.currentPositionMs,
                    lyricsContent = track?.lyrics,
                    isGenerating = isGeneratingLyrics,
                    onGenerateWithAi = onGenerateLyrics,
                    onSeekToTimestamp = onSeek,
                    onClose = { showLyrics = false },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Circular Crescent Moon Artwork with Concentric Rings and Glowing Handle
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Concentric Glow Arc and Track
                    Canvas(modifier = Modifier.size(270.dp)) {
                        val strokeWidth = 5.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val centerOffset = Offset(size.width / 2, size.height / 2)

                        // Dotted outer guide ring
                        drawCircle(
                            color = colors.accentGlow.copy(alpha = 0.2f),
                            radius = radius + 10.dp.toPx(),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                            )
                        )

                        // Inner dark ring shadow
                        drawCircle(
                            color = colors.glassCardBackground,
                            radius = radius - 8.dp.toPx()
                        )

                        // Arc Track
                        drawArc(
                            color = colors.glassBorder,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Active Gradient Arc
                        val sweepAngle = 270f * progress
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(colors.accentGlow, colors.secondaryGlow, colors.accentGlow),
                                center = centerOffset
                            ),
                            startAngle = 135f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth + 2f, cap = StrokeCap.Round)
                        )

                        // Glowing Circular Thumb Handle at arc tip
                        val thumbAngleRad = Math.toRadians((135f + sweepAngle).toDouble())
                        val thumbX = centerOffset.x + radius * cos(thumbAngleRad).toFloat()
                        val thumbY = centerOffset.y + radius * sin(thumbAngleRad).toFloat()

                        // Glow halo
                        drawCircle(
                            color = colors.secondaryGlow.copy(alpha = 0.45f),
                            radius = 12.dp.toPx(),
                            center = Offset(thumbX, thumbY)
                        )
                        // Solid center dot
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(thumbX, thumbY)
                        )
                    }

                    // Inner Album Artwork Container
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(CircleShape)
                            .border(3.dp, colors.glassBorder, CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(colors.glassCardBackground, colors.backgroundDeep)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Night sky / theme background
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(colors.backgroundDeep, colors.glassCardBackground)
                                )
                            )

                            // Mountain silhouette
                            val mountainPath = androidx.compose.ui.graphics.Path().apply {
                                moveTo(0f, h * 0.72f)
                                lineTo(w * 0.25f, h * 0.65f)
                                lineTo(w * 0.5f, h * 0.69f)
                                lineTo(w * 0.75f, h * 0.64f)
                                lineTo(w, h * 0.7f)
                                lineTo(w, h)
                                lineTo(0f, h)
                                close()
                            }
                            drawPath(mountainPath, colors.backgroundDeep.copy(alpha = 0.9f))

                            // Water / neon shimmer
                            drawRect(
                                brush = Brush.verticalGradient(
                                    listOf(colors.accentGlow.copy(alpha = 0.35f), Color.Transparent),
                                    startY = h * 0.7f,
                                    endY = h
                                ),
                                topLeft = Offset(0f, h * 0.7f),
                                size = Size(w, h * 0.3f)
                            )

                            // Crescent Moon / Orb
                            val moonCenter = Offset(w * 0.48f, h * 0.42f)
                            val moonRadius = w * 0.28f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    listOf(Color.White, colors.secondaryGlow.copy(alpha = 0.8f), colors.accentGlow.copy(alpha = 0.4f)),
                                    center = moonCenter,
                                    radius = moonRadius
                                ),
                                radius = moonRadius,
                                center = moonCenter
                            )
                            // Inner carve
                            drawCircle(
                                color = colors.backgroundDeep,
                                radius = moonRadius * 0.95f,
                                center = Offset(moonCenter.x + moonRadius * 0.38f, moonCenter.y - moonRadius * 0.15f)
                            )

                            // Shimmer stars
                            val stars = listOf(
                                Offset(w * 0.2f, h * 0.25f),
                                Offset(w * 0.75f, h * 0.28f),
                                Offset(w * 0.85f, h * 0.45f),
                                Offset(w * 0.3f, h * 0.5f),
                                Offset(w * 0.68f, h * 0.18f)
                            )
                            stars.forEach { pt ->
                                drawCircle(Color.White, radius = 1.5f, center = pt)
                            }
                        }
                    }

                    // Timestamp directly under circular ring
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "${currentSec / 60}:${String.format("%02d", currentSec % 60)} / ${totalSec / 60}:${String.format("%02d", totalSec % 60)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.secondaryGlow
                        )
                    }
                }

                // Title, Artist and Heart Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = track?.title ?: "Midnight Echo",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = track?.artist ?: "Noctara",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("player_favorite_btn")
                    ) {
                        Icon(
                            imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (uiState.isFavorite) colors.accentGlow else colors.textMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Real-time Dynamic Glass Visualizer Waveform Studio
                RealtimeGlassVisualizer(
                    reactiveFrame = reactiveFrame,
                    isPlaying = uiState.isPlaying,
                    mode = GlassVisualizerMode.DYNAMIC_FREQUENCY_BARS,
                    heightDp = 36.dp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Horizontal Slider with Glowing Circular Handle & Timestamps
        Column(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek((newProgress * uiState.durationMs).toLong())
                        }
                    }
                    .testTag("player_progress_slider")
            ) {
                val trackHeight = 3.dp.toPx()
                val cy = size.height / 2
                val activeWidth = size.width * progress

                // Inactive track
                drawRoundRect(
                    color = colors.sliderTrack,
                    topLeft = Offset(0f, cy - trackHeight / 2),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                )

                // Active track
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(colors.accentGlow, colors.secondaryGlow)),
                    topLeft = Offset(0f, cy - trackHeight / 2),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                )

                // Thumb handle with glow
                val thumbX = activeWidth.coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
                drawCircle(
                    color = colors.accentGlow.copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = colors.accentGlow,
                    radius = 5.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${currentSec / 60}:${String.format("%02d", currentSec % 60)}",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
                Text(
                    text = "${totalSec / 60}:${String.format("%02d", totalSec % 60)}",
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }
        }

        // Circular Floating Controls: [Shuffle] [Prev] (( Play/Pause )) [Next] [Repeat]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shuffle Button with subtle glass circle
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.glassCardBackground)
                    .clickable(onClick = onToggleShuffle)
                    .testTag("btn_shuffle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.isShuffle) colors.secondaryGlow else colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Previous Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colors.glassCardBackground)
                    .clickable(onClick = onPrevious)
                    .testTag("btn_prev"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Circular Play/Pause Button with Outer Glowing Ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(2.dp, colors.accentGlow.copy(alpha = 0.7f), CircleShape)
                    .background(colors.glassCardBackground)
                    .clickable(onClick = onPlayPause)
                    .testTag("btn_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Next Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colors.glassCardBackground)
                    .clickable(onClick = onNext)
                    .testTag("btn_next"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Repeat Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(colors.glassCardBackground)
                    .clickable(onClick = onToggleRepeat)
                    .testTag("btn_repeat"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (uiState.isRepeatOne) colors.secondaryGlow else colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Pill Buttons: [Add to Playlist] | [Lyrics]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(colors.glassCardBackground)
                .border(1.dp, colors.glassBorder, RoundedCornerShape(22.dp))
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.Playlists) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Playlist",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(colors.glassBorder)
            )

            // Realtime Synchronized Lyrics Pill Button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (showLyrics) colors.accentGlow.copy(alpha = 0.18f) else Color.Transparent)
                    .clickable { showLyrics = !showLyrics }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = "Lyrics",
                    tint = if (showLyrics) colors.accentGlow else colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showLyrics) "Hide Lyrics" else "Lyrics",
                    fontSize = 13.sp,
                    fontWeight = if (showLyrics) FontWeight.Bold else FontWeight.Medium,
                    color = if (showLyrics) colors.accentGlow else colors.textSecondary
                )
            }
        }
    }
}
