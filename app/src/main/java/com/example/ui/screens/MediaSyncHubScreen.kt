package com.example.ui.screens

import android.net.Uri
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.sync.AutoMediaSyncEngine
import com.example.media.sync.MediaSyncStatus
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaSyncHubScreen(
    syncEngine: AutoMediaSyncEngine,
    onLaunchFolderPicker: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val report by syncEngine.syncReport.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("media_sync_hub_screen")
    ) {
        MatrixTopAppBar(
            title = "Auto Media Sync",
            onBackClick = onBack,
            actions = {
                IconButton(
                    onClick = { syncEngine.triggerSyncNow() },
                    modifier = Modifier.testTag("btn_trigger_sync_top")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync Now",
                        tint = colors.accentGlow
                    )
                }
            }
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. RADAR / SYNC STATUS HERO
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
                    borderColor = if (report.status == MediaSyncStatus.SCANNING) colors.accentGlow else colors.glassBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (report.status == MediaSyncStatus.SCANNING) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .scale(pulseScale)
                                        .clip(CircleShape)
                                        .background(colors.accentGlow.copy(alpha = 0.2f))
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(colors.primaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (report.status == MediaSyncStatus.SCANNING) Icons.Default.Sync else Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (report.status == MediaSyncStatus.SCANNING) "Synchronizing Local Storage..." else "Real-Time Media Synchronized",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        Text(
                            text = report.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { syncEngine.triggerSyncNow() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_sync_now"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGlow,
                                    contentColor = Color.Black
                                ),
                                enabled = report.status != MediaSyncStatus.SCANNING
                            ) {
                                if (report.status == MediaSyncStatus.SCANNING) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scanning...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Force Resync", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = onLaunchFolderPicker,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_add_saf_folder"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.backgroundSurface.copy(alpha = 0.9f),
                                    contentColor = colors.accentGlow
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGlow.copy(alpha = 0.6f))
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Custom Folder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. METRICS OVERVIEW CARDS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Audio card
                    CrystalGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = colors.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Audio Files", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${report.totalAudioCount}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text("Music & Sounds", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }

                    // Video card
                    CrystalGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = colors.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFE040FB), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Video Files", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${report.totalVideoCount}",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text("HD / 4K Clips", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }

                    // Total Size card
                    CrystalGlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(18.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                        borderColor = colors.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = Color(0xFF1DE9B6), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Indexed Size", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = report.totalSizeText,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text("${report.foldersSummary.size} Folders", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }
                }
            }

            // 3. AUTO-SYNC TOGGLE BANNER
            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.7f),
                    borderColor = colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentGlow.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Real-Time MediaStore Observer",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Auto-detects newly downloaded music, camera recordings, and saved media",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = report.isAutoSyncActive,
                            onCheckedChange = { syncEngine.setAutoSyncEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = colors.accentGlow,
                                uncheckedThumbColor = colors.textSecondary,
                                uncheckedTrackColor = colors.glassBorder
                            )
                        )
                    }
                }
            }

            // 4. DISCOVERED MEDIA FOLDERS LIST
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INDEXED STORAGE DIRECTORIES (${report.foldersSummary.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.accentGlow
                    )
                    if (report.lastSyncTimestamp > 0) {
                        val formattedDate = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(report.lastSyncTimestamp))
                        Text(
                            text = "Last Synced: $formattedDate",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            items(report.foldersSummary) { folder ->
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
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
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (folder.isCustomSafFolder) Color(0xFF1DE9B6).copy(alpha = 0.2f)
                                    else Color(0xFFFFB300).copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (folder.isCustomSafFolder) Icons.Default.SdCard else Icons.Default.Folder,
                                contentDescription = null,
                                tint = if (folder.isCustomSafFolder) Color(0xFF1DE9B6) else Color(0xFFFFB300),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = folder.path,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 11.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${folder.audioCount} audio • ${folder.videoCount} video",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentGlow
                            )
                            Text(
                                text = folder.totalSizeMbText,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
