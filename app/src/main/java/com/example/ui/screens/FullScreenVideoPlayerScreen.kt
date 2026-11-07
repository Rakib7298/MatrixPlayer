package com.example.ui.screens

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.RefBlueCyanGlow
import com.example.ui.theme.RefPurpleLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Super Crystal Liquid Glass Full-Screen Video Player with 70% transparent rounded corner control panels,
 * real gestures (vertical drag left for brightness, vertical drag right for volume, double-tap 10s seek,
 * two-finger pinch-to-zoom 1x-3x), aspect ratio cycling, audio & subtitle track chooser modal, and screen lock.
 *
 * Visually cloned from reference screenshots:
 * - 5eb155e2: Landscape video player with floating capsules
 * - Screenshot_2026-11-07-00-12-18-887: Vision glass frosted player controls
 * - b35ecc5e & 1cb5f32d: Audio and Subtitle chooser modal dialog
 */
@OptIn(UnstableApi::class)
@Composable
fun FullScreenVideoPlayerScreen(
    video: VideoItem?,
    videoEngine: MatrixVideoEngine,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    subtitleManager: com.example.media.subtitle.MatrixSubtitleManager? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    val videoState by videoEngine.videoState.collectAsStateWithLifecycle()

    // Controls visibility and auto-hide
    var showControls by remember { mutableStateOf(true) }
    var userInteracting by remember { mutableStateOf(false) }

    // Gesture State: HUD indicators & Drag directions
    var hudBrightnessVisible by remember { mutableStateOf(false) }
    var hudVolumeVisible by remember { mutableStateOf(false) }
    var hudSeekVisible by remember { mutableStateOf(false) }
    var hudSeekDeltaMs by remember { mutableLongStateOf(0L) }
    var hudSeekTargetMs by remember { mutableLongStateOf(0L) }

    var activeGestureDirection by remember { mutableStateOf<String?>(null) } // "SEEK", "BRIGHTNESS", "VOLUME"
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var dragStartPosMs by remember { mutableLongStateOf(0L) }

    var seekBubbleText by remember { mutableStateOf<String?>(null) }
    var seekBubbleForward by remember { mutableStateOf(true) }

    // Pinch-to-zoom state
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Audio & Subtitle chooser modal
    var showTrackChooserDialog by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }

    // Auto-hide controls timer
    LaunchedEffect(showControls, videoState.isPlaying, videoState.isLocked, userInteracting) {
        if (showControls && videoState.isPlaying && !videoState.isLocked && !userInteracting) {
            delay(5000)
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
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Base Layer: ExoPlayer Video Surface with pinch-to-zoom graphicsLayer
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
            // Real Media3 PlayerView
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
                modifier = Modifier.fillMaxSize()
            )

            // Scenic 4K Alpine Nature Backdrop ("Beau paysage" fallback canvas if video is buffering or local file not yet loaded)
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val w = size.width
                val h = size.height

                // Golden sunset sky
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFFFED7AA), Color(0xFFF97316)),
                        startY = 0f,
                        endY = h * 0.45f
                    )
                )

                // Mountain Peaks silhouette
                val mountainPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.4f)
                    lineTo(w * 0.2f, h * 0.22f)
                    lineTo(w * 0.35f, h * 0.35f)
                    lineTo(w * 0.5f, h * 0.18f)
                    lineTo(w * 0.68f, h * 0.32f)
                    lineTo(w * 0.85f, h * 0.25f)
                    lineTo(w, h * 0.38f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(mountainPath, Color(0xFF334155))

                // Alpine Pine Tree Layer
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF166534), Color(0xFF14532D)),
                        startY = h * 0.38f,
                        endY = h * 0.6f
                    ),
                    topLeft = Offset(0f, h * 0.38f),
                    size = Size(w, h * 0.22f)
                )

                // Crystal Lake Water Reflection with subtle ripples
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0F766E), Color(0xFF115E59), Color(0xFF042F2E)),
                        startY = h * 0.6f,
                        endY = h
                    ),
                    topLeft = Offset(0f, h * 0.6f),
                    size = Size(w, h * 0.4f)
                )
            }
        }

        // Gesture Overlay Layer: Handles Tap, Double-Tap, Vertical Brightness & Volume Drag, Pinch-to-zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(videoState.isLocked) {
                    if (videoState.isLocked) return@pointerInput
                    detectTransformGestures { _, pan, zoom, _ ->
                        // Pinch to zoom
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
                            // When locked, tapping shows the unlock button briefly
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
                                seekBubbleForward = false
                            } else if (isRight) {
                                videoEngine.seekRelative(10000L)
                                seekBubbleText = "+10s"
                                seekBubbleForward = true
                            } else {
                                // Double tap center resets zoom
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
                        onDragStart = { offset ->
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
                            if (activeGestureDirection == "SEEK" && hudSeekVisible) {
                                videoEngine.seekTo(hudSeekTargetMs)
                            }
                            activeGestureDirection = null
                        },
                        onDrag = { change, dragAmount ->
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            if (activeGestureDirection == null) {
                                val absX = kotlin.math.abs(totalDragX)
                                val absY = kotlin.math.abs(totalDragY)
                                val threshold = 10f

                                if (absX > threshold || absY > threshold) {
                                    if (absX > absY) {
                                        activeGestureDirection = "SEEK"
                                    } else {
                                        val touchX = change.position.x
                                        val screenMid = size.width / 2f
                                        activeGestureDirection = if (touchX < screenMid) "BRIGHTNESS" else "VOLUME"
                                    }
                                }
                            }

                            when (activeGestureDirection) {
                                "SEEK" -> {
                                    val fullWidthMs = 120_000L // 2 minutes seeking window across full screen
                                    val deltaMs = (totalDragX / size.width.toFloat() * fullWidthMs).toLong()
                                    val targetMs = (dragStartPosMs + deltaMs).coerceIn(0L, durationMs.coerceAtLeast(1000L))
                                    hudSeekTargetMs = targetMs
                                    hudSeekDeltaMs = targetMs - dragStartPosMs
                                    hudSeekVisible = true
                                }
                                "BRIGHTNESS" -> {
                                    val delta = -dragAmount.y / 300f
                                    videoEngine.adjustBrightnessDelta(activity, delta)
                                    hudBrightnessVisible = true
                                }
                                "VOLUME" -> {
                                    val delta = -dragAmount.y / 300f
                                    videoEngine.adjustVolumeDelta(delta)
                                    hudVolumeVisible = true
                                }
                            }
                        }
                    )
                }
        )

        // HUD OVERLAYS: Double-tap seek bubble, Brightness HUD, Volume HUD
        // Double-tap Seek Bubble
        seekBubbleText?.let { text ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp),
                contentAlignment = if (seekBubbleForward) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xB311141E))
                        .border(1.5.dp, Brush.radialGradient(listOf(Color(0x8000F0FF), Color(0x33B026FF))), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (seekBubbleForward) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = RefBlueCyanGlow,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = text,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Center HUD: Horizontal Drag Seek Capsule
        AnimatedVisibility(
            visible = hudSeekVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD90B101D))
                    .border(1.5.dp, Brush.horizontalGradient(listOf(RefBlueCyanGlow, RefPurpleLight)), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (hudSeekDeltaMs >= 0) Icons.Default.FastForward else Icons.Default.FastRewind,
                            contentDescription = null,
                            tint = RefBlueCyanGlow,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "${if (hudSeekDeltaMs >= 0) "+" else ""}${hudSeekDeltaMs / 1000}s",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    val targetMin = hudSeekTargetMs / 60000
                    val targetSec = (hudSeekTargetMs % 60000) / 1000
                    val targetStr = String.format("%02d:%02d", targetMin, targetSec)

                    Text(
                        text = "$targetStr / $durationTimeString",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Target progress bar indicator
                    val targetRatio = if (durationMs > 0) (hudSeekTargetMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
                    Box(
                        modifier = Modifier
                            .width(160.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0x40FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(targetRatio)
                                .background(Brush.horizontalGradient(listOf(RefBlueCyanGlow, RefPurpleLight)))
                        )
                    }
                }
            }
        }

        // Left HUD: Screen Brightness Vertical Capsule
        AnimatedVisibility(
            visible = hudBrightnessVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xB30B101D))
                    .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(26.dp))
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness",
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(22.dp)
                    )

                    // Vertical Progress Bar
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x40FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(videoState.brightnessPercent)
                                .align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))))
                        )
                    }

                    Text(
                        text = "${(videoState.brightnessPercent * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Right HUD: Audio Volume Vertical Capsule
        AnimatedVisibility(
            visible = hudVolumeVisible,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(170.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xB30B101D))
                    .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(26.dp))
                    .padding(vertical = 14.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Volume",
                        tint = RefBlueCyanGlow,
                        modifier = Modifier.size(22.dp)
                    )

                    // Vertical Progress Bar
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(80.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0x40FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(videoState.volumePercent)
                                .align(Alignment.BottomCenter)
                                .background(Brush.verticalGradient(listOf(RefBlueCyanGlow, RefPurpleLight)))
                        )
                    }

                    Text(
                        text = "${(videoState.volumePercent * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // SCREEN LOCKED FLOATING UNLOCK BUTTON
        if (videoState.isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd).padding(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xB30B101D))
                        .border(1.5.dp, Color(0xFFFACC15), RoundedCornerShape(24.dp))
                        .clickable { videoEngine.setScreenLock(false) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Locked", tint = Color(0xFFFACC15), modifier = Modifier.size(20.dp))
                    Text("Screen Locked • Tap to Unlock", color = Color(0xFFFACC15), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // LIVE DUAL SUBTITLES OVERLAY (Synchronized with video position ms)
        subtitleManager?.let { sm ->
            val settings by sm.settings.collectAsStateWithLifecycle()
            val upperCue = sm.getCurrentUpperCue(videoState.currentPositionMs)
            val lowerCue = sm.getCurrentLowerCue(videoState.currentPositionMs)

            if (upperCue != null || lowerCue != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = if (showControls && !videoState.isLocked) 130.dp else 40.dp)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    upperCue?.let { text ->
                        Text(
                            text = text,
                            fontSize = settings.upperFontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = settings.upperColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = settings.backgroundOpacity))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    lowerCue?.let { text ->
                        Text(
                            text = text,
                            fontSize = settings.lowerFontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = settings.lowerColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = settings.backgroundOpacity))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // FULL 70% TRANSPARENT SUPER CRYSTAL LIQUID GLASS CONTROL PANELS
        if (!videoState.isLocked) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    // TOP BAR: Title & Floating Glass Action Pill Capsules
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Capsule: Back Button + Title + 4K Tag
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xB30B101D)) // 70% transparent crystal glass
                                .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { onBack() }
                            )

                            Text(
                                text = video?.title ?: "Beau paysage (The Studio)",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = Color.White
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0x3300F0FF))
                                    .border(0.5.dp, RefBlueCyanGlow.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "4K HDR",
                                    color = RefBlueCyanGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Right Capsule: [Loop/Replay] [PiP] [Audio & CC] [More Settings]
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xB30B101D)) // 70% transparent crystal glass
                                .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Loop Toggle
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (videoState.isLooping) RefBlueCyanGlow else Color.White,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable { videoEngine.toggleLoop() }
                            )

                            // PiP Mode
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture-in-Picture",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable {
                                        try {
                                            activity?.enterPictureInPictureMode()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                            )

                            // Subtitles & Audio Chooser (UI references b35ecc5e & 1cb5f32d)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (videoState.currentSubtitleTrackIndex > 0) Color(0x4000F0FF) else Color.Transparent)
                                    .clickable { showTrackChooserDialog = true }
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Subtitles,
                                    contentDescription = "Subtitles",
                                    tint = if (videoState.currentSubtitleTrackIndex > 0) RefBlueCyanGlow else Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Text(
                                    text = "CC",
                                    color = if (videoState.currentSubtitleTrackIndex > 0) RefBlueCyanGlow else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Quick Equalizer link
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "EQ",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable { onNavigate(Screen.EqStudio) }
                            )

                            // More options
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(19.dp)
                                    .clickable { onNavigate(Screen.SubtitleManager) }
                            )
                        }
                    }

                    // CENTER FLOATING CRYSTAL CONTROLS: [-10s] [ Play/Pause ] [+10s]
                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Track / Fast Rewind 10s
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xB311141E))
                                .border(1.dp, Color(0x55FFFFFF), CircleShape)
                                .clickable { videoEngine.seekRelative(-10000L) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "Rewind 10s",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        // Main Glowing Circular Glass Play/Pause Button
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xCC1A1F2E), Color(0xB30B101D))
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    Brush.linearGradient(
                                        listOf(RefBlueCyanGlow, RefPurpleLight, Color(0x80FFFFFF))
                                    ),
                                    CircleShape
                                )
                                .clickable { videoEngine.togglePlayPause() }
                                .testTag("video_play_pause_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (videoState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (videoState.isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        // Next Track / Fast Forward 10s
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xB311141E))
                                .border(1.dp, Color(0x55FFFFFF), CircleShape)
                                .clickable { videoEngine.seekRelative(10000L) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = "Forward 10s",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // BOTTOM 70% TRANSPARENT SUPER CRYSTAL LIQUID GLASS CONTROL PANEL
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xB30B101D)) // Exactly 70% transparent midnight crystal glass
                            .border(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0x60FFFFFF),
                                        Color(0x3300F0FF),
                                        Color(0x44B026FF),
                                        Color(0x60FFFFFF)
                                    )
                                ),
                                RoundedCornerShape(30.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Section 1: Timeline Scrubber & Timestamp Labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentTimeString,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.width(44.dp)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                // Interactive Glowing Timeline Track
                                Canvas(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .pointerInput(durationMs) {
                                            detectTapGestures { offset ->
                                                val factor = (offset.x / size.width).coerceIn(0f, 1f)
                                                videoEngine.seekTo((factor * durationMs).toLong())
                                            }
                                        }
                                        .pointerInput(durationMs) {
                                            detectDragGestures { change, _ ->
                                                val factor = (change.position.x / size.width).coerceIn(0f, 1f)
                                                videoEngine.seekTo((factor * durationMs).toLong())
                                            }
                                        }
                                        .testTag("video_timeline_slider")
                                ) {
                                    val cy = size.height / 2
                                    val trackHeight = 4.dp.toPx()

                                    // Background Track
                                    drawRoundRect(
                                        color = Color(0x4DFFFFFF),
                                        topLeft = Offset(0f, cy - trackHeight / 2),
                                        size = Size(size.width, trackHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                                    )

                                    // Active Track Gradient (Cyan to Purple)
                                    val activeWidth = size.width * progress
                                    drawRoundRect(
                                        brush = Brush.horizontalGradient(
                                            listOf(RefBlueCyanGlow, RefPurpleLight)
                                        ),
                                        topLeft = Offset(0f, cy - trackHeight / 2),
                                        size = Size(activeWidth, trackHeight),
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
                                    )

                                    // Glowing Thumb with double ring
                                    drawCircle(
                                        color = RefBlueCyanGlow.copy(alpha = 0.35f),
                                        radius = 11.dp.toPx(),
                                        center = Offset(activeWidth, cy)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 6.dp.toPx(),
                                        center = Offset(activeWidth, cy)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = durationTimeString,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFA1A1AA),
                                    modifier = Modifier.width(44.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Section 2: Floating Pill Capsules Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Pill Group: [Aspect Mode] [Zoom Status] [HW Accel]
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0x55181822))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Aspect Ratio Toggle
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                val nextMode = when (videoState.aspectRatioMode) {
                                                    VideoAspectRatioMode.FIT -> VideoAspectRatioMode.FILL
                                                    VideoAspectRatioMode.FILL -> VideoAspectRatioMode.ZOOM
                                                    VideoAspectRatioMode.ZOOM -> VideoAspectRatioMode.FIXED_4_3
                                                    VideoAspectRatioMode.FIXED_4_3 -> VideoAspectRatioMode.FIXED_21_9
                                                    VideoAspectRatioMode.FIXED_21_9 -> VideoAspectRatioMode.FIT
                                                }
                                                videoEngine.setAspectRatio(nextMode)
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CropFree,
                                            contentDescription = "Aspect Ratio",
                                            tint = RefBlueCyanGlow,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = videoState.aspectRatioMode.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Zoom percentage indicator if zoomed
                                    if (zoomScale > 1.05f) {
                                        Text(
                                            text = "${(zoomScale * 100).toInt()}% Zoom",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8),
                                            modifier = Modifier.clickable {
                                                zoomScale = 1.0f
                                                panOffsetX = 0f
                                                panOffsetY = 0f
                                            }
                                        )
                                    }

                                    // HW Decoder Tag
                                    Text(
                                        text = "HW+",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4ADE80)
                                    )
                                }

                                // Right Pill Group: [Playback Speed] [Audio/CC Dialog] [Screen Lock]
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(Color(0x55181822))
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(18.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Speed Pill: Click cycles through 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x, 3.0x
                                    Row(
                                        modifier = Modifier
                                            .clickable {
                                                val nextSpeed = when (videoState.playbackSpeed) {
                                                    0.5f -> 0.75f
                                                    0.75f -> 1.0f
                                                    1.0f -> 1.25f
                                                    1.25f -> 1.5f
                                                    1.5f -> 2.0f
                                                    2.0f -> 3.0f
                                                    3.0f -> 4.0f
                                                    else -> 0.5f
                                                }
                                                videoEngine.setPlaybackSpeed(nextSpeed)
                                            },
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "Speed",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "${videoState.playbackSpeed}x",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    // Audio Tracks quick button
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = "Audio Tracks",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable { showTrackChooserDialog = true }
                                    )

                                    // Screen Orientation toggle indicator
                                    Icon(
                                        imageVector = Icons.Default.ScreenRotation,
                                        contentDescription = "Rotate",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable { }
                                    )

                                    // Screen Lock button
                                    Icon(
                                        imageVector = if (videoState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                        contentDescription = "Lock",
                                        tint = if (videoState.isLocked) Color(0xFFFACC15) else Color.White,
                                        modifier = Modifier
                                            .size(17.dp)
                                            .clickable { videoEngine.setScreenLock(true) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TABBED AUDIO & SUBTITLE CHOOSER DIALOG (Cloned from Reference b35ecc5e & 1cb5f32d)
        if (showTrackChooserDialog) {
            AudioAndSubtitleChooserDialog(
                videoState = videoState,
                onDismiss = { showTrackChooserDialog = false },
                onSelectAudioTrack = { idx -> videoEngine.selectAudioTrack(idx) },
                onSelectSubtitleTrack = { idx -> videoEngine.selectSubtitleTrack(idx) },
                onAdjustAudioDelay = { delta -> videoEngine.adjustAudioDelay(delta) },
                onAdjustSubtitleDelay = { delta -> videoEngine.adjustSubtitleDelay(delta) }
            )
        }
    }
}

/**
 * Modern Super Crystal Liquid Glass Dialog matching Reference b35ecc5e:
 * "Choose subtitles and audio tracks" with side-by-side tabs and delay step controls.
 */
@Composable
fun AudioAndSubtitleChooserDialog(
    videoState: com.example.media.video.VideoPlaybackState,
    onDismiss: () -> Unit,
    onSelectAudioTrack: (Int) -> Unit,
    onSelectSubtitleTrack: (Int) -> Unit,
    onAdjustAudioDelay: (Long) -> Unit,
    onAdjustSubtitleDelay: (Long) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Audio, 1 = Subtitles
    var subtitleFontSize by remember { mutableIntStateOf(25) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.70f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(520.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xE6141824)) // Deep crystal glass panel
                .border(
                    1.5.dp,
                    Brush.linearGradient(listOf(Color(0x60FFFFFF), Color(0x3000F0FF), Color(0x40B026FF))),
                    RoundedCornerShape(28.dp)
                )
                .clickable(enabled = false) { }
                .padding(22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Subtitles & Audio Tracks",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        ),
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

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher: [Audio] | [Subtitles]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x400B101D))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 0) Color(0x6600F0FF) else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Audio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 1) Color(0x6600F0FF) else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("Subtitles", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // AUDIO TAB CONTENT
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Available Streams:", color = Color(0xFFA1A1AA), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        videoState.availableAudioTracks.forEachIndexed { index, trackName ->
                            val isSelected = videoState.currentAudioTrackIndex == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x4000F0FF) else Color(0x20FFFFFF))
                                    .clickable { onSelectAudioTrack(index) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trackName,
                                    color = if (isSelected) RefBlueCyanGlow else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = RefBlueCyanGlow, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Audio Delay Control
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Audio Delay:", color = Color.White, fontSize = 13.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { onAdjustAudioDelay(-50L) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("-50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${videoState.audioDelayMs} ms", color = RefBlueCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { onAdjustAudioDelay(50L) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("+50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    // SUBTITLES TAB CONTENT (Cloned from Reference b35ecc5e & 1cb5f32d)
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Available Subtitles:", color = Color(0xFFA1A1AA), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        videoState.availableSubtitleTracks.forEachIndexed { index, subName ->
                            val isSelected = videoState.currentSubtitleTrackIndex == index
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) Color(0x4000F0FF) else Color(0x20FFFFFF))
                                    .clickable { onSelectSubtitleTrack(index) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subName,
                                    color = if (isSelected) RefBlueCyanGlow else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = RefBlueCyanGlow, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle Delay Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subtitle Delay:", color = Color.White, fontSize = 13.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { onAdjustSubtitleDelay(-100L) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("-100ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${videoState.subtitleDelayMs} ms", color = RefBlueCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { onAdjustSubtitleDelay(100L) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("+100ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Subtitle Font Size Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Font Size: ${subtitleFontSize}sp", color = Color.White, fontSize = 13.sp)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { if (subtitleFontSize > 15) subtitleFontSize -= 2 }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("A-", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0x40FFFFFF))
                                        .clickable { if (subtitleFontSize < 40) subtitleFontSize += 2 }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("A+", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Done button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Brush.horizontalGradient(listOf(RefBlueCyanGlow, RefPurpleLight)))
                            .clickable { onDismiss() }
                            .padding(horizontal = 22.dp, vertical = 10.dp)
                    ) {
                        Text("Apply & Close", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
