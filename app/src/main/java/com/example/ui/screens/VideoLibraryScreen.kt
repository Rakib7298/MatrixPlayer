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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VideoLibrary
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
import com.example.model.VideoItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.components.VideoThumbnailView
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors

@Composable
fun VideoLibraryScreen(
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf(MediaSortOption.TITLE_ASC) }
    var isGridView by remember { mutableStateOf(true) }
    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var selectedFolderFilter by remember { mutableStateOf<String?>(null) }

    val folderCategories = remember(videos) {
        val folders = videos.map { video ->
            val uriStr = video.uri
            when {
                uriStr.contains("/") -> uriStr.substringBeforeLast("/").substringAfterLast("/")
                else -> "Video Library"
            }
        }.distinct().filter { it.isNotBlank() }
        listOf("All Folders") + folders
    }

    val processedVideos = remember(searchQuery, videos, sortOption, selectedFolderFilter) {
        var list = videos

        if (selectedFolderFilter != null && selectedFolderFilter != "All Folders") {
            list = list.filter { video ->
                video.uri.contains(selectedFolderFilter!!, ignoreCase = true)
            }
        }

        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.resolution.contains(searchQuery, ignoreCase = true)
            }
        }

        when (sortOption) {
            MediaSortOption.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            MediaSortOption.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            MediaSortOption.ARTIST_ASC -> list.sortedBy { it.resolution }
            MediaSortOption.DURATION_DESC -> list.sortedByDescending { it.durationMs }
            MediaSortOption.DURATION_ASC -> list.sortedBy { it.durationMs }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("video_library_screen")
    ) {
        MatrixTopAppBar(
            title = "Video Library",
            actions = {
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("btn_toggle_video_view")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List View",
                        tint = colors.accentGlow
                    )
                }
                IconButton(onClick = { onNavigate(Screen.SubtitleManager) }) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = "Subtitles",
                        tint = colors.accentGlow
                    )
                }
            }
        )

        // Codec compatibility badge banner
        CrystalGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = colors.accentGlow.copy(alpha = 0.12f),
            borderColor = colors.accentGlow.copy(alpha = 0.35f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = colors.accentGlow,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Hardware Accelerated Codecs",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "H.264 • H.265/HEVC • AV1 • VP9 • MKV • MP4 • WebM",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
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

        // Search & Sort Bar
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
                    .testTag("search_videos_field"),
                placeholder = { Text("Search videos...", color = colors.textSecondary, fontSize = 13.sp) },
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

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(processedVideos) { video ->
                    CrystalGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVideoClick(video) },
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.75f),
                        borderColor = colors.glassBorder,
                        testTag = "video_grid_card_${video.id}"
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                VideoThumbnailView(
                                    uri = video.uri,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = colors.accentGlow,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = video.resolution,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${video.sizeMb} MB • ${(video.durationMs / 1000) / 60}m",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(processedVideos) { video ->
                    CrystalGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onVideoClick(video) },
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.75f),
                        borderColor = colors.glassBorder,
                        testTag = "video_card_${video.id}"
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                VideoThumbnailView(
                                    uri = video.uri,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Video",
                                        tint = colors.accentGlow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${video.resolution} • ${video.sizeMb} MB • ${(video.durationMs / 1000) / 60}m ${((video.durationMs / 1000) % 60)}s",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
