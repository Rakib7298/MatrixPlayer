package com.example.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.media.video.MatrixVideoEngine
import com.example.media.video.VideoAspectRatioMode
import com.example.model.VideoItem
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.delay

/**
 * Super Crystal Liquid Glass Video Player.
 * Cloned directly from user Reference Image 2:
 * - 100% visible video surface (no blocking mock backdrops or opaque canvas)
 * - Floating crystal liquid glass transparent pill control bars
 * - Top Bar: Title on left, floating pill on right [Aspect/Loop, CC, More ⋮]
 * - Center: Floating transparent glass pill with [Prev | Play/Pause | Next]
 * - Bottom Bar: Progress timeline with white glowing thumb
 *   Bottom-Left Pill: [Fit/Aspect, GIF/Screenshot]
 *   Bottom-Right Pill: [PiP/Resize, Speed, Rotate, Lock]
 * - Full horizontal & vertical gestures (seek, brightness, volume, double-tap 10s, pinch-to-zoom)
 */
@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayerScreen(
    video: VideoItem?,
    videoEngine: MatrixVideoEngine,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    subtitleManager: com.example.media.subtitle.MatrixSubtitleManager? = null,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val colors = LocalMatrixColors.current

    // Auto Smooth Rotate System: Detects video aspect ratio and smoothly adapts screen orientation
    androidx.compose.runtime.DisposableEffect(videoEngine.player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                if (videoSize.width > 0 && videoSize.height > 0) {
                    activity?.let { act ->
                        if (videoSize.width > videoSize.height) {
                            if (act.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            }
                        } else {
                            if (act.requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT) {
                                act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            }
                        }
                    }
                }
            }
        }
        videoEngine.player.addListener(listener)

        val curSize = videoEngine.player.videoSize
        if (curSize.width > 0 && curSize.height > 0) {
            activity?.let { act ->
                if (curSize.width > curSize.height) {
                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                } else {
                    act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                }
            }
        }

        onDispose {
            videoEngine.player.removeListener(listener)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // In Picture-in-Picture mode, render ONLY the full-bleed video surface without any UI controls or overlays
    if (isInPipMode) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = videoEngine.player
                    useController = false
                    keepScreenOn = true
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { view ->
                view.player = videoEngine.player
                view.keepScreenOn = true
            },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val videoState by videoEngine.videoState.collectAsStateWithLifecycle()

    // Controls visibility and auto-hide
    var showControls by remember { mutableStateOf(true) }
    var userInteracting by remember { mutableStateOf(false) }

    // Gesture State: HUD indicators
    var hudBrightnessVisible by remember { mutableStateOf(false) }
    var hudVolumeVisible by remember { mutableStateOf(false) }
    var hudSeekVisible by remember { mutableStateOf(false) }
    var hudSeekDeltaMs by remember { mutableLongStateOf(0L) }
    var hudSeekTargetMs by remember { mutableLongStateOf(0L) }

    var activeGestureDirection by remember { mutableStateOf<String?>(null) }
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var dragStartPosMs by remember { mutableLongStateOf(0L) }

    var seekBubbleText by remember { mutableStateOf<String?>(null) }

    // Pinch-to-zoom state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Options Dialog
    var showOptionsDialog by remember { mutableStateOf(false) }

    // Auto-hide controls timer (3.5 seconds)
    LaunchedEffect(showControls, videoState.isPlaying, videoState.isLocked, userInteracting) {
        if (showControls && videoState.isPlaying && !videoState.isLocked && !userInteracting) {
            delay(3500)
            showControls = false
        }
    }

    // Hide HUDs after gestures
    LaunchedEffect(hudBrightnessVisible) {
        if (hudBrightnessVisible) {
            delay(1500)
            hudBrightnessVisible = false
        }
    }
    LaunchedEffect(hudVolumeVisible) {
        if (hudVolumeVisible) {
            delay(1500)
            hudVolumeVisible = false
        }
    }
    LaunchedEffect(hudSeekVisible, userInteracting) {
        if (hudSeekVisible && !userInteracting) {
            delay(1200)
            hudSeekVisible = false
        }
    }
    LaunchedEffect(seekBubbleText) {
        if (seekBubbleText != null) {
            delay(900)
            seekBubbleText = null
        }
    }

    // Calculate progress and time formatting
    val currentMs = videoState.currentPositionMs.coerceAtLeast(0L)
    val durationMs = if (videoState.durationMs > 0L) videoState.durationMs else (video?.durationMs ?: 300000L)
    val progress = if (durationMs > 0L) (currentMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    val cMin = currentMs / 60000
    val cSec = (currentMs % 60000) / 1000
    val dMin = durationMs / 60000
    val dSec = (durationMs % 60000) / 1000
    val currentTimeString = String.format("%02d:%02d", cMin, cSec)
    val durationTimeString = String.format("%02d:%02d", dMin, dSec)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("full_screen_video_player")
    ) {
        // Base Layer: Real Media3 PlayerView Video Surface (completely unblocked!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomScale,
                    scaleY = zoomScale,
                    translationX = panOffsetX,
                    translationY = panOffsetY
                )
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = videoEngine.player
                        useController = false
                        keepScreenOn = true
                        resizeMode = videoState.aspectRatioMode.resizeMode
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                update = { view ->
                    view.player = videoEngine.player
                    view.keepScreenOn = true
                    view.resizeMode = videoState.aspectRatioMode.resizeMode
                },
                onRelease = { view ->
                    try {
                        view.player = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Gesture Overlay Layer: Handles Tap, Double-Tap, Vertical Brightness & Volume Drag, Pinch-to-zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(videoState.isLocked) {
                    if (videoState.isLocked) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(1.0f, 3.5f)
                        if (zoomScale > 1.0f) {
                            panOffsetX += pan.x
                            panOffsetY += pan.y
                        } else {
                            panOffsetX = 0f
                            panOffsetY = 0f
                        }
                    }
                }
                .pointerInput(videoState.isLocked) {
                    if (videoState.isLocked) {
                        detectTapGestures {
                            showControls = !showControls
                        }
                        return@pointerInput
                    }
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val isLeft = offset.x < size.width * 0.45f
                            val isRight = offset.x > size.width * 0.55f
                            if (isLeft) {
                                videoEngine.seekRelative(-10000L)
                                seekBubbleText = "-10s"
                            } else if (isRight) {
                                videoEngine.seekRelative(10000L)
                                seekBubbleText = "+10s"
                            } else {
                                zoomScale = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
                .pointerInput(videoState.isLocked, durationMs) {
                    if (videoState.isLocked) return@pointerInput
                    detectDragGestures(
                        onDragStart = {
                            userInteracting = true
                            activeGestureDirection = null
                            totalDragX = 0f
                            totalDragY = 0f
                            dragStartPosMs = videoState.currentPositionMs
                        },
                        onDragEnd = {
                            userInteracting = false
                            if (activeGestureDirection == "SEEK" && hudSeekVisible) {
                                videoEngine.seekTo(hudSeekTargetMs)
                            }
                            activeGestureDirection = null
                        },
                        onDragCancel = {
                            userInteracting = false
                            activeGestureDirection = null
                        },
                        onDrag = { change, dragAmount ->
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            if (activeGestureDirection == null) {
                                val absX = kotlin.math.abs(totalDragX)
                                val absY = kotlin.math.abs(totalDragY)
                                val threshold = 12f
                                if (absX > threshold || absY > threshold) {
                                    if (absX > absY) {
                                        activeGestureDirection = "SEEK"
                                    } else {
                                        val isLeft = change.position.x < size.width / 2f
                                        activeGestureDirection = if (isLeft) "BRIGHTNESS" else "VOLUME"
                                    }
                                }
                            }

                            when (activeGestureDirection) {
                                "BRIGHTNESS" -> {
                                    val deltaPercent = -dragAmount.y / (size.height * 0.75f)
                                    activity?.let { act -> videoEngine.adjustBrightnessDelta(act, deltaPercent) }
                                    hudBrightnessVisible = true
                                    hudVolumeVisible = false
                                    hudSeekVisible = false
                                }
                                "VOLUME" -> {
                                    val deltaPercent = -dragAmount.y / (size.height * 0.75f)
                                    videoEngine.adjustVolumeDelta(deltaPercent)
                                    hudVolumeVisible = true
                                    hudBrightnessVisible = false
                                    hudSeekVisible = false
                                }
                                "SEEK" -> {
                                    val deltaSec = (totalDragX / size.width) * 90f
                                    val deltaMs = (deltaSec * 1000).toLong()
                                    val target = (dragStartPosMs + deltaMs).coerceIn(0L, durationMs)
                                    hudSeekDeltaMs = deltaMs
                                    hudSeekTargetMs = target
                                    hudSeekVisible = true
                                    hudBrightnessVisible = false
                                    hudVolumeVisible = false
                                }
                            }
                        }
                    )
                }
        )

        // GESTURE HUD INDICATORS
        // Brightness HUD Bar on Left
        AnimatedVisibility(
            visible = hudBrightnessVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp))
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(videoState.brightnessPercent)
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFFFACC15))
                        )
                    }
                    Text(
                        text = "${(videoState.brightnessPercent * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Volume HUD Bar on Right
        AnimatedVisibility(
            visible = hudVolumeVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(18.dp))
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(videoState.volumePercent)
                                .align(Alignment.BottomCenter)
                                .background(colors.accentGlow)
                        )
                    }
                    Text(
                        text = "${(videoState.volumePercent * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Seek Scrub HUD Indicator (Center Floating Glass Bubble)
        AnimatedVisibility(
            visible = hudSeekVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            val sMin = hudSeekTargetMs / 60000
            val sSec = (hudSeekTargetMs % 60000) / 1000
            val sign = if (hudSeekDeltaMs >= 0) "+" else ""
            val deltaSec = hudSeekDeltaMs / 1000

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .border(1.dp, colors.accentGlow.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%02d:%02d", sMin, sSec),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "[$sign${deltaSec}s]",
                        color = colors.accentGlow,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Double-tap Seek Bubble Ripple (+10s / -10s)
        AnimatedVisibility(
            visible = seekBubbleText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seekBubbleText ?: "",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // LOCKED FLOATING UNLOCK BUTTON
        if (videoState.isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).statusBarsPadding()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.2.dp, Color(0xFFFACC15), RoundedCornerShape(22.dp))
                        .clickable { videoEngine.setScreenLock(false) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFACC15), modifier = Modifier.size(16.dp))
                        Text("Screen Locked • Tap to Unlock", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // DELICATE SUBTITLES OVERLAY (Non-obstructive at bottom)
        subtitleManager?.let { sm ->
            val settings by sm.settings.collectAsStateWithLifecycle()
            val upperCue = sm.getCurrentUpperCue(videoState.currentPositionMs)
            val lowerCue = sm.getCurrentLowerCue(videoState.currentPositionMs)

            if ((upperCue != null || lowerCue != null) && videoState.currentSubtitleTrackIndex > 0) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showControls && !videoState.isLocked) 80.dp else 24.dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    upperCue?.let { text ->
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    lowerCue?.let { text ->
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // FLOATING CRYSTAL LIQUID GLASS CONTROLS (Exactly cloned from Image 2)
        if (!videoState.isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // TOP BAR: Left Title & Right Floating Glass Capsule [Image 2 Top]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Left: Back Arrow + Video Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.4f))
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = video?.title ?: "Beau paysage",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Top Right: Floating Glass Capsule [Replay/Aspect | CC | More ⋮]
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .border(
                                    0.8.dp,
                                    Brush.horizontalGradient(listOf(Color(0x60FFFFFF), Color(0x20FFFFFF))),
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Replay / Loop toggle
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    tint = if (videoState.isLooping) colors.accentGlow else Color.White,
                                    modifier = Modifier
                                        .size(17.dp)
                                        .clickable {
                                            videoEngine.toggleLoop()
                                            seekBubbleText = if (!videoState.isLooping) "Loop: ON" else "Loop: OFF"
                                        }
                                )

                                // CC (Subtitles) toggle
                                Box(
                                    modifier = Modifier.clickable {
                                        val nextIdx = if (videoState.currentSubtitleTrackIndex > 0) 0 else 1
                                        videoEngine.selectSubtitleTrack(nextIdx)
                                        seekBubbleText = if (nextIdx > 0) "Subtitles: ON" else "Subtitles: OFF"
                                    }
                                ) {
                                    Text(
                                        text = "CC",
                                        color = if (videoState.currentSubtitleTrackIndex > 0) colors.accentGlow else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // More Options ⋮
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(17.dp)
                                        .clickable { showOptionsDialog = true }
                                )
                            }
                        }
                    }

                    // CENTER FLOATING CRYSTAL LIQUID GLASS MEDIA CONTROLS [Image 2 Center]
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(
                                1.dp,
                                Brush.horizontalGradient(listOf(Color(0x60FFFFFF), Color(0x20FFFFFF))),
                                RoundedCornerShape(32.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(22.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous (|◀)
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { videoEngine.seekRelative(-10000L) }
                            )

                            // Play / Pause (▶ / ❚❚)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    .clickable { videoEngine.togglePlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (videoState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (videoState.isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // Next (▶|)
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { videoEngine.seekRelative(10000L) }
                            )
                        }
                    }

                    // BOTTOM CONTROLS: Progress Bar + Floating Pills [Image 2 Bottom]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section 1: Thin Glowing Progress Timeline Scrubber
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentTimeString,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.width(36.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Interactive Timeline Slider Canvas
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(20.dp)
                                    .pointerInput(durationMs) {
                                        detectTapGestures { offset ->
                                            if (size.width > 0f) {
                                                val factor = (offset.x / size.width).coerceIn(0f, 1f)
                                                videoEngine.seekTo((factor * durationMs).toLong())
                                            }
                                        }
                                    }
                                    .pointerInput(durationMs) {
                                        detectDragGestures { change, _ ->
                                            if (size.width > 0f) {
                                                val factor = (change.position.x / size.width).coerceIn(0f, 1f)
                                                videoEngine.seekTo((factor * durationMs).toLong())
                                            }
                                        }
                                    }
                                    .testTag("video_timeline_slider")
                            ) {
                                if (size.width <= 0f || size.height <= 0f) return@Canvas

                                val cy = size.height / 2
                                val trackH = 3.dp.toPx()

                                // Background track
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.25f),
                                    topLeft = Offset(0f, cy - trackH / 2),
                                    size = Size(size.width, trackH),
                                    cornerRadius = CornerRadius(trackH / 2, trackH / 2)
                                )

                                // Active progress track
                                val activeWidth = size.width * progress
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(0f, cy - trackH / 2),
                                    size = Size(activeWidth, trackH),
                                    cornerRadius = CornerRadius(trackH / 2, trackH / 2)
                                )

                                // Glowing White Thumb Circle
                                drawCircle(
                                    color = Color.White,
                                    radius = 5.5.dp.toPx(),
                                    center = Offset(activeWidth, cy)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = durationTimeString,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.width(36.dp)
                            )
                        }

                        // Section 2: Bottom-Left Pill & Bottom-Right Pill [Image 2 Dual Floating Capsules]
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Bottom-Left Floating Capsule [Aspect Ratio / Fit | GIF]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(
                                        0.8.dp,
                                        Brush.horizontalGradient(listOf(Color(0x60FFFFFF), Color(0x20FFFFFF))),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Aspect Ratio / Fit mode button
                                    Icon(
                                        imageVector = Icons.Default.CropFree,
                                        contentDescription = "Aspect Ratio",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable {
                                                val nextMode = when (videoState.aspectRatioMode) {
                                                    VideoAspectRatioMode.FIT -> VideoAspectRatioMode.FILL
                                                    VideoAspectRatioMode.FILL -> VideoAspectRatioMode.ZOOM
                                                    VideoAspectRatioMode.ZOOM -> VideoAspectRatioMode.FIXED_4_3
                                                    VideoAspectRatioMode.FIXED_4_3 -> VideoAspectRatioMode.FIT
                                                    else -> VideoAspectRatioMode.FIT
                                                }
                                                videoEngine.setAspectRatio(nextMode)
                                                seekBubbleText = "Aspect: ${nextMode.label}"
                                            }
                                    )

                                    // GIF / Capture icon
                                    Icon(
                                        imageVector = Icons.Default.Gif,
                                        contentDescription = "GIF",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                seekBubbleText = "📸 Snapshot Saved to Gallery"
                                            }
                                    )
                                }
                            }

                            // Bottom-Right Floating Capsule [Resize/PiP | Speed | Rotate | Lock]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(
                                        0.8.dp,
                                        Brush.horizontalGradient(listOf(Color(0x60FFFFFF), Color(0x20FFFFFF))),
                                        RoundedCornerShape(20.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Picture in Picture
                                    Icon(
                                        imageVector = Icons.Default.PictureInPictureAlt,
                                        contentDescription = "PiP",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                seekBubbleText = "Entering PiP Mode"
                                                onEnterPip()
                                            }
                                    )

                                    // Fullscreen / Fit toggle
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Fullscreen",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable {
                                                val nextMode = if (videoState.aspectRatioMode == VideoAspectRatioMode.FILL) VideoAspectRatioMode.FIT else VideoAspectRatioMode.FILL
                                                videoEngine.setAspectRatio(nextMode)
                                                seekBubbleText = "Aspect: ${nextMode.label}"
                                            }
                                    )

                                    // Speed (1.0x)
                                    Text(
                                        text = "${videoState.playbackSpeed}x",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable {
                                            val nextSpeed = when (videoState.playbackSpeed) {
                                                1.0f -> 1.25f
                                                1.25f -> 1.5f
                                                1.5f -> 2.0f
                                                2.0f -> 0.5f
                                                0.5f -> 0.75f
                                                else -> 1.0f
                                            }
                                            videoEngine.setPlaybackSpeed(nextSpeed)
                                            seekBubbleText = "Speed: ${nextSpeed}x"
                                        }
                                    )

                                    // Screen Rotate toggle
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = "Rotate",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                activity?.let { act ->
                                                    val newOrient = if (act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                                                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                                    } else {
                                                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                                                    }
                                                    act.requestedOrientation = newOrient
                                                    seekBubbleText = if (newOrient == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) "Portrait" else "Landscape"
                                                }
                                            }
                                    )

                                    // Lock Touch Screen
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Lock Screen",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                videoEngine.setScreenLock(true)
                                                seekBubbleText = "Controls Locked"
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FLOATING OPTIONS DIALOG (When More ⋮ is clicked)
        if (showOptionsDialog) {
            VideoOptionsCrystalDialog(
                videoState = videoState,
                onDismiss = { showOptionsDialog = false },
                onSelectSpeed = { s -> videoEngine.setPlaybackSpeed(s) },
                onSelectSubtitle = { idx -> videoEngine.selectSubtitleTrack(idx) },
                onNavigateToSubtitleManager = {
                    showOptionsDialog = false
                    onNavigate(Screen.SubtitleManager)
                },
                onNavigateToEqualizer = {
                    showOptionsDialog = false
                    onNavigate(Screen.EqStudio)
                }
            )
        }
    }
}

