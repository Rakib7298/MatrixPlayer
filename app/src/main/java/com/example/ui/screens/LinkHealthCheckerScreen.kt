package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import com.example.media.streaming.LinkHealthCheckerEngine
import com.example.media.streaming.LinkHealthResult
import com.example.media.streaming.StreamHealth
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.launch

@Composable
fun LinkHealthCheckerScreen(
    checkerEngine: LinkHealthCheckerEngine,
    repository: MediaRepository,
    onPlayTrack: (TrackItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val isScanning by checkerEngine.isScanning.collectAsState()
    val report by checkerEngine.currentReport.collectAsState()

    var inputUrl by remember { mutableStateOf("") }
    var singleResult by remember { mutableStateOf<LinkHealthResult?>(null) }
    var isCheckingSingle by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("link_health_checker_screen")
    ) {
        MatrixTopAppBar(
            title = "Link Health Checker",
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
                                    text = "NETWORK LATENCY & DIAGNOSTICS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accentGlow
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Stream Link Health Prober",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Probe HTTP headers, latency (ms), MIME type, and protocol validity for M3U/M3U8 feeds and audio/video endpoints.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            // 2. SINGLE LINK PROBE CARD
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
                                    text = "Probe Single Stream Endpoint",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                            }

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
                            onValueChange = {
                                inputUrl = it
                                singleResult = null
                            },
                            placeholder = { Text("https://stream.nightride.fm/synthwave.mp3", color = colors.textSecondary) },
                            label = { Text("Stream URL / M3U8 Link", color = colors.textSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("health_input_url"),
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

                        Button(
                            onClick = {
                                if (inputUrl.isNotBlank()) {
                                    isCheckingSingle = true
                                    scope.launch {
                                        singleResult = checkerEngine.checkSingleLink(inputUrl)
                                        isCheckingSingle = false
                                    }
                                }
                            },
                            enabled = !isCheckingSingle && inputUrl.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_probe_health"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = colors.backgroundDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isCheckingSingle) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = colors.backgroundDeep, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Probing Network Latency...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Run Health Probe", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Single Probe Diagnostic Result Card
                        singleResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            CrystalGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = colors.backgroundDeep.copy(alpha = 0.8f),
                                borderColor = Color(res.healthStatus.colorHex)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (res.isReachable) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(res.healthStatus.colorHex),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = res.healthStatus.label,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color(res.healthStatus.colorHex)
                                            )
                                        }

                                        Text(
                                            text = "HTTP ${res.httpCode}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                            color = colors.textPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(text = "Title: ${res.title}", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                                    Text(text = "MIME Content: ${res.contentType}", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                                    Text(text = "Protocol: ${res.protocol.label}", style = MaterialTheme.typography.labelSmall, color = Color(res.protocol.badgeColor))
                                    Text(text = "Latency: ${res.latencyMs} ms", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace), color = colors.accentGlow)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = {
                                            val item = TrackItem(
                                                id = "health_test_${System.currentTimeMillis()}",
                                                title = res.title,
                                                artist = res.protocol.label,
                                                album = "Health Probed Feed",
                                                durationMs = 0L,
                                                uri = res.url
                                            )
                                            onPlayTrack(item)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGlow, contentColor = colors.backgroundDeep),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Play Stream in Player", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. BATCH SCAN CARD
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
                            Text(
                                text = "Batch Preset Diagnostic Scan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )

                            Button(
                                onClick = {
                                    scope.launch {
                                        val pairs = checkerEngine.defaultEndpointsToTest.map { it.url to it.name }
                                        checkerEngine.runBatchScan(pairs)
                                    }
                                },
                                enabled = !isScanning,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.accentGlow, contentColor = colors.backgroundDeep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_run_batch_health")
                            ) {
                                if (isScanning) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = colors.backgroundDeep, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Run Batch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (report.totalTested > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Online: ${report.onlineCount} / ${report.totalTested}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = "Offline: ${report.offlineCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFEF4444)
                                )
                                Text(
                                    text = "Avg Ping: ${report.avgLatencyMs} ms",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                    color = colors.accentGlow
                                )
                            }
                        }
                    }
                }
            }

            // 4. BATCH RESULTS LIST
            if (report.results.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAGNOSTIC RESULTS (${report.results.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textSecondary
                        )

                        TextButton(
                            onClick = {
                                scope.launch {
                                    checkerEngine.importHealthyLinksToRoom(report.results.filter { it.isReachable }, repository)
                                }
                            }
                        ) {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.accentGlow)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Healthy to Room", fontSize = 12.sp, color = colors.accentGlow)
                        }
                    }
                }

                items(report.results) { res ->
                    CrystalGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = Color(res.healthStatus.colorHex)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(res.healthStatus.colorHex))
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = res.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${res.latencyMs} ms",
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                        color = Color(res.healthStatus.colorHex)
                                    )
                                }

                                Text(
                                    text = "${res.protocol.label} • ${res.contentType} • HTTP ${res.httpCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = {
                                    val item = TrackItem(
                                        id = "health_batch_${res.url.hashCode()}",
                                        title = res.title,
                                        artist = res.protocol.label,
                                        album = "Probed Feed",
                                        durationMs = 0L,
                                        uri = res.url
                                    )
                                    onPlayTrack(item)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = colors.accentGlow)
                            }
                        }
                    }
                }
            }
        }
    }
}
