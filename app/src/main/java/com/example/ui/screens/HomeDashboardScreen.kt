package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.streaming.StreamStation
import com.example.model.TrackItem
import com.example.model.VideoItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.GlassSurface
import com.example.ui.components.GlowingProgressRing
import com.example.ui.components.LiveStreamingOptionPanel
import com.example.ui.components.MediaThumbnailView
import com.example.ui.components.ThumbnailMediaType
import com.example.ui.components.VideoThumbnailView
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors
import com.example.viewmodel.PlayerUiState

data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val screen: Screen,
    val accentColor: Color
)

@Composable
fun HomeDashboardScreen(
    uiState: PlayerUiState,
    tracks: List<TrackItem>,
    videos: List<VideoItem> = emptyList(),
    history: List<TrackItem> = emptyList(),
    stations: List<StreamStation> = emptyList(),
    reactiveFrame: com.example.media.visualizer.AudioReactiveFrame = com.example.media.visualizer.AudioReactiveFrame(),
    onTrackClick: (TrackItem) -> Unit,
    onVideoClick: ((VideoItem) -> Unit)? = null,
    onOpenLocalFilePicker: () -> Unit = {},
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    val quickActions = listOf(
        QuickAction("Visualizer", Icons.Default.GraphicEq, Screen.VisualizerStudio, colors.accentGlow),
        QuickAction("EQ Studio", Icons.Default.Equalizer, Screen.EqStudio, Color(0xFFB026FF)),
        QuickAction("Videos", Icons.Default.VideoLibrary, Screen.VideoLibrary, Color(0xFF00E5FF)),
        QuickAction("Playlists", Icons.Default.QueueMusic, Screen.Playlists, Color(0xFFFF2A85)),
        QuickAction("Favorites", Icons.Default.Favorite, Screen.Favorites, Color(0xFFFF5252)),
        QuickAction("History", Icons.Default.History, Screen.PlaybackHistory, Color(0xFFFFAB00)),
        QuickAction("Streams", Icons.Default.Wifi, Screen.StreamingSources, Color(0xFF1DE9B6)),
        QuickAction("Folders", Icons.Default.Folder, Screen.Folders, Color(0xFF7C4DFF)),
        QuickAction("Albums", Icons.Default.Album, Screen.Albums, Color(0xFF00B0FF)),
        QuickAction("Artists", Icons.Default.Person, Screen.Artists, Color(0xFFE040FB)),
        QuickAction("Subtitles", Icons.Default.Subtitles, Screen.SubtitleManager, Color(0xFF69F0AE)),
        QuickAction("Auto Sync", Icons.Default.Sync, Screen.MediaSyncHub, Color(0xFF00F0FF)),
        QuickAction("Stream Import", Icons.Default.CloudDownload, Screen.UniversalStreamImport, Color(0xFF1DE9B6)),
        QuickAction("GitHub M3U", Icons.Default.Code, Screen.GitHubPlaylists, Color(0xFF6E40C9)),
        QuickAction("AI Chat", Icons.Default.AutoAwesome, Screen.AiChat, Color(0xFF00F0FF)),
        QuickAction("Link Health", Icons.Default.Sensors, Screen.LinkHealthChecker, Color(0xFF10B981))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_dashboard_screen")
    ) {
        // Pinned Top Bar: Stays permanently visible with compact spacing and crystal glass effect
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            borderRadius = 20.dp,
            borderWidth = 1.dp,
            backgroundOpacity = 0.22f,
            hasCenteredShadow = true
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MATRIX PLAYER",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            fontSize = 19.sp
                        ),
                        color = colors.textPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onNavigate(Screen.AiChat) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.accentGlow)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Chat",
                            tint = colors.backgroundDeep,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(Screen.Settings) },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.glassCardBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
        ) {

        // Last Playing Media File Preview Option (Audio, Video, or Streaming Link)
        item {
            val lastVideo = uiState.currentVideo ?: videos.firstOrNull()
            val lastTrack = history.firstOrNull() ?: uiState.currentTrack ?: tracks.firstOrNull()

            val isLiveStream = uiState.currentTrack?.album?.contains("Live Stream", ignoreCase = true) == true ||
                    uiState.currentTrack?.uri?.startsWith("http") == true
            val isVideoLast = uiState.currentVideo != null && !uiState.isPlaying

            val mediaTypeLabel = when {
                isVideoLast -> "VIDEO PREVIEW"
                isLiveStream -> "LIVE STREAM PREVIEW"
                else -> "LAST PLAYED AUDIO"
            }

            val mediaTitle = when {
                isVideoLast -> lastVideo?.title ?: "Video File"
                isLiveStream -> uiState.currentTrack?.title ?: "Live Broadcast"
                else -> lastTrack?.title ?: "Midnight Echo"
            }

            val mediaSubtitle = when {
                isVideoLast -> "${lastVideo?.resolution ?: "HD"} • Video File"
                isLiveStream -> uiState.currentTrack?.artist ?: "Streaming Station"
                else -> "${lastTrack?.artist ?: "Unknown Artist"} • ${lastTrack?.album ?: "Audio Library"}"
            }

            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable {
                        when {
                            isVideoLast && lastVideo != null -> {
                                onVideoClick?.invoke(lastVideo) ?: onNavigate(Screen.FullScreenVideoPlayer)
                            }
                            else -> {
                                onNavigate(Screen.FullScreenMusicPlayer)
                            }
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                borderRadius = 22.dp,
                borderWidth = 1.dp,
                backgroundOpacity = 0.16f,
                hasCenteredShadow = true,
                backdropGlow = colors.accentGlow.copy(alpha = 0.25f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Preview Visual: Video Thumbnail, Channel Logo, or Album Art
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        MediaThumbnailView(
                            url = if (isVideoLast) lastVideo?.thumbnailUri ?: lastVideo?.uri
                            else if (isLiveStream) uiState.currentTrack?.albumArtUri
                            else lastTrack?.albumArtUri,
                            title = mediaTitle,
                            modifier = Modifier.fillMaxSize(),
                            mediaType = if (isVideoLast) ThumbnailMediaType.VIDEO
                            else if (isLiveStream) ThumbnailMediaType.CHANNEL_STREAM
                            else ThumbnailMediaType.AUDIO,
                            showLiveBadge = isLiveStream,
                            badgeText = if (isLiveStream) "LIVE" else if (isVideoLast) "VIDEO" else null
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentGlow)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = mediaTypeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = colors.accentGlow
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = mediaTitle,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = mediaSubtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.5.sp),
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Round Glass Resume Button with Centered Shadow
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.accentGlow)
                            .clickable {
                                when {
                                    isVideoLast && lastVideo != null -> {
                                        onVideoClick?.invoke(lastVideo) ?: onNavigate(Screen.FullScreenVideoPlayer)
                                    }
                                    else -> {
                                        if (lastTrack != null) onTrackClick(lastTrack)
                                        else onNavigate(Screen.FullScreenMusicPlayer)
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = colors.backgroundDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Advanced Modern Option Panel for Streaming Live Media from M3U, M3U8, GitHub Repos
        item {
            LiveStreamingOptionPanel(
                stations = stations,
                onPlayTrack = onTrackClick,
                onPlayVideo = { video ->
                    onVideoClick?.invoke(video) ?: onNavigate(Screen.FullScreenVideoPlayer)
                },
                onOpenLocalFilePicker = onOpenLocalFilePicker,
                onNavigateToUniversalImport = { onNavigate(Screen.UniversalStreamImport) },
                onNavigateToGitHub = { onNavigate(Screen.GitHubPlaylists) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        // Quick Features Navigation Horizontal Pill Row
        item {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Text(
                    text = "STUDIOS & HUBS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickActions) { action ->
                        CrystalGlassCard(
                            modifier = Modifier
                                .clickable { onNavigate(action.screen) }
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = action.accentColor.copy(alpha = 0.14f),
                            borderColor = action.accentColor.copy(alpha = 0.35f),
                            testTag = "hub_${action.title.lowercase()}"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    tint = action.accentColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = action.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Real-Time Liquid Glass Visualizer Studio Card (Powered by Media3 Audio Engine)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                com.example.ui.components.LiquidGlassVisualizerCanvas(
                    reactiveFrame = reactiveFrame,
                    isPlaying = uiState.isPlaying,
                    heightDp = 130.dp,
                    showControlsHeader = true,
                    initialType = com.example.ui.components.LiquidVisualizerType.NEON_SPECTRUM,
                    accentColor = colors.accentGlow,
                    secondaryColor = Color(0xFFB026FF)
                )
            }
        }

        // Recent Discovered Tracks Section
        item {
            Text(
                text = "RECENT TRACKS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 6.dp)
            )
        }

        items(tracks) { track ->
            CrystalGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp)
                    .clickable { onTrackClick(track) },
                shape = RoundedCornerShape(12.dp),
                backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                borderColor = if (track.id == uiState.currentTrack?.id) colors.accentGlow else colors.glassBorder,
                borderWidth = if (track.id == uiState.currentTrack?.id) 1.2.dp else 0.8.dp,
                testTag = "track_item_${track.id}"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MediaThumbnailView(
                        url = track.albumArtUri,
                        title = track.title,
                        modifier = Modifier.size(34.dp),
                        mediaType = ThumbnailMediaType.AUDIO
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.durationMs / 1000 / 60}:${String.format("%02d", (track.durationMs / 1000) % 60)}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = colors.textSecondary
                        )
                    }

                    if (track.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFF2A85),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
}
