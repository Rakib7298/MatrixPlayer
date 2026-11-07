package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PlaylistItem
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.flow.Flow

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistItem>,
    allLibraryTracks: List<TrackItem>,
    onPlayTrack: (TrackItem) -> Unit,
    onCreatePlaylist: (name: String, colorHex: String) -> Unit,
    onDeletePlaylist: (playlistId: String) -> Unit,
    onAddTrackToPlaylist: (playlistId: String, mediaId: String) -> Unit,
    onRemoveTrackFromPlaylist: (playlistId: String, mediaId: String) -> Unit,
    getTracksForPlaylist: (playlistId: String) -> Flow<List<TrackItem>>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedPlaylistForDetail by remember { mutableStateOf<PlaylistItem?>(null) }
    var showAddSongsDialog by remember { mutableStateOf(false) }

    // Color options for custom playlist
    val colorOptions = listOf(
        "#00F0FF", "#1DE9B6", "#B026FF", "#E040FB", "#FF5252", "#F59E0B"
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("playlists_screen"),
        containerColor = Color.Transparent,
        topBar = {
            MatrixTopAppBar(
                title = "Playlists (${playlists.size})",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = colors.accentGlow,
                contentColor = colors.backgroundDeep,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("fab_create_playlist")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Playlist")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Stats Banner
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "OFFLINE & CLOUD PLAYLISTS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentGlow
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Room Persistent Collections",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Manage custom queues, dynamic mixes, and smart folders",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            items(playlists) { playlist ->
                val cardColor = try {
                    Color(android.graphics.Color.parseColor(playlist.iconColorHex))
                } catch (e: Exception) {
                    colors.accentGlow
                }

                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlaylistForDetail = playlist },
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder,
                    testTag = "playlist_card_${playlist.id}"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(cardColor.copy(alpha = 0.85f), colors.backgroundDeep)
                                    )
                                )
                                .border(1.dp, cardColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${playlist.trackCount} Tracks • ${playlist.durationText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Playlist",
                                tint = colors.textSecondary.copy(alpha = 0.7f)
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }
                }
            }
        }
    }

    // 1. CREATE PLAYLIST DIALOG
    if (showCreateDialog) {
        var playlistName by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf(colorOptions[0]) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "New Matrix Playlist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Create a custom playlist stored in your local Room database.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("e.g. Cyberpunk Drift", color = colors.textSecondary) },
                        label = { Text("Playlist Name", color = colors.textSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_playlist_name"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentGlow,
                            unfocusedBorderColor = colors.glassBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Accent Color:",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val parsed = Color(android.graphics.Color.parseColor(hex))
                            val isChosen = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) Color.White else colors.glassBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistName.isNotBlank()) {
                            onCreatePlaylist(playlistName.trim(), selectedColor)
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentGlow,
                        contentColor = colors.backgroundDeep
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_playlist")
                ) {
                    Text("Create", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            },
            containerColor = colors.backgroundSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 2. PLAYLIST DETAIL DIALOG / MODAL
    selectedPlaylistForDetail?.let { playlist ->
        val playlistTracks by getTracksForPlaylist(playlist.id).collectAsState(initial = emptyList())
        val cardColor = try {
            Color(android.graphics.Color.parseColor(playlist.iconColorHex))
        } catch (e: Exception) {
            colors.accentGlow
        }

        Dialog(onDismissRequest = { selectedPlaylistForDetail = null }) {
            CrystalGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(540.dp)
                    .testTag("playlist_detail_dialog"),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = colors.backgroundSurface.copy(alpha = 0.95f),
                borderColor = cardColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardColor.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.QueueMusic, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = playlist.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${playlistTracks.size} Tracks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        IconButton(onClick = { selectedPlaylistForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action buttons: Play All, Add Songs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (playlistTracks.isNotEmpty()) {
                                    onPlayTrack(playlistTracks.first())
                                    selectedPlaylistForDetail = null
                                }
                            },
                            enabled = playlistTracks.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = colors.backgroundDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play All", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { showAddSongsDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.backgroundDeep.copy(alpha = 0.8f),
                                contentColor = colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = colors.accentGlow)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Songs", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Track List
                    if (playlistTracks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, tint = colors.textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("This playlist is empty", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Click 'Add Songs' to add music", color = colors.accentGlow, fontSize = 12.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(playlistTracks) { track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.backgroundDeep.copy(alpha = 0.5f))
                                        .clickable {
                                            onPlayTrack(track)
                                            selectedPlaylistForDetail = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = cardColor, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
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

                                    IconButton(
                                        onClick = { onRemoveTrackFromPlaylist(playlist.id, track.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = colors.textSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Songs Subdialog
        if (showAddSongsDialog) {
            Dialog(onDismissRequest = { showAddSongsDialog = false }) {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(460.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.95f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Select Songs from Library",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            IconButton(onClick = { showAddSongsDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(allLibraryTracks) { track ->
                                val isInPlaylist = playlistTracks.any { it.id == track.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.backgroundDeep.copy(alpha = 0.4f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artist,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }

                                    if (isInPlaylist) {
                                        Text("Added", color = colors.accentGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        IconButton(
                                            onClick = { onAddTrackToPlaylist(playlist.id, track.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add Track", tint = colors.accentGlow)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
