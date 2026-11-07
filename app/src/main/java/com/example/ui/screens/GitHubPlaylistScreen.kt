package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.MediaRepository
import com.example.media.github.GitHubPlaylistManager
import com.example.media.github.GitHubPresetPlaylist
import com.example.media.github.M3uPlaylist
import com.example.media.github.M3uTrack
import com.example.media.m3u.M3uParserService
import com.example.media.m3u.ParsedM3uPlaylist
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.M3uPreviewDialog
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.launch

@Composable
fun GitHubPlaylistScreen(
    playlistManager: GitHubPlaylistManager,
    repository: MediaRepository,
    onPlayTrack: (TrackItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val isLoading by playlistManager.isLoading.collectAsState()
    val statusMessage by playlistManager.statusMessage.collectAsState()
    val previewPlaylist by playlistManager.previewPlaylist.collectAsState()
    val importedPlaylists by playlistManager.importedGitHubPlaylists.collectAsState()

    var inputUrl by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var selectedAccentColor by remember { mutableStateOf("#00F0FF") }
    var showDetailedModal by remember { mutableStateOf(false) }

    val accentColors = listOf("#00F0FF", "#1DE9B6", "#B026FF", "#E040FB", "#FF5252", "#F59E0B")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("github_playlist_screen")
    ) {
        MatrixTopAppBar(
            title = "GitHub Playlists",
            onBackClick = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HERO BANNER
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.75f),
                    borderColor = colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentGlow)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "OPEN SOURCE COMMUNITY PLAYLISTS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accentGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GitHub M3U / M3U8 Integration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Import, parse, and synchronize direct M3U/M3U8 playlists from raw GitHub repositories into your local Room database.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF24292E))
                                .border(1.dp, colors.accentGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // 2. URL IMPORTER CARD
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, contentDescription = null, tint = colors.accentGlow)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Import from GitHub URL",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }

                            // Paste from clipboard button
                            IconButton(
                                onClick = {
                                    val text = clipboardManager.getText()?.text
                                    if (!text.isNullOrBlank()) {
                                        inputUrl = text
                                    }
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            placeholder = { Text("https://raw.githubusercontent.com/.../playlist.m3u", color = colors.textSecondary) },
                            label = { Text("GitHub M3U / M3U8 URL", color = colors.textSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("github_input_url"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentGlow,
                                unfocusedBorderColor = colors.glassBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            placeholder = { Text("Custom Playlist Title (Optional)", color = colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentGlow,
                                unfocusedBorderColor = colors.glassBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fetch button
                        Button(
                            onClick = {
                                if (inputUrl.isNotBlank()) {
                                    scope.launch {
                                        playlistManager.fetchGitHubPlaylist(inputUrl, inputName)
                                    }
                                }
                            },
                            enabled = !isLoading && inputUrl.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_fetch_github"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = colors.backgroundDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.backgroundDeep, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Parsing GitHub M3U...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetch & Parse M3U", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Status notification message
                        AnimatedVisibility(visible = statusMessage != null) {
                            statusMessage?.let { msg ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.backgroundDeep.copy(alpha = 0.6f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = msg, style = MaterialTheme.typography.labelSmall, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 3. PARSED PLAYLIST PREVIEW (If loaded)
            previewPlaylist?.let { playlist ->
                item {
                    CrystalGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("github_preview_card"),
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.85f),
                        borderColor = colors.accentGlow
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "PARSED M3U PLAYLIST",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.accentGlow
                                    )
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${playlist.totalTracks} Tracks • ${playlist.totalDurationText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }

                                TextButton(onClick = { playlistManager.clearPreview() }) {
                                    Text("Discard", color = colors.textSecondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Accent color choice for Room playlist
                            Text(text = "Choose Playlist Accent Color:", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                accentColors.forEach { hex ->
                                    val parsed = Color(android.graphics.Color.parseColor(hex))
                                    val isSelected = selectedAccentColor == hex
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(parsed)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else colors.glassBorder,
                                                shape = CircleShape
                                            )
                                            .clickable { selectedAccentColor = hex }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action to import directly to Room database
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            playlistManager.importToRoomDatabase(playlist, repository, selectedAccentColor)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_import_room"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentGlow,
                                        contentColor = colors.backgroundDeep
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Quick Import", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showDetailedModal = true },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("btn_inspect_m3u"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.backgroundDeep.copy(alpha = 0.8f),
                                        contentColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.accentGlow)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Inspect & Select", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Sample preview of first 5 tracks
                            Text(
                                text = "Parsed Track Entries (Sample):",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            playlist.tracks.take(6).forEachIndexed { index, track ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.backgroundDeep.copy(alpha = 0.5f))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                        color = colors.accentGlow,
                                        modifier = Modifier.width(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${track.artist} • ${track.group}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.textSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val item = TrackItem(
                                                id = "gh_preview_${index}",
                                                title = track.title,
                                                artist = track.artist,
                                                album = playlist.name,
                                                durationMs = if (track.durationSec > 0) track.durationSec * 1000L else 0L,
                                                uri = track.url
                                            )
                                            onPlayTrack(item)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = colors.accentGlow, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. CURATED GITHUB COMMUNITY PRESETS
            item {
                Text(
                    text = "CURATED GITHUB REPOSITORIES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(playlistManager.presets) { preset ->
                val cardColor = try {
                    Color(android.graphics.Color.parseColor(preset.accentColor))
                } catch (e: Exception) {
                    colors.accentGlow
                }

                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(cardColor.copy(alpha = 0.85f), colors.backgroundDeep)
                                    )
                                )
                                .border(1.dp, cardColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = preset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.genre,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = cardColor
                                )
                                Text(
                                    text = " • ${preset.trackCount} Tracks",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                inputUrl = preset.rawUrl
                                inputName = preset.title
                                scope.launch {
                                    playlistManager.fetchGitHubPlaylist(preset.rawUrl, preset.title)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.backgroundDeep.copy(alpha = 0.8f),
                                contentColor = cardColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, cardColor.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 5. IMPORTED GITHUB PLAYLISTS
            if (importedPlaylists.isNotEmpty()) {
                item {
                    Text(
                        text = "IMPORTED GITHUB COLLECTIONS IN ROOM (${importedPlaylists.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                items(importedPlaylists) { pl ->
                    CrystalGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = colors.glassBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${pl.name} (GitHub)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${pl.totalTracks} Tracks • Room Synced",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }

                            // Re-sync action
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        playlistManager.fetchGitHubPlaylist(pl.sourceUrl, pl.name)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.CloudSync, contentDescription = "Sync", tint = colors.accentGlow)
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive M3U Entry Inspector Dialog
    if (showDetailedModal && previewPlaylist != null) {
        val parsedPlaylist = remember(previewPlaylist) {
            val content = previewPlaylist!!.tracks.joinToString("\n") { track ->
                "#EXTINF:${track.durationSec} group-title=\"${track.group}\",${track.artist} - ${track.title}\n${track.url}"
            }
            M3uParserService.parseM3uContent(content, previewPlaylist!!.name)
        }

        M3uPreviewDialog(
            playlist = parsedPlaylist,
            onDismiss = { showDetailedModal = false },
            onConfirmImport = { playlistName, selectedEntries, colorHex ->
                scope.launch {
                    M3uParserService.saveEntriesToRoomDatabase(playlistName, selectedEntries, repository, colorHex)
                    showDetailedModal = false
                }
            },
            onPreviewPlayTrack = { track ->
                onPlayTrack(track)
            }
        )
    }
}
