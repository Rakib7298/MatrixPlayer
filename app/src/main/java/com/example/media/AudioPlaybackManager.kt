package com.example.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.TrackItem
import com.example.service.MatrixMediaPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlaybackEngineState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val currentMediaId: String? = null,
    val audioSessionId: Int = 0
)

@OptIn(UnstableApi::class)
class AudioPlaybackManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _engineState = MutableStateFlow(PlaybackEngineState())
    val engineState: StateFlow<PlaybackEngineState> = _engineState.asStateFlow()

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MatrixMediaPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                setupPlayerListener()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateState()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updateState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateState()
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateState()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateState()
            }
        })
        updateState()
    }

    fun updateState() {
        val player = mediaController ?: return
        val currentId = player.currentMediaItem?.mediaId
        val dur = if (player.duration > 0) player.duration else 0L
        val pos = if (player.currentPosition > 0) player.currentPosition else 0L
        val sessionId = player.sessionExtras.getInt("audio_session_id", 0)

        _engineState.value = _engineState.value.copy(
            isPlaying = player.isPlaying,
            currentPositionMs = pos,
            durationMs = dur,
            isShuffle = player.shuffleModeEnabled,
            repeatMode = player.repeatMode,
            currentMediaId = currentId,
            audioSessionId = sessionId
        )
    }

    fun playTrack(track: TrackItem) {
        val player = mediaController ?: return

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(track.title)
            .setArtist(track.artist)
            .setAlbumTitle(track.album)
            .build()

        val safeUri = if (track.uri.isNotBlank()) {
            Uri.parse(track.uri)
        } else {
            Uri.parse("https://storage.googleapis.com/exoplayer-test-media-0/Jazz_In_Paris.mp3")
        }

        val mediaItem = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(safeUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        updateState()
    }

    fun playStream(url: String, title: String, artist: String = "Live Stream") {
        val player = mediaController ?: return

        val safeUri = if (url.isNotBlank()) {
            Uri.parse(url)
        } else {
            Uri.parse("https://stream.nightride.fm/synthwave.mp3")
        }

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle("Live Stream")
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId("stream_${System.currentTimeMillis()}")
            .setUri(safeUri)
            .setMediaMetadata(mediaMetadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        updateState()
    }

    fun togglePlayPause() {
        val player = mediaController ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        updateState()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        updateState()
    }

    fun toggleShuffle() {
        val player = mediaController ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
        updateState()
    }

    fun toggleRepeat() {
        val player = mediaController ?: return
        player.repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        updateState()
    }

    fun release() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
