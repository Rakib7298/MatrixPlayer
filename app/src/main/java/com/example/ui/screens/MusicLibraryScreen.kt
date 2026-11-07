package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.model.MediaSortOption
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
    var sortOption by remember { mutableStateOf(MediaSortOption.TITLE_ASC) }
    var isGridView by remember { mutableStateOf(false) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var selectedFolderFilter by remember { mutableStateOf<String?>(null) }

    // Discover unique folder categories from track URIs
    val folderCategories = remember(tracks) {
        val folders = tracks.map { track ->
            val uriStr = track.uri
            when {
                uriStr.contains("/") -> uriStr.substringBeforeLast("/").substringAfterLast("/")
                else -> "Default Media"
            }
        }.distinct().filter { it.isNotBlank() }
        listOf("All Folders") + folders
    }

    val processedTracks = remember(searchQuery, tracks, sortOption, selectedFolderFilter) {
        var list = tracks

        // Apply folder filter
        if (selectedFolderFilter != null && selectedFolderFilter != "All Folders") {
            list = list.filter { track ->
                track.uri.contains(selectedFolderFilter!!, ignoreCase = true)
            }
        }

        // Apply search query
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true) ||
                        it.album.contains(searchQuery, ignoreCase = true)
            }
        }

        // Apply sorting
        when (sortOption) {
            MediaSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            MediaSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            MediaSortOption.ARTIST_ASC -> list.sortedBy { it.artist.lowercase() }
            MediaSortOption.DURATION_DESC -> list.sortedByDescending { it.durationMs }
            MediaSortOption.DURATION_ASC -> list.sortedBy { it.durationMs }
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
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("btn_toggle_view_mode")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List View",
                        tint = colors.accentGlow
                    )
                }
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

        // Subcategory & Folder Filter Pills
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.accentGlow.copy(alpha = 0.2f),
                    borderColor = colors.accentGlow,
                    modifier = Modifier.clickable { selectedFolderFilter = null }
                ) {
                    Text(
                        text = "Tracks (${processedTracks.size})",
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
        }

        // Folder Filter Row
        if (folderCategories.size > 1) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(folderCategories) { folderName ->
                    val isSelected = (selectedFolderFilter == folderName) || (selectedFolderFilter == null && folderName == "All Folders")
                    CrystalGlassCard(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = if (isSelected) colors.accentGlow.copy(alpha = 0.25f) else colors.backgroundSurface.copy(alpha = 0.4f),
                        borderColor = if (isSelected) colors.accentGlow else colors.glassBorder,
                        modifier = Modifier.clickable {
                            selectedFolderFilter = if (folderName == "All Folders") null else folderName
                        }
                    ) {
                        Text(
                            text = "📁 $folderName",
                            fontSize = 12.sp,
                            color = if (isSelected) colors.accentGlow else colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Search and Sort Control Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_tracks_field"),
                placeholder = { Text("Search music...", color = colors.textSecondary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = colors.accentGlow, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(16.dp),
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

            Spacer(modifier = Modifier.width(8.dp))

            // Sort Dropdown Chip
            Box {
                CrystalGlassCard(
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.5f),
                    borderColor = colors.glassBorder,
                    modifier = Modifier.clickable { sortDropdownExpanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sortOption.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.accentGlow
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Sort Options",
                            tint = colors.accentGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = sortDropdownExpanded,
                    onDismissRequest = { sortDropdownExpanded = false },
                    modifier = Modifier.background(colors.backgroundSurface)
                ) {
                    MediaSortOption.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label, color = colors.textPrimary, fontSize = 13.sp) },
                            onClick = {
                                sortOption = option
                                sortDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Main Content View (Grid vs List)
        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(processedTracks) { track ->
                    val isPlayingCurrent = track.id == currentTrackId
                    CrystalGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackClick(track) },
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = if (isPlayingCurrent) colors.accentGlow.copy(alpha = 0.2f) else colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = if (isPlayingCurrent) colors.accentGlow else colors.glassBorder,
                        borderWidth = 1.dp,
                        testTag = "track_grid_card_${track.id}"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colors.primaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isPlayingCurrent) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isPlayingCurrent) colors.accentGlow else colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(processedTracks) { track ->
                    val isPlayingCurrent = track.id == currentTrackId
                    CrystalGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onTrackClick(track) },
                        shape = RoundedCornerShape(16.dp),
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
                        }
                    }
                }
            }
        }
    }
}