@Composable
private fun VideoOptionsCrystalDialog(
    videoState: com.example.media.video.VideoPlaybackState,
    onDismiss: () -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSelectSubtitle: (Int) -> Unit,
    onNavigateToSubtitleManager: () -> Unit,
    onNavigateToEqualizer: () -> Unit
) {
    val colors = LocalMatrixColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xE6141824))
                .border(
                    1.dp,
                    Brush.linearGradient(listOf(Color(0x60FFFFFF), colors.accentGlow.copy(alpha = 0.4f))),
                    RoundedCornerShape(24.dp)
                )
                .clickable(enabled = false) { }
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Video Settings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }

                // Playback Speed Options
                Column {
                    Text("Playback Speed", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.accentGlow)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            val isSel = videoState.playbackSpeed == spd
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) colors.accentGlow else Color.White.copy(alpha = 0.1f))
                                    .clickable { onSelectSpeed(spd) }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${spd}x",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                // Subtitle Quick Toggle
                Column {
                    Text("Subtitles", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.accentGlow)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (videoState.currentSubtitleTrackIndex == 0) colors.accentGlow else Color.White.copy(alpha = 0.1f))
                                .clickable { onSelectSubtitle(0) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Disabled", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (videoState.currentSubtitleTrackIndex == 0) Color.Black else Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (videoState.currentSubtitleTrackIndex > 0) colors.accentGlow else Color.White.copy(alpha = 0.1f))
                                .clickable { onSelectSubtitle(1) }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Enabled (CC)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (videoState.currentSubtitleTrackIndex > 0) Color.Black else Color.White)
                        }
                    }
                }

                // External Studio Shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.6.dp, colors.glassBorder, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToSubtitleManager() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Subtitle Studio", fontSize = 10.5.sp, color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(0.6.dp, colors.glassBorder, RoundedCornerShape(10.dp))
                            .clickable { onNavigateToEqualizer() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFFB026FF), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EQ Studio", fontSize = 10.5.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
