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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
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
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.GlassVisualizerMode
import com.example.ui.components.RealtimeGlassVisualizer
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors
import com.example.ui.theme.RefBlueCyanGlow
import com.example.ui.theme.RefCardDark
import com.example.ui.theme.RefMidnightBg
import com.example.ui.theme.RefPurpleGlow
import com.example.ui.theme.RefPurpleLight
import com.example.ui.theme.RefSliderTrack
import com.example.ui.theme.RefTextMuted
import com.example.ui.theme.RefTextSecondary
import com.example.ui.theme.RefTextWhite
import com.example.viewmodel.PlayerUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FullScreenMusicPlayerScreen(
    uiState: PlayerUiState,
    reactiveFrame: AudioReactiveFrame = AudioReactiveFrame(),
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
    val track = uiState.currentTrack
    val currentSec = uiState.currentPositionMs / 1000
    val totalSec = if (uiState.durationMs > 0) uiState.durationMs / 1000 else 288L
    val progress = if (totalSec > 0) (currentSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) else 0.54f

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
            .background(RefMidnightBg)
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
                    tint = RefTextWhite
                )
            }

            IconButton(
                onClick = { onNavigate(Screen.Settings) },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("player_more_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = RefTextWhite
                )
            }
        }

        // Center: Circular Crescent Moon Artwork with Concentric Rings and Glowing Handle
        Box(
            modifier = Modifier
                .size(290.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Concentric Glow Arc and Track
            Canvas(modifier = Modifier.size(280.dp)) {
                val strokeWidth = 5.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2
                val centerOffset = Offset(size.width / 2, size.height / 2)

                // Dotted outer guide ring
                drawCircle(
                    color = Color(0x22A78BFA),
                    radius = radius + 12.dp.toPx(),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
                    )
                )

                // Inner dark ring shadow
                drawCircle(
                    color = Color(0xFF161A24),
                    radius = radius - 8.dp.toPx()
                )

                // Arc Track (Subtle dark purple/blue)
                drawArc(
                    color = Color(0xFF1E2536),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Gradient Arc
                val sweepAngle = 270f * progress
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(RefPurpleGlow, RefBlueCyanGlow, RefPurpleLight),
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
                    color = RefBlueCyanGlow.copy(alpha = 0.45f),
                    radius = 12.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )
                // Solid center dot
                drawCircle(
                    color = Color.White,
                    radius = 6.dp.toPx(),
                    center = Offset(thumbX, thumbY)
                )
            }

            // Inner Album Artwork Container (Noctara / Crescent Moon Night Sky Art)
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFF1F2637), CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Crescent Moon Artwork Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Night sky deep water reflection
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF090D1A), Color(0xFF131D33), Color(0xFF0B1220))
                        )
                    )

                    // Distant mountains silhouette
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
                    drawPath(mountainPath, Color(0xFF050811))

                    // Water shimmer
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0x6638BDF8), Color.Transparent),
                            startY = h * 0.7f,
                            endY = h
                        ),
                        topLeft = Offset(0f, h * 0.7f),
                        size = Size(w, h * 0.3f)
                    )

                    // Crescent Moon
                    val moonCenter = Offset(w * 0.48f, h * 0.42f)
                    val moonRadius = w * 0.28f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0x6693C5FD)),
                            center = moonCenter,
                            radius = moonRadius
                        ),
                        radius = moonRadius,
                        center = moonCenter
                    )
                    // Inner shadow to carve crescent
                    drawCircle(
                        color = Color(0xFF090D1A),
                        radius = moonRadius * 0.95f,
                        center = Offset(moonCenter.x + moonRadius * 0.38f, moonCenter.y - moonRadius * 0.15f)
                    )

                    // Stars
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

            // Timestamp directly under the circular ring: 2:36 / 4:48
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = "${currentSec / 60}:${String.format("%02d", currentSec % 60)} / ${totalSec / 60}:${String.format("%02d", totalSec % 60)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = RefBlueCyanGlow
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
            Column {
                Text(
                    text = track?.title ?: "Midnight Echo",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = RefTextWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track?.artist ?: "Noctara",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RefTextMuted
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("player_favorite_btn")
            ) {
                Icon(
                    imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (uiState.isFavorite) RefPurpleGlow else RefTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Real-time Dynamic Glass Visualizer Waveform Studio
        RealtimeGlassVisualizer(
            reactiveFrame = reactiveFrame,
            isPlaying = uiState.isPlaying,
            mode = GlassVisualizerMode.DYNAMIC_FREQUENCY_BARS,
            heightDp = 38.dp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Horizontal Purple Slider with Glowing Circular Handle & Timestamps
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
                    color = RefSliderTrack,
                    topLeft = Offset(0f, cy - trackHeight / 2),
                    size = Size(size.width, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                )

                // Active purple track
                drawRoundRect(
                    brush = Brush.horizontalGradient(listOf(RefPurpleGlow, RefPurpleLight)),
                    topLeft = Offset(0f, cy - trackHeight / 2),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                )

                // Thumb handle with glow
                val thumbX = activeWidth.coerceIn(8.dp.toPx(), size.width - 8.dp.toPx())
                drawCircle(
                    color = RefPurpleGlow.copy(alpha = 0.4f),
                    radius = 9.dp.toPx(),
                    center = Offset(thumbX, cy)
                )
                drawCircle(
                    color = RefPurpleLight,
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
                    color = RefTextMuted
                )
                Text(
                    text = "${totalSec / 60}:${String.format("%02d", totalSec % 60)}",
                    fontSize = 12.sp,
                    color = RefTextMuted
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
                    .background(Color(0xFF141923))
                    .clickable(onClick = onToggleShuffle)
                    .testTag("btn_shuffle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (uiState.isShuffle) RefBlueCyanGlow else RefTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Previous Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141923))
                    .clickable(onClick = onPrevious)
                    .testTag("btn_prev"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = RefTextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Big Circular Play/Pause Button with Outer Glowing Ring
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .border(2.dp, RefPurpleGlow.copy(alpha = 0.6f), CircleShape)
                    .background(Color(0xFF131722))
                    .clickable(onClick = onPlayPause)
                    .testTag("btn_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                    tint = RefTextWhite,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Next Button
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141923))
                    .clickable(onClick = onNext)
                    .testTag("btn_next"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = RefTextWhite,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Repeat Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF141923))
                    .clickable(onClick = onToggleRepeat)
                    .testTag("btn_repeat"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (uiState.isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    tint = if (uiState.isRepeatOne) RefBlueCyanGlow else RefTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Pill Buttons: [Add to Playlist] | [Lyrics]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF141822))
                .border(1.dp, Color(0xFF1E2536), RoundedCornerShape(22.dp))
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
                    tint = RefTextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Playlist",
                    fontSize = 13.sp,
                    color = RefTextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(20.dp)
                    .background(Color(0xFF242C3F))
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.VisualizerStudio) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = null,
                    tint = RefTextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Lyrics",
                    fontSize = 13.sp,
                    color = RefTextSecondary
                )
            }
        }
    }
}
