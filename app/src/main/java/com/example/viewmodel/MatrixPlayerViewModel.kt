package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MatrixDatabase
import com.example.data.media.LocalMediaScanner
import com.example.data.repository.MediaRepository
import com.example.model.AlbumItem
import com.example.model.ArtistItem
import com.example.model.FolderItem
import com.example.model.PlaylistItem
import com.example.model.TrackItem
import com.example.model.VideoItem
import com.example.ui.theme.MatrixThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: TrackItem? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 45000L,
    val durationMs: Long = 214000L,
    val isShuffle: Boolean = false,
    val isRepeatOne: Boolean = false,
    val isFavorite: Boolean = false,
    val volumeLevel: Float = 0.85f,
    val bassBoost: Float = 60f,
    val currentTheme: MatrixThemeMode = MatrixThemeMode.MIDNIGHT_NEON,
    val selectedVisualizerIndex: Int = 0,
    val eqBands: List<Float> = listOf(4.0f, -1.5f, 2.5f, -0.5f),
    val selectedPreset: String = "Deep",
    val sleepTimerMinutes: Int? = null,
    val isScanning: Boolean = false,
    val currentVideo: VideoItem? = null
)

class MatrixPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = MatrixDatabase.getInstance(application)
    private val scanner = LocalMediaScanner(application)
    val repository = MediaRepository(database, scanner)
    val playbackManager = com.example.media.AudioPlaybackManager(application, viewModelScope)
    val effectManager = com.example.media.audiofx.AndroidAudioEffectManager(application)
    val visualizerEngine = com.example.media.visualizer.RealAudioVisualizerEngine(application)
    val videoEngine = com.example.media.video.MatrixVideoEngine(application)
    val subtitleManager = com.example.media.subtitle.MatrixSubtitleManager(application)
    val streamingManager = com.example.media.streaming.StreamingManager(application, viewModelScope)
    val gitHubPlaylistManager = com.example.media.github.GitHubPlaylistManager(application, viewModelScope)
    val aiEngine = com.example.media.ai.MatrixAiChatEngine(application, viewModelScope)
    val healthCheckerEngine = com.example.media.streaming.LinkHealthCheckerEngine(application)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val reactiveAudioFrame: StateFlow<com.example.media.visualizer.AudioReactiveFrame> =
        visualizerEngine.reactiveFrame

    // Default reference media items
    val defaultTracks = listOf(
        TrackItem("1", "Midnight Echo", "Noctara", "Digital Horizons", 288000L, isFavorite = true),
        TrackItem("2", "Snooze", "SZA", "SOS", 201000L, isFavorite = true),
        TrackItem("3", "Cyber Odyssey", "Neon Pulse", "Digital Horizons", 214000L, isFavorite = true),
        TrackItem("4", "Crystal Matrix", "Liquid Synths", "Liquid Glass", 188000L, isFavorite = false),
        TrackItem("5", "Hyperdrive Groove", "Vector Velocity", "Neon Horizons", 202000L, isFavorite = false)
    )

    val defaultVideos = listOf(
        VideoItem("v1", "The Studio", 180000L, "3840x2160 (4K)", 450.2, "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"),
        VideoItem("v2", "Liquid Glass Visuals Showcase", 95000L, "1920x1080 (FHD)", 180.5, "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"),
        VideoItem("v3", "Audio Spectrum Studio Demo", 320000L, "1920x1080 (FHD)", 290.0, "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4")
    )

    val playlists = listOf(
        PlaylistItem("p1", "Cyber Bass Essentials", 18, "1h 12m", "#00F0FF"),
        PlaylistItem("p2", "Late Night Liquid Vibes", 14, "54m", "#1DE9B6"),
        PlaylistItem("p3", "High Octane Workout", 22, "1h 35m", "#B026FF"),
        PlaylistItem("p4", "Synthwave Chillout", 12, "48m", "#E040FB")
    )

    val albums = listOf(
        AlbumItem("Digital Horizons", "Neon Pulse", 8, "2024"),
        AlbumItem("Liquid Glass", "Liquid Synths", 10, "2024"),
        AlbumItem("SOS", "SZA", 12, "2023")
    )

    val artists = listOf(
        ArtistItem("Noctara", 10, 2),
        ArtistItem("SZA", 14, 2),
        ArtistItem("Neon Pulse", 12, 2),
        ArtistItem("Liquid Synths", 18, 3)
    )

    val folders = listOf(
        FolderItem("Music", "/storage/emulated/0/Music", 42, 0),
        FolderItem("Download", "/storage/emulated/0/Download", 15, 6),
        FolderItem("Movies", "/storage/emulated/0/Movies", 0, 8)
    )

    // Reactive StateFlows from Room database with fallback to default reference tracks
    val roomTracks: StateFlow<List<TrackItem>> = repository.audioTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultTracks)

    val roomVideos: StateFlow<List<VideoItem>> = repository.videos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultVideos)

    val roomFavorites: StateFlow<List<TrackItem>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultTracks.filter { it.isFavorite })

    val roomHistory: StateFlow<List<TrackItem>> = repository.playbackHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultTracks.take(3))

    val roomPlaylists: StateFlow<List<PlaylistItem>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), playlists)

    fun createPlaylist(name: String, colorHex: String = "#00F0FF") {
        viewModelScope.launch {
            repository.createPlaylist(name, colorHex)
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
        }
    }

    fun addTrackToPlaylist(playlistId: String, mediaId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, mediaId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: String, mediaId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, mediaId)
        }
    }

    fun getTracksForPlaylist(playlistId: String): kotlinx.coroutines.flow.Flow<List<TrackItem>> =
        repository.getTracksForPlaylist(playlistId)

    fun playPlaylist(tracks: List<TrackItem>) {
        if (tracks.isNotEmpty()) {
            playTrack(tracks.first())
        }
    }

    fun playStreamStation(station: com.example.media.streaming.StreamStation) {
        streamingManager.playStation(station)
        if (station.protocol == com.example.media.streaming.StreamProtocol.HLS && station.url.contains("mux.dev")) {
            val video = VideoItem(
                id = station.id,
                title = station.name,
                durationMs = 0L,
                resolution = "Adaptive HLS",
                sizeMb = 0.0,
                uri = station.url
            )
            playVideo(video)
        } else {
            playbackManager.playStream(station.url, station.name, station.genre)
            _uiState.value = _uiState.value.copy(
                currentTrack = TrackItem(
                    id = station.id,
                    title = station.name,
                    artist = station.genre,
                    album = "Live Stream (${station.protocol.label})",
                    durationMs = 0L,
                    uri = station.url,
                    isFavorite = station.isBookmarked
                ),
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = 0L
            )
        }
    }

    init {
        // Initialize default track
        _uiState.value = _uiState.value.copy(
            currentTrack = defaultTracks[0],
            durationMs = defaultTracks[0].durationMs,
            isFavorite = defaultTracks[0].isFavorite
        )

        // Seed Room DB and scan local media in background
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty(defaultTracks, defaultVideos)
            scanLocalMediaStore()
        }

        // Ticking progress loop for active playback
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isPlaying) {
                    val next = _uiState.value.currentPositionMs + 1000
                    if (next >= _uiState.value.durationMs) {
                        _uiState.value = _uiState.value.copy(currentPositionMs = 0L)
                    } else {
                        _uiState.value = _uiState.value.copy(currentPositionMs = next)
                    }
                }
            }
        }

        // Attach RealAudioVisualizerEngine & AndroidAudioEffectManager to Media3 audio session ID
        viewModelScope.launch {
            playbackManager.engineState.collect { engineState ->
                if (engineState.audioSessionId > 0) {
                    visualizerEngine.attachAudioSession(engineState.audioSessionId)
                    effectManager.attachAudioSession(engineState.audioSessionId)
                }
            }
        }

        // Ticking audio-reactive loop for DSP fallback frame generation
        viewModelScope.launch {
            var t = 0f
            while (true) {
                delay(50)
                t += 0.1f
                visualizerEngine.updateSimulatedFrame(t, _uiState.value.isPlaying)
            }
        }
    }

    fun scanLocalMediaStore() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isScanning = true)
            try {
                repository.scanLocalMedia()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.value = _uiState.value.copy(isScanning = false)
            }
        }
    }

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
        val nextPlay = !_uiState.value.isPlaying
        _uiState.value = _uiState.value.copy(isPlaying = nextPlay)
        if (nextPlay) {
            _uiState.value.currentTrack?.let { track ->
                viewModelScope.launch {
                    repository.recordHistory(track.id, _uiState.value.currentPositionMs)
                }
            }
        }
    }

    fun playTrack(track: TrackItem) {
        playbackManager.playTrack(track)
        _uiState.value = _uiState.value.copy(
            currentTrack = track,
            isPlaying = true,
            currentPositionMs = 0L,
            durationMs = track.durationMs,
            isFavorite = track.isFavorite
        )
        viewModelScope.launch {
            repository.recordHistory(track.id, 0L)
        }
    }

    fun nextTrack() {
        val currentList = roomTracks.value.ifEmpty { defaultTracks }
        val currentIdx = currentList.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val nextIdx = if (currentIdx in 0 until currentList.size - 1) currentIdx + 1 else 0
        playTrack(currentList[nextIdx])
    }

    fun previousTrack() {
        val currentList = roomTracks.value.ifEmpty { defaultTracks }
        val currentIdx = currentList.indexOfFirst { it.id == _uiState.value.currentTrack?.id }
        val prevIdx = if (currentIdx > 0) currentIdx - 1 else currentList.size - 1
        playTrack(currentList[prevIdx])
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
        _uiState.value = _uiState.value.copy(isShuffle = !_uiState.value.isShuffle)
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
        _uiState.value = _uiState.value.copy(isRepeatOne = !_uiState.value.isRepeatOne)
    }

    fun toggleFavorite() {
        val nextFav = !_uiState.value.isFavorite
        _uiState.value = _uiState.value.copy(isFavorite = nextFav)
        _uiState.value.currentTrack?.let { track ->
            viewModelScope.launch {
                repository.toggleFavorite(track.id, nextFav)
            }
        }
    }

    fun setTheme(theme: MatrixThemeMode) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
    }

    fun selectVisualizer(index: Int) {
        _uiState.value = _uiState.value.copy(selectedVisualizerIndex = index)
    }

    fun updateEqBand(index: Int, gain: Float) {
        val newBands = _uiState.value.eqBands.toMutableList()
        if (index in newBands.indices) {
            newBands[index] = gain
            _uiState.value = _uiState.value.copy(eqBands = newBands)
            effectManager.setBandGain(index, gain)
        }
    }

    fun setBassBoost(value: Float) {
        _uiState.value = _uiState.value.copy(bassBoost = value)
        effectManager.setBassBoostStrength(value)
    }

    fun setEqPreset(preset: String) {
        val gains = when (preset) {
            "Deep" -> listOf(4.0f, -1.5f, 2.5f, -0.5f)
            "Electronic" -> listOf(3.5f, 1.0f, 2.0f, 4.0f)
            "Vocal" -> listOf(-1.0f, 2.5f, 3.0f, -0.5f)
            "Flat" -> listOf(0.0f, 0.0f, 0.0f, 0.0f)
            else -> listOf(2.0f, 0.0f, 1.5f, 2.0f)
        }
        _uiState.value = _uiState.value.copy(selectedPreset = preset, eqBands = gains)
        gains.forEachIndexed { idx, g ->
            effectManager.setBandGain(idx, g)
        }
    }

    fun playVideo(video: VideoItem) {
        _uiState.value = _uiState.value.copy(currentVideo = video)
        videoEngine.prepareAndPlay(video.uriString, video.title)
    }

    override fun onCleared() {
        super.onCleared()
        playbackManager.release()
        effectManager.release()
        visualizerEngine.release()
        videoEngine.release()
    }
}
