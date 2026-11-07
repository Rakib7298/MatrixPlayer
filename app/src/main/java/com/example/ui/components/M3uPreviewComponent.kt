package com.example.ui.components

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.media.m3u.ParsedM3uEntry
import com.example.media.m3u.ParsedM3uPlaylist
import com.example.model.TrackItem
import com.example.ui.theme.LocalMatrixColors

@Composable
fun M3uPreviewDialog(
    playlist: ParsedM3uPlaylist,
    onDismiss: () -> Unit,
    onConfirmImport: (playlistName: String, selectedEntries: List<ParsedM3uEntry>, accentColorHex: String) -> Unit,
    onPreviewPlayTrack: (TrackItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val accentColors = listOf("#00F0FF", "#1DE9B6", "#B026FF", "#E040FB", "#FF5252", "#F59E0B")

    var playlistTitle by remember { mutableStateOf(playlist.name) }
    var selectedAccentColor by remember { mutableStateOf(accentColors[0]) }
    val entryStates = remember { mutableStateListOf(*playlist.entries.toTypedArray()) }
    var editingEntryIndex by remember { mutableStateOf<Int?>(null) }

    val selectedCount = entryStates.count { it.isSelected }

    Dialog(onDismissRequest = onDismiss) {
        CrystalGlassCard(
            modifier = modifier
                .fillMaxWidth()
                .height(580.dp)
                .testTag("m3u_preview_dialog"),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = colors.backgroundSurface.copy(alpha = 0.95f),
            borderColor = colors.accentGlow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "M3U Entry Preview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Review EXTINF metadata & select entries before database insert",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Editable Playlist Title Input
                OutlinedTextField(
                    value = playlistTitle,
                    onValueChange = { playlistTitle = it },
                    label = { Text("Playlist Name for Room DB", color = colors.textSecondary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("m3u_preview_playlist_name"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentGlow,
                        unfocusedBorderColor = colors.glassBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Accent Color & Select All Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        accentColors.forEach { hex ->
                            val parsed = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedAccentColor == hex
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(parsed)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else colors.glassBorder,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedAccentColor = hex }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            val allSelected = entryStates.all { it.isSelected }
                            for (i in entryStates.indices) {
                                entryStates[i] = entryStates[i].copy(isSelected = !allSelected)
                            }
                        }
                    ) {
                        Icon(Icons.Default.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.accentGlow)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (entryStates.all { it.isSelected }) "Deselect All" else "Select All",
                            fontSize = 12.sp,
                            color = colors.accentGlow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Entries Count Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Selected Entries: $selectedCount / ${entryStates.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.accentGlow
                    )
                    Text(
                        text = playlist.totalDurationText,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Entries List with EXTINF details, checkbox, and edit modal
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(entryStates) { index, entry ->
                        CrystalGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            backgroundColor = if (entry.isSelected)
                                colors.backgroundDeep.copy(alpha = 0.7f)
                            else
                                colors.backgroundDeep.copy(alpha = 0.3f),
                            borderColor = if (entry.isSelected) colors.glassBorder else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = entry.isSelected,
                                    onCheckedChange = { checked ->
                                        entryStates[index] = entry.copy(isSelected = checked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = colors.accentGlow,
                                        uncheckedColor = colors.textSecondary
                                    )
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = entry.metadata.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))

                                        // Badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(entry.metadata.streamType.badgeColor).copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = entry.metadata.streamType.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                color = Color(entry.metadata.streamType.badgeColor)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${entry.metadata.artist} • ${entry.metadata.groupTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = entry.streamUrl,
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                                        color = colors.textSecondary.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Quick Preview Play Button
                                IconButton(
                                    onClick = {
                                        onPreviewPlayTrack(
                                            TrackItem(
                                                id = entry.id,
                                                title = entry.metadata.title,
                                                artist = entry.metadata.artist,
                                                album = playlistTitle,
                                                durationMs = if (entry.metadata.durationSec > 0) entry.metadata.durationSec * 1000L else 0L,
                                                uri = entry.streamUrl
                                            )
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Test Play", tint = colors.accentGlow, modifier = Modifier.size(20.dp))
                                }

                                // Inline Edit Metadata Button
                                IconButton(
                                    onClick = { editingEntryIndex = index },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Entry", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Import Button
                Button(
                    onClick = {
                        val validTitle = if (playlistTitle.isNotBlank()) playlistTitle.trim() else "M3U Playlist"
                        onConfirmImport(validTitle, entryStates.toList(), selectedAccentColor)
                    },
                    enabled = selectedCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_confirm_m3u_import"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentGlow,
                        contentColor = colors.backgroundDeep
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add $selectedCount Entries to Room Database", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // In-line Edit Subdialog
    editingEntryIndex?.let { index ->
        if (index in entryStates.indices) {
            val entry = entryStates[index]
            var editTitle by remember { mutableStateOf(entry.metadata.title) }
            var editArtist by remember { mutableStateOf(entry.metadata.artist) }
            var editGroup by remember { mutableStateOf(entry.metadata.groupTitle) }

            Dialog(onDismissRequest = { editingEntryIndex = null }) {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.95f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Edit EXTINF Entry Metadata",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            label = { Text("Title", color = colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editArtist,
                            onValueChange = { editArtist = it },
                            label = { Text("Artist", color = colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editGroup,
                            onValueChange = { editGroup = it },
                            label = { Text("Group Category", color = colors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingEntryIndex = null }) {
                                Text("Cancel", color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val updatedMeta = entry.metadata.copy(
                                        title = editTitle.ifBlank { "Stream Track" },
                                        artist = editArtist.ifBlank { "Online Stream" },
                                        groupTitle = editGroup.ifBlank { "M3U Stream" }
                                    )
                                    entryStates[index] = entry.copy(metadata = updatedMeta)
                                    editingEntryIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGlow, contentColor = colors.backgroundDeep),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
