package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.streaming.StreamHealth
import com.example.media.streaming.StreamProtocol
import com.example.media.streaming.StreamStation
import com.example.media.streaming.StreamingManager
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.launch

@Composable
fun StreamingSourcesScreen(
    streamingManager: StreamingManager,
    onPlayStation: (StreamStation) -> Unit,
    onNavigateToGitHub: () -> Unit = {},
    onNavigateToHealthChecker: () -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val scope = rememberCoroutineScope()

    val stations by streamingManager.stations.collectAsState()
    val telemetry by streamingManager.telemetry.collectAsState()
    val recentStreams by streamingManager.recentStreams.collectAsState()

    var inputUrl by remember { mutableStateOf("") }
    var inputName by remember { mutableStateOf("") }
    var inputGenre by remember { mutableStateOf("Electronic / Synth") }
    var isCheckingHealth by remember { mutableStateOf(false) }
    var testHealthResult by remember { mutableStateOf<StreamHealth?>(null) }
    var selectedFilter by remember { mutableStateOf("All Stations") }

    val filterOptions = listOf("All Stations", "Favorites", "HLS Live", "Synthwave", "Ambient / Lo-Fi")

    val filteredStations = remember(stations, selectedFilter) {
        when (selectedFilter) {
            "Favorites" -> stations.filter { it.isBookmarked }
            "HLS Live" -> stations.filter { it.protocol == StreamProtocol.HLS }
            "Synthwave" -> stations.filter { it.genre.contains("Synth", ignoreCase = true) || it.genre.contains("EDM", ignoreCase = true) }
            "Ambient / Lo-Fi" -> stations.filter { it.genre.contains("Ambient", ignoreCase = true) || it.genre.contains("Lo-Fi", ignoreCase = true) }
            else -> stations
        }
    }

    // Infinite breathing glow for live streaming badge
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("streaming_sources_screen")
    ) {
        MatrixTopAppBar(
            title = "Streaming & Live Stations",
            onBackClick = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. LIVE TELEMETRY CONTROL PANEL (Super Crystal Liquid Glass 70% transparent)
            item {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stream_telemetry_card"),
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.72f),
                    borderColor = if (telemetry.isStreaming) colors.accentGlow else colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
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
                                        .background(
                                            if (telemetry.isStreaming)
                                                colors.accentGlow.copy(alpha = pulseAlpha)
                                            else Color(0xFF64748B)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (telemetry.isStreaming) "LIVE STREAM ACTIVE" else "STANDBY / READY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (telemetry.isStreaming) colors.accentGlow else colors.textSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.backgroundDeep.copy(alpha = 0.6f))
                                    .border(1.dp, colors.glassBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${telemetry.currentBitrateKbps} KBPS • 44.1 kHz",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = colors.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = telemetry.activeStation?.name ?: "No station selected",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = telemetry.activeStation?.description ?: "Select an online station or enter a custom stream URL below to start playback.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Buffer Bar & Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Buffer Cache: ${(telemetry.bufferPercentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "${telemetry.downloadedBytes / (1024 * 1024)} MB Downloaded",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = colors.accentGlow
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { telemetry.bufferPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.accentGlow,
                            trackColor = colors.backgroundDeep.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // 1.5. GITHUB PLAYLISTS SHORTCUT BANNER
            item {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToGitHub() }
                        .testTag("btn_goto_github_playlists"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = Color(0xFF6E40C9)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF24292E))
                                .border(1.dp, Color(0xFF6E40C9), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GitHub M3U / M3U8 Playlists",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Import curated open source community streams directly to Room",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Button(
                            onClick = { onNavigateToGitHub() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6E40C9),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1.8. LINK HEALTH CHECKER SHORTCUT BANNER
            item {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToHealthChecker() }
                        .testTag("btn_goto_health_checker"),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = Color(0xFF10B981)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF064E3B))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Link Health Checker Pro",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Probe latency (ms), HTTP status codes, and server headers for live feeds",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Button(
                            onClick = { onNavigateToHealthChecker() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Check", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. CUSTOM STREAM DIRECT CONNECT PANEL
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddLink, contentDescription = null, tint = colors.accentGlow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Direct Stream & Custom Endpoint",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = {
                                inputUrl = it
                                testHealthResult = null
                            },
                            placeholder = { Text("https://example.com/live/stream.m3u8", color = colors.textSecondary) },
                            label = { Text("Stream URL (HLS .m3u8, MP3, AAC, DASH)", color = colors.textSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stream_input_url"),
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

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                placeholder = { Text("Station Name", color = colors.textSecondary) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentGlow,
                                    unfocusedBorderColor = colors.glassBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = inputGenre,
                                onValueChange = { inputGenre = it },
                                placeholder = { Text("Genre", color = colors.textSecondary) },
                                modifier = Modifier.weight(0.8f),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colors.accentGlow,
                                    unfocusedBorderColor = colors.glassBorder,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Ping & Connect buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (inputUrl.isNotBlank()) {
                                        isCheckingHealth = true
                                        scope.launch {
                                            testHealthResult = streamingManager.checkStreamHealth(inputUrl)
                                            isCheckingHealth = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.backgroundDeep.copy(alpha = 0.8f),
                                    contentColor = colors.textPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.glassBorder)
                            ) {
                                if (isCheckingHealth) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.accentGlow, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pinging...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Sensors, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.accentGlow)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ping Latency", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    if (inputUrl.isNotBlank()) {
                                        streamingManager.addCustomStream(
                                            name = inputName.ifBlank { "Custom Live Stream" },
                                            url = inputUrl,
                                            genre = inputGenre.ifBlank { "Direct Stream" }
                                        )
                                        val customStation = streamingManager.stations.value.first()
                                        onPlayStation(customStation)
                                        inputUrl = ""
                                        inputName = ""
                                    }
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("btn_connect_stream"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGlow,
                                    contentColor = colors.backgroundDeep
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect Stream", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        AnimatedVisibility(visible = testHealthResult != null) {
                            testHealthResult?.let { health ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(health.colorHex).copy(alpha = 0.15f))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(health.colorHex), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Stream Response: ${health.label}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(health.colorHex),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. CATEGORY FILTER TABS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) colors.accentGlow else colors.backgroundSurface.copy(alpha = 0.6f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) colors.accentGlow else colors.glassBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) colors.backgroundDeep else colors.textPrimary
                            )
                        }
                    }
                }
            }

            // 4. STATIONS LIST
            items(filteredStations) { station ->
                val isActive = telemetry.activeStation?.id == station.id && telemetry.isStreaming

                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("station_card_${station.id}"),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = if (isActive)
                        colors.backgroundSurface.copy(alpha = 0.85f)
                    else
                        colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = if (isActive) colors.accentGlow else colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Protocol avatar badge
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            Color(station.protocol.badgeColor),
                                            Color(0xFF0F172A)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (station.protocol == StreamProtocol.HLS) Icons.Default.Sensors else Icons.Default.Radio,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        // Station Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = station.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                // Health pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(station.health.colorHex).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = station.health.label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color(station.health.colorHex)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = station.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = station.protocol.label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(station.protocol.badgeColor)
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${station.bitrateKbps} kbps",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = station.genre,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = colors.textSecondary
                                )
                            }
                        }

                        // Bookmark button
                        IconButton(
                            onClick = { streamingManager.toggleBookmark(station.id) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (station.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (station.isBookmarked) colors.accentGlow else colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Play Button
                        IconButton(
                            onClick = {
                                onPlayStation(station)
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) colors.accentGlow else colors.backgroundDeep.copy(alpha = 0.7f)
                                )
                        ) {
                            Icon(
                                imageVector = if (isActive) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                contentDescription = "Play Station",
                                tint = if (isActive) colors.backgroundDeep else colors.accentGlow,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
