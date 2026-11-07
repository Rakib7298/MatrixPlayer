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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors

@Composable
fun MusicLibraryScreen(
    tracks: List<TrackItem>,
    currentTrackId: String?,
    onTrackClick: (TrackItem) -> Unit,
    onNavigate: (Screen) -> Unit,
    onShuffleAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredTracks = remember(searchQuery, tracks) {
        if (searchQuery.isBlank()) tracks
        else tracks.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("music_library_screen")
    ) {
        MatrixTopAppBar(
            title = "Music Library",
            actions = {
                IconButton(
                    onClick = onShuffleAll,
                    modifier = Modifier.testTag("btn_shuffle_library")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle All",
                        tint = colors.accentGlow
                    )
                }
            }
        )

        // Subcategory Pills (Albums, Artists, Folders, Playlists)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.accentGlow.copy(alpha = 0.2f),
                    borderColor = colors.accentGlow,
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Tracks (${tracks.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.accentGlow,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.glassCardBackground,
                    borderColor = colors.glassBorder,
                    modifier = Modifier.clickable { onNavigate(Screen.Albums) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Album, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Albums", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }
            }
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.glassCardBackground,
                    borderColor = colors.glassBorder,
                    modifier = Modifier.clickable { onNavigate(Screen.Artists) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Artists", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }
            }
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.glassCardBackground,
                    borderColor = colors.glassBorder,
                    modifier = Modifier.clickable { onNavigate(Screen.Folders) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Folders", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }
            }
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.glassCardBackground,
                    borderColor = colors.glassBorder,
                    modifier = Modifier.clickable { onNavigate(Screen.Playlists) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QueueMusic, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Playlists", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_tracks_field"),
            placeholder = { Text("Search title, artist or album...", color = colors.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.accentGlow) },
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accentGlow,
                unfocusedBorderColor = colors.glassBorder,
                focusedContainerColor = colors.backgroundSurface.copy(alpha = 0.5f),
                unfocusedContainerColor = colors.backgroundSurface.copy(alpha = 0.3f),
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary
            ),
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(filteredTracks) { track ->
                val isPlayingCurrent = track.id == currentTrackId
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { onTrackClick(track) },
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = if (isPlayingCurrent) colors.accentGlow.copy(alpha = 0.15f) else colors.backgroundSurface.copy(alpha = 0.65f),
                    borderColor = if (isPlayingCurrent) colors.accentGlow else colors.glassBorder,
                    borderWidth = 1.dp,
                    testTag = "track_card_${track.id}"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlayingCurrent) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isPlayingCurrent) colors.accentGlow else colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${track.artist} • ${track.album}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "${track.durationMs / 1000 / 60}:${String.format("%02d", (track.durationMs / 1000) % 60)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )

                        IconButton(onClick = {}) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
}
