package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.FloatingPlaybackBar
import com.example.ui.components.MatrixBackground
import com.example.ui.components.MatrixBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AlbumsScreen
import com.example.ui.screens.ArtistsScreen
import com.example.ui.screens.EqStudioScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.FoldersScreen
import com.example.ui.screens.FullScreenMusicPlayerScreen
import com.example.ui.screens.FullScreenVideoPlayerScreen
import com.example.ui.screens.GitHubPlaylistScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.MusicLibraryScreen
import com.example.ui.screens.PlaybackHistoryScreen
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StreamingSourcesScreen
import com.example.ui.screens.SubtitleManagerScreen
import com.example.ui.screens.VideoLibraryScreen
import com.example.ui.screens.VisualizerStudioScreen
import com.example.ui.theme.MatrixPlayerTheme
import com.example.viewmodel.MatrixPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MatrixPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            MatrixPlayerTheme(themeMode = uiState.currentTheme) {
                MatrixAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MatrixAppContent(viewModel: MatrixPlayerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tracks by viewModel.roomTracks.collectAsStateWithLifecycle()
    val videos by viewModel.roomVideos.collectAsStateWithLifecycle()
    val favorites by viewModel.roomFavorites.collectAsStateWithLifecycle()
    val history by viewModel.roomHistory.collectAsStateWithLifecycle()

    // Navigation BackStack state
    val screenStack = remember { mutableStateListOf<Screen>(Screen.Splash) }
    val currentScreen = screenStack.lastOrNull() ?: Screen.Home

    fun navigateTo(screen: Screen) {
        if (screen == Screen.Home) {
            screenStack.clear()
            screenStack.add(Screen.Home)
        } else {
            screenStack.add(screen)
        }
    }

    fun popBack() {
        if (screenStack.size > 1) {
            screenStack.removeAt(screenStack.size - 1)
        }
    }

    // Handle system back button for all secondary screens
    if (screenStack.size > 1) {
        BackHandler {
            popBack()
        }
    }

    val isFullScreenPlayback = currentScreen == Screen.FullScreenMusicPlayer || currentScreen == Screen.FullScreenVideoPlayer
    val isSplash = currentScreen == Screen.Splash

    MatrixBackground {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                if (!isSplash && !isFullScreenPlayback) {
                    androidx.compose.foundation.layout.Column {
                        // Floating Playback Bar: Render Aqua Liquid Glass Floating Card if Aqua theme or standard
                        if (uiState.currentTheme == com.example.ui.theme.MatrixThemeMode.AQUA_LIQUID_GLASS) {
                            com.example.ui.components.AquaLiquidGlassFloatingCard(
                                track = uiState.currentTrack,
                                isPlaying = uiState.isPlaying,
                                currentPositionMs = uiState.currentPositionMs,
                                durationMs = uiState.durationMs,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextTrack() },
                                onPrevious = { viewModel.previousTrack() },
                                onClickCard = { navigateTo(Screen.FullScreenMusicPlayer) }
                            )
                        } else {
                            FloatingPlaybackBar(
                                track = uiState.currentTrack,
                                isPlaying = uiState.isPlaying,
                                currentPositionMs = uiState.currentPositionMs,
                                durationMs = uiState.durationMs,
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextTrack() },
                                onPrevious = { viewModel.previousTrack() },
                                onClickBar = { navigateTo(Screen.FullScreenMusicPlayer) }
                            )
                        }

                        // Glass Bottom Navigation Bar
                        MatrixBottomNavBar(
                            currentRoute = currentScreen.route,
                            onNavigate = { screen -> navigateTo(screen) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFullScreenPlayback || isSplash) androidx.compose.foundation.layout.PaddingValues() else innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        Screen.Splash -> SplashScreen(
                            onSplashFinished = {
                                screenStack.clear()
                                screenStack.add(Screen.Home)
                            }
                        )

                        Screen.Home -> HomeDashboardScreen(
                            uiState = uiState,
                            tracks = tracks,
                            onTrackClick = { track ->
                                viewModel.playTrack(track)
                            },
                            onNavigate = { s -> navigateTo(s) }
                        )

                        Screen.MusicLibrary -> MusicLibraryScreen(
                            tracks = tracks,
                            currentTrackId = uiState.currentTrack?.id,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onNavigate = { s -> navigateTo(s) },
                            onShuffleAll = {
                                viewModel.toggleShuffle()
                                if (tracks.isNotEmpty()) {
                                    viewModel.playTrack(tracks.random())
                                }
                            }
                        )

                        Screen.Albums -> AlbumsScreen(
                            albums = viewModel.albums,
                            onAlbumClick = { },
                            onBack = { popBack() }
                        )

                        Screen.Artists -> ArtistsScreen(
                            artists = viewModel.artists,
                            onArtistClick = { },
                            onBack = { popBack() }
                        )

                        Screen.Folders -> FoldersScreen(
                            folders = viewModel.folders,
                            onFolderClick = { },
                            onBack = { popBack() }
                        )

                        Screen.Playlists -> PlaylistsScreen(
                            playlists = viewModel.roomPlaylists.collectAsStateWithLifecycle().value,
                            allLibraryTracks = tracks,
                            onPlayTrack = { track -> viewModel.playTrack(track) },
                            onCreatePlaylist = { name, color -> viewModel.createPlaylist(name, color) },
                            onDeletePlaylist = { id -> viewModel.deletePlaylist(id) },
                            onAddTrackToPlaylist = { plId, trackId -> viewModel.addTrackToPlaylist(plId, trackId) },
                            onRemoveTrackFromPlaylist = { plId, trackId -> viewModel.removeTrackFromPlaylist(plId, trackId) },
                            getTracksForPlaylist = { plId -> viewModel.getTracksForPlaylist(plId) },
                            onBack = { popBack() }
                        )

                        Screen.FullScreenMusicPlayer -> FullScreenMusicPlayerScreen(
                            uiState = uiState,
                            onPlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextTrack() },
                            onPrevious = { viewModel.previousTrack() },
                            onSeek = { pos -> viewModel.seekTo(pos) },
                            onToggleShuffle = { viewModel.toggleShuffle() },
                            onToggleRepeat = { viewModel.toggleRepeat() },
                            onToggleFavorite = { viewModel.toggleFavorite() },
                            onNavigate = { s -> navigateTo(s) },
                            onBack = { popBack() }
                        )

                        Screen.VisualizerStudio -> VisualizerStudioScreen(
                            selectedIndex = uiState.selectedVisualizerIndex,
                            isPlaying = uiState.isPlaying,
                            reactiveFrame = viewModel.reactiveAudioFrame.collectAsStateWithLifecycle().value,
                            onSelectVisualizer = { idx -> viewModel.selectVisualizer(idx) },
                            onBack = { popBack() }
                        )

                        Screen.EqStudio -> EqStudioScreen(
                            bands = uiState.eqBands,
                            bassBoost = uiState.bassBoost,
                            selectedPreset = uiState.selectedPreset,
                            reactiveFrame = viewModel.reactiveAudioFrame.collectAsStateWithLifecycle().value,
                            onBandChange = { idx, gain -> viewModel.updateEqBand(idx, gain) },
                            onBassBoostChange = { b -> viewModel.setBassBoost(b) },
                            onSelectPreset = { preset -> viewModel.setEqPreset(preset) },
                            onBack = { popBack() }
                        )

                        Screen.VideoLibrary -> VideoLibraryScreen(
                            videos = videos,
                            onVideoClick = { video ->
                                viewModel.playVideo(video)
                                navigateTo(Screen.FullScreenVideoPlayer)
                            },
                            onNavigate = { s -> navigateTo(s) }
                        )

                        Screen.FullScreenVideoPlayer -> FullScreenVideoPlayerScreen(
                            video = uiState.currentVideo ?: videos.firstOrNull(),
                            videoEngine = viewModel.videoEngine,
                            subtitleManager = viewModel.subtitleManager,
                            onNavigate = { s -> navigateTo(s) },
                            onBack = { popBack() }
                        )

                        Screen.SubtitleManager -> SubtitleManagerScreen(
                            subtitleManager = viewModel.subtitleManager,
                            onBack = { popBack() }
                        )

                        Screen.StreamingSources -> StreamingSourcesScreen(
                            streamingManager = viewModel.streamingManager,
                            onPlayStation = { station -> viewModel.playStreamStation(station) },
                            onNavigateToGitHub = { navigateTo(Screen.GitHubPlaylists) },
                            onBack = { popBack() }
                        )

                        Screen.GitHubPlaylists -> GitHubPlaylistScreen(
                            playlistManager = viewModel.gitHubPlaylistManager,
                            repository = viewModel.repository,
                            onPlayTrack = { track -> viewModel.playTrack(track) },
                            onBack = { popBack() }
                        )

                        Screen.PlaybackHistory -> PlaybackHistoryScreen(
                            historyTracks = history,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onBack = { popBack() }
                        )

                        Screen.Favorites -> FavoritesScreen(
                            favoriteTracks = favorites,
                            onTrackClick = { track -> viewModel.playTrack(track) },
                            onBack = { popBack() }
                        )

                        Screen.Settings -> SettingsScreen(
                            currentTheme = uiState.currentTheme,
                            onSelectTheme = { theme -> viewModel.setTheme(theme) },
                            onNavigate = { s -> navigateTo(s) },
                            onBack = { popBack() }
                        )

                        Screen.About -> AboutScreen(
                            onBack = { popBack() }
                        )
                    }
                }
            }
        }
    }
}
