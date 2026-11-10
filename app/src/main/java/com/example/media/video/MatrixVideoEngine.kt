package com.example.media.video

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.view.Window
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VideoAspectRatioMode(val label: String, val resizeMode: Int) {
    FIT("Fit (16:9)", 0),
    FILL("Fill Screen", 3),
    ZOOM("Zoomed", 4),
    FIXED_4_3("4:3 Standard", 0),
    FIXED_21_9("21:9 Cinema", 0)
}

data class VideoPlaybackState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatioMode: VideoAspectRatioMode = VideoAspectRatioMode.FIT,
    val isLocked: Boolean = false,
    val isLooping: Boolean = false,
    val volumePercent: Float = 0.75f,
    val brightnessPercent: Float = 0.7f,
    val currentAudioTrackIndex: Int = 0,
    val availableAudioTracks: List<String> = listOf("Track 1: Original Audio (Stereo)", "Track 2: Director's Commentary"),
    val currentSubtitleTrackIndex: Int = 1,
    val availableSubtitleTracks: List<String> = listOf("Disabled", "English (CC) [Universal UTF-8]", "Bengali (বাংলা) UTF-8", "Spanish (Español)"),
    val audioDelayMs: Long = 0L,
    val subtitleDelayMs: Long = 0L,
    val hardwareDecoderActive: Boolean = true
)

@OptIn(UnstableApi::class)
class MatrixVideoEngine(private val context: Context) {

    val trackSelector = DefaultTrackSelector(context)
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .setTrackSelector(trackSelector)
        .setWakeMode(C.WAKE_MODE_NONE)
        .build()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _videoState = MutableStateFlow(VideoPlaybackState())
    val videoState: StateFlow<VideoPlaybackState> = _videoState.asStateFlow()

    init {
        // Read initial volume
        val maxVol = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        val curVol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
        _videoState.value = _videoState.value.copy(
            volumePercent = (curVol.toFloat() / maxVol.toFloat()).coerceIn(0f, 1f)
        )

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _videoState.value = _videoState.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val dur = if (player.duration > 0) player.duration else 0L
                _videoState.value = _videoState.value.copy(
                    durationMs = dur,
                    currentPositionMs = player.currentPosition
                )
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _videoState.value = _videoState.value.copy(
                    currentPositionMs = player.currentPosition
                )
            }
        })
    }

    fun prepareAndPlay(uriString: String, title: String) {
        try {
            val safeUri = when {
                uriString.isBlank() -> "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
                uriString.startsWith("content://") || uriString.startsWith("file://") || uriString.startsWith("http://") || uriString.startsWith("https://") -> uriString
                uriString.startsWith("/") -> "file://$uriString"
                else -> uriString
            }

            val mediaItemBuilder = MediaItem.Builder()
                .setUri(safeUri)
                .setMediaId(if (title.isNotBlank()) title else "matrix_video_${System.currentTimeMillis()}")

            if (safeUri.contains(".m3u8", ignoreCase = true) || safeUri.contains("m3u8", ignoreCase = true) || safeUri.contains("/hls", ignoreCase = true)) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
            } else if (safeUri.contains(".mpd", ignoreCase = true)) {
                mediaItemBuilder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_MPD)
            }

            val mediaItem = mediaItemBuilder.build()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _videoState.value = _videoState.value.copy(
                isPlaying = true,
                currentPositionMs = 0L
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))
        _videoState.value = _videoState.value.copy(currentPositionMs = player.currentPosition)
    }

    fun seekRelative(deltaMs: Long) {
        val newPos = (player.currentPosition + deltaMs).coerceIn(0L, player.duration.coerceAtLeast(0L))
        seekTo(newPos)
    }

    fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
        _videoState.value = _videoState.value.copy(playbackSpeed = speed)
    }

    fun setAspectRatio(mode: VideoAspectRatioMode) {
        _videoState.value = _videoState.value.copy(aspectRatioMode = mode)
    }

    fun setScreenLock(locked: Boolean) {
        _videoState.value = _videoState.value.copy(isLocked = locked)
    }

    fun adjustVolumeDelta(deltaPercent: Float) {
        val newVol = (_videoState.value.volumePercent + deltaPercent).coerceIn(0f, 1f)
        _videoState.value = _videoState.value.copy(volumePercent = newVol)

        audioManager?.let { am ->
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (newVol * max).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
        }
    }

    fun adjustBrightnessDelta(activity: Activity?, deltaPercent: Float) {
        val newBright = (_videoState.value.brightnessPercent + deltaPercent).coerceIn(0.05f, 1f)
        _videoState.value = _videoState.value.copy(brightnessPercent = newBright)

        activity?.window?.let { window ->
            val lp = window.attributes
            lp.screenBrightness = newBright
            window.attributes = lp
        }
    }

    fun toggleLoop() {
        val nextLoop = !_videoState.value.isLooping
        player.repeatMode = if (nextLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
        _videoState.value = _videoState.value.copy(isLooping = nextLoop)
    }

    fun selectAudioTrack(trackIndex: Int) {
        _videoState.value = _videoState.value.copy(currentAudioTrackIndex = trackIndex)
    }

    fun selectSubtitleTrack(trackIndex: Int) {
        _videoState.value = _videoState.value.copy(currentSubtitleTrackIndex = trackIndex)
    }

    fun adjustAudioDelay(deltaMs: Long) {
        _videoState.value = _videoState.value.copy(audioDelayMs = _videoState.value.audioDelayMs + deltaMs)
    }

    fun adjustSubtitleDelay(deltaMs: Long) {
        _videoState.value = _videoState.value.copy(subtitleDelayMs = _videoState.value.subtitleDelayMs + deltaMs)
    }

    fun release() {
        player.release()
    }
}
