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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.GlowingProgressRing
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
    onTrackClick: (TrackItem) -> Unit,
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
        QuickAction("GitHub M3U", Icons.Default.Code, Screen.GitHubPlaylists, Color(0xFF6E40C9)),
        QuickAction("AI Chat", Icons.Default.AutoAwesome, Screen.AiChat, Color(0xFF00F0FF)),
        QuickAction("Link Health", Icons.Default.Sensors, Screen.LinkHealthChecker, Color(0xFF10B981))
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_dashboard_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MATRIX PLAYER",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Crystal Liquid Audio & Video",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.accentGlow
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onNavigate(Screen.AiChat) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.accentGlow)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Chat",
                                tint = colors.backgroundDeep
                            )
                        }

                        IconButton(
                            onClick = { onNavigate(Screen.Settings) },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(colors.glassCardBackground)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // Now Playing Hero Glass Banner
        item {
            val currentTrack = uiState.currentTrack ?: tracks.firstOrNull()
            if (currentTrack != null) {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                        .clickable { onNavigate(Screen.FullScreenMusicPlayer) },
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.85f),
                    borderColor = colors.glassBorder,
                    borderWidth = 1.5.dp,
                    testTag = "home_now_playing_hero"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Glowing album artwork circle
                        Box(
                            modifier = Modifier.size(76.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = if (uiState.durationMs > 0)
                                (uiState.currentPositionMs.toFloat() / uiState.durationMs).coerceIn(0f, 1f)
                            else 0f

                            GlowingProgressRing(
                                progress = progress,
                                modifier = Modifier.size(76.dp),
                                strokeWidth = 3.dp,
                                color = colors.accentGlow
                            )
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOW PLAYING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentGlow
                            )
                            Text(
                                text = currentTrack.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentTrack.artist} • ${currentTrack.album}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(colors.accentGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = colors.backgroundDeep,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Features Navigation Horizontal Pill Row
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "STUDIOS & HUBS",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickActions) { action ->
                        CrystalGlassCard(
                            modifier = Modifier
                                .clickable { onNavigate(action.screen) }
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(18.dp),
                            backgroundColor = action.accentColor.copy(alpha = 0.14f),
                            borderColor = action.accentColor.copy(alpha = 0.35f),
                            testTag = "hub_${action.title.lowercase()}"
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.title,
                                    tint = action.accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = action.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Discovered Tracks Section
        item {
            Text(
                text = "RECENT TRACKS",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textSecondary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 8.dp)
            )
        }

        items(tracks) { track ->
            CrystalGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 5.dp)
                    .clickable { onTrackClick(track) },
                shape = RoundedCornerShape(16.dp),
                backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                borderColor = if (track.id == uiState.currentTrack?.id) colors.accentGlow else colors.glassBorder,
                borderWidth = if (track.id == uiState.currentTrack?.id) 1.5.dp else 1.dp,
                testTag = "track_item_${track.id}"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.primaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${track.artist} • ${track.durationMs / 1000 / 60}:${String.format("%02d", (track.durationMs / 1000) % 60)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    if (track.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFF2A85),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
