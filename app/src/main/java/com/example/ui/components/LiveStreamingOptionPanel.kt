package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.streaming.StreamHealth
import com.example.media.streaming.StreamProtocol
import com.example.media.streaming.StreamStation
import com.example.model.TrackItem
import com.example.model.VideoItem
import com.example.ui.theme.LocalMatrixColors

enum class StreamingPanelMode(val label: String) {
    CHANNELS("Live Channels"),
    QUICK_URL("Direct Stream"),
    GITHUB("GitHub Repos")
}

/**
 * Advanced Modern Option Panel for Streaming Live Media (M3U, M3U8, GitHub Repos).
 * Features:
 * - Live Channel Icons & Media Thumbnails (via Coil with chromatic fallbacks)
 * - Grid & List View Toggle
 * - Real-time stream health badges (latency, protocol, resolution)
 * - Direct playback into ExoPlayer (Audio or FullScreen Video Player)
 * - Instant GitHub repository scanning & M3U parsing
 */
@Composable
fun LiveStreamingOptionPanel(
    stations: List<StreamStation>,
    onPlayTrack: (TrackItem) -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onOpenLocalFilePicker: () -> Unit = {},
    onNavigateToUniversalImport: () -> Unit = {},
    onNavigateToGitHub: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    var selectedMode by remember { mutableStateOf(StreamingPanelMode.CHANNELS) }
    var isGridView by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    // Quick URL Input state
    var quickStreamUrl by remember { mutableStateOf("") }
    var quickStreamTitle by remember { mutableStateOf("") }
    var quickStreamLogoUrl by remember { mutableStateOf("") }
    var quickStreamIsVideo by remember { mutableStateOf(true) }

    // Pulse animation for LIVE badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Category options
    val categories = remember(stations) {
        val list = stations.map { it.genre.split("/").first().trim() }.distinct().filter { it.isNotBlank() }
        listOf("All") + list
    }

    val filteredStations = remember(stations, searchQuery, selectedCategory) {
        var result = stations
        if (selectedCategory != "All") {
            result = result.filter { it.genre.contains(selectedCategory, ignoreCase = true) }
        }
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.genre.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
        result
    }

    CrystalGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_streaming_option_panel"),
        shape = RoundedCornerShape(18.dp),
        backgroundColor = colors.backgroundSurface.copy(alpha = 0.85f),
        borderColor = colors.accentGlow.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Live Radar Badge + Title + Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = pulseAlpha))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE STREAMING STUDIO",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        ),
                        color = colors.textPrimary
                    )
                }

                // Grid / List toggle
                if (selectedMode == StreamingPanelMode.CHANNELS) {
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_toggle_stream_layout")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle Grid/List",
                            tint = colors.accentGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StreamingPanelMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) colors.accentGlow.copy(alpha = 0.25f)
                                else colors.backgroundDeep.copy(alpha = 0.6f)
                            )
                            .border(
                                0.8.dp,
                                if (isSelected) colors.accentGlow else colors.glassBorder.copy(alpha = 0.4f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedMode = mode }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 9.5.sp
                            ),
                            color = if (isSelected) colors.accentGlow else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedMode) {
                StreamingPanelMode.CHANNELS -> {
                    // Category Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSel = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSel) colors.accentGlow.copy(alpha = 0.3f)
                                        else colors.backgroundDeep.copy(alpha = 0.5f)
                                    )
                                    .border(
                                        0.5.dp,
                                        if (isSel) colors.accentGlow else colors.glassBorder.copy(alpha = 0.3f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (isSel) colors.accentGlow else colors.textSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Channel Items (Grid or List)
                    if (isGridView) {
                        // Compact 2-column channel grid
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredStations.chunked(2).forEach { rowStations ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowStations.forEach { station ->
                                        StreamGridCard(
                                            station = station,
                                            onPlay = {
                                                playStream(station, onPlayTrack, onPlayVideo)
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowStations.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        // Elegant stream list
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            filteredStations.take(6).forEach { station ->
                                StreamListCard(
                                    station = station,
                                    onPlay = {
                                        playStream(station, onPlayTrack, onPlayVideo)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bottom Action Row (Import Local M3U, Universal Importer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenLocalFilePicker,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.backgroundDeep,
                                contentColor = colors.accentGlow
                            ),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.accentGlow.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Local .m3u", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onNavigateToUniversalImport,
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import Full M3U8", fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                StreamingPanelMode.QUICK_URL -> {
                    // Direct Stream URL Launcher
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = quickStreamUrl,
                            onValueChange = { quickStreamUrl = it },
                            label = { Text("Stream URL (.m3u8, .m3u, .mp4, .mp3)", color = colors.textSecondary, fontSize = 10.sp) },
                            placeholder = { Text("https://example.com/live/channel.m3u8", color = colors.textSecondary.copy(alpha = 0.5f), fontSize = 10.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_quick_stream_url"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.accentGlow,
                                unfocusedBorderColor = colors.glassBorder,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = quickStreamTitle,
                                onValueChange = { quickStreamTitle = it },
                                label = { Text("Channel Name", color = colors.textSecondary, fontSize = 10.sp) },
                                placeholder = { Text("My Live Channel", color = colors.textSecondary.copy(alpha = 0.5f), fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentGlow,
                                    unfocusedBorderColor = colors.glassBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )

                            OutlinedTextField(
                                value = quickStreamLogoUrl,
                                onValueChange = { quickStreamLogoUrl = it },
                                label = { Text("Logo/Thumbnail URL", color = colors.textSecondary, fontSize = 10.sp) },
                                placeholder = { Text("https://.../logo.png", color = colors.textSecondary.copy(alpha = 0.5f), fontSize = 10.sp) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentGlow,
                                    unfocusedBorderColor = colors.glassBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                )
                            )
                        }

                        // Logo preview thumbnail if URL entered
                        if (quickStreamLogoUrl.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text("Icon Preview:", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                MediaThumbnailView(
                                    url = quickStreamLogoUrl,
                                    title = quickStreamTitle.ifBlank { "Channel" },
                                    modifier = Modifier.size(36.dp),
                                    mediaType = ThumbnailMediaType.CHANNEL_STREAM,
                                    showLiveBadge = true
                                )
                            }
                        }

                        // Stream Player Mode Switch (Video HLS Player vs Audio Stream Player)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (quickStreamIsVideo) "Playback: FullScreen Video Engine (HLS/Adaptive)" else "Playback: Real Audio & Visualizer Engine",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = colors.accentGlow
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.backgroundDeep)
                                    .border(0.5.dp, colors.accentGlow, RoundedCornerShape(6.dp))
                                    .clickable { quickStreamIsVideo = !quickStreamIsVideo }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (quickStreamIsVideo) "Switch to Audio" else "Switch to Video",
                                    fontSize = 9.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (quickStreamUrl.isNotBlank()) {
                                    val name = quickStreamTitle.ifBlank { "Live Stream" }
                                    if (quickStreamIsVideo) {
                                        onPlayVideo(
                                            VideoItem(
                                                id = "quick_live_${System.currentTimeMillis()}",
                                                title = name,
                                                durationMs = 0L,
                                                resolution = "LIVE HLS",
                                                sizeMb = 0.0,
                                                uri = quickStreamUrl,
                                                thumbnailUri = quickStreamLogoUrl.ifBlank { null }
                                            )
                                        )
                                    } else {
                                        onPlayTrack(
                                            TrackItem(
                                                id = "quick_live_${System.currentTimeMillis()}",
                                                title = name,
                                                artist = "Online Broadcast",
                                                album = "Live Stream",
                                                durationMs = 0L,
                                                uri = quickStreamUrl,
                                                albumArtUri = quickStreamLogoUrl.ifBlank { null }
                                            )
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .testTag("btn_launch_quick_stream"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = Color.Black
                            ),
                            enabled = quickStreamUrl.isNotBlank()
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Launch Live Stream Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                StreamingPanelMode.GITHUB -> {
                    // GitHub Repo Explorer Card
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Import live IPTV & radio streams directly from public GitHub repositories without downloading manual files:",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = colors.textSecondary
                        )

                        val popularRepos = listOf(
                            Pair("iptv-org/iptv", "IPTV-Org: 8,000+ Global TV Channels with Logos"),
                            Pair("free-radio-directory/stations", "Free Radio: Curated Global Stations"),
                            Pair("matrix-player/playlists", "Matrix Cyberwave & Lo-Fi 24/7 Streams")
                        )

                        popularRepos.forEach { (repo, desc) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.backgroundDeep.copy(alpha = 0.6f))
                                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToGitHub() }
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    MediaThumbnailView(
                                        url = null,
                                        title = repo,
                                        modifier = Modifier.size(28.dp),
                                        mediaType = ThumbnailMediaType.GITHUB_REPO
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(repo, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp), color = colors.accentGlow)
                                        Text(desc, style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp), color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        Button(
                            onClick = onNavigateToGitHub,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF24292E),
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, colors.accentGlow.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open GitHub Stream Explorer", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamListCard(
    station: StreamStation,
    onPlay: () -> Unit
) {
    val colors = LocalMatrixColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.backgroundDeep.copy(alpha = 0.6f))
            .border(0.6.dp, colors.glassBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable { onPlay() }
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Media Thumbnail
            MediaThumbnailView(
                url = station.logoUrl,
                title = station.name,
                modifier = Modifier.size(38.dp),
                mediaType = if (station.protocol == StreamProtocol.HLS) ThumbnailMediaType.VIDEO else ThumbnailMediaType.CHANNEL_STREAM,
                showLiveBadge = true,
                badgeText = if (station.protocol == StreamProtocol.HLS) "HLS" else "LIVE"
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Health Indicator Dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(station.health.colorHex))
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.genre,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(" • ", fontSize = 9.sp, color = colors.textSecondary)
                    Text(
                        text = "${station.bitrateKbps} kbps",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontFamily = FontFamily.Monospace),
                        color = colors.accentGlow
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play Button
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.accentGlow)
                    .clickable { onPlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play ${station.name}",
                    tint = colors.backgroundDeep,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StreamGridCard(
    station: StreamStation,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundDeep.copy(alpha = 0.7f))
            .border(0.6.dp, colors.glassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Channel Logo / Media Thumbnail
            MediaThumbnailView(
                url = station.logoUrl,
                title = station.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                mediaType = if (station.protocol == StreamProtocol.HLS) ThumbnailMediaType.VIDEO else ThumbnailMediaType.CHANNEL_STREAM,
                showLiveBadge = true,
                badgeText = if (station.protocol == StreamProtocol.HLS) "HLS" else "LIVE"
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = station.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp
                ),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = station.genre,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${station.bitrateKbps}k",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.accentGlow
                )

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(colors.accentGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = colors.backgroundDeep,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

private fun playStream(
    station: StreamStation,
    onPlayTrack: (TrackItem) -> Unit,
    onPlayVideo: (VideoItem) -> Unit
) {
    if (station.protocol == StreamProtocol.HLS || station.genre.contains("HLS", ignoreCase = true) || station.url.contains(".m3u8")) {
        // Stream as Video HLS in FullScreen Video Player
        onPlayVideo(
            VideoItem(
                id = station.id,
                title = station.name,
                durationMs = 0L,
                resolution = "LIVE HLS",
                sizeMb = 0.0,
                uri = station.url,
                thumbnailUri = station.logoUrl
            )
        )
    } else {
        // Stream as Audio Live Radio in Music Player with Visualizer
        onPlayTrack(
            TrackItem(
                id = station.id,
                title = station.name,
                artist = station.genre,
                album = "Live Stream",
                durationMs = 0L,
                uri = station.url,
                albumArtUri = station.logoUrl
            )
        )
    }
}
