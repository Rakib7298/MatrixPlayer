package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.m3u.M3uStreamType
import com.example.media.m3u.ParsedM3uEntry
import com.example.media.streaming.DiscoveredRepoPlaylist
import com.example.media.streaming.StreamHealth
import com.example.media.streaming.UniversalStreamImportEngine
import com.example.model.TrackItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.launch

@Composable
fun UniversalStreamImportScreen(
    importEngine: UniversalStreamImportEngine,
    onPlayTrack: (TrackItem) -> Unit,
    onLaunchFilePicker: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val scope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val state by importEngine.state.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var inputUrl by remember { mutableStateOf("") }
    var rawTextContent by remember { mutableStateOf("") }
    var isRawTextExpanded by remember { mutableStateOf(false) }
    var inputRepo by remember { mutableStateOf("iptv-org/iptv") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val tabs = listOf("M3U / M3U8 Direct", "GitHub Repositories", "Featured Feeds")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("universal_stream_import_screen")
    ) {
        MatrixTopAppBar(
            title = "Stream & Repo Importer",
            onBackClick = onBack
        )

        // Custom M3 Glass Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.backgroundSurface.copy(alpha = 0.6f),
            contentColor = colors.accentGlow,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.accentGlow,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) colors.accentGlow else colors.textSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status Message Pill if present
            if (!state.statusMessage.isNullOrBlank()) {
                item {
                    CrystalGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = colors.accentGlow.copy(alpha = 0.15f),
                        borderColor = colors.accentGlow.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.statusMessage!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // TAB CONTENT
            when (selectedTab) {
                0 -> {
                    // TAB 1: M3U / M3U8 DIRECT (URL / Local File / Raw text)
                    item {
                        CrystalGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = colors.backgroundSurface.copy(alpha = 0.75f),
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
                                        Text("Stream URL or M3U Link", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                    }
                                    IconButton(
                                        onClick = {
                                            val text = clipboardManager.getText()?.text
                                            if (!text.isNullOrBlank()) inputUrl = text
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = inputUrl,
                                    onValueChange = { inputUrl = it },
                                    placeholder = { Text("https://test-streams.mux.dev/.../stream.m3u8", color = colors.textSecondary, fontSize = 13.sp) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("stream_input_url"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accentGlow,
                                        unfocusedBorderColor = colors.glassBorder,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (inputUrl.isNotBlank()) {
                                                scope.launch { importEngine.fetchFromUrl(inputUrl) }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_load_m3u_url"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.accentGlow,
                                            contentColor = Color.Black
                                        ),
                                        enabled = !state.isScanning && inputUrl.isNotBlank()
                                    ) {
                                        if (state.isScanning) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Fetch & Parse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Button(
                                        onClick = onLaunchFilePicker,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_pick_local_m3u"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.backgroundSurface.copy(alpha = 0.8f),
                                            contentColor = colors.accentGlow
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGlow.copy(alpha = 0.5f))
                                    ) {
                                        Icon(Icons.Default.InsertDriveFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Local .m3u File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Raw Paste Section
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isRawTextExpanded = !isRawTextExpanded }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = colors.textSecondary
                                    )
                                    Text(
                                        text = if (isRawTextExpanded) "Hide Paste Text Area" else "Or Paste Raw #EXTM3U Content",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.textSecondary
                                    )
                                }

                                AnimatedVisibility(visible = isRawTextExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        OutlinedTextField(
                                            value = rawTextContent,
                                            onValueChange = { rawTextContent = it },
                                            placeholder = { Text("#EXTM3U\n#EXTINF:-1,Sample Stream\nhttps://...", color = colors.textSecondary, fontSize = 12.sp) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                                .testTag("raw_m3u_text_field"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = colors.accentGlow,
                                                unfocusedBorderColor = colors.glassBorder,
                                                focusedTextColor = colors.textPrimary,
                                                unfocusedTextColor = colors.textPrimary
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                if (rawTextContent.isNotBlank()) {
                                                    importEngine.parseRawText(rawTextContent)
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = colors.accentGlow, contentColor = Color.Black)
                                        ) {
                                            Text("Parse Content Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 2: GITHUB REPO EXPLORER
                    item {
                        CrystalGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = colors.backgroundSurface.copy(alpha = 0.75f),
                            borderColor = colors.glassBorder
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF24292E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("GitHub Repository Scanner", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                        Text("Auto-detect all .m3u & .m3u8 playlists in repository", fontSize = 11.sp, color = colors.textSecondary)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = inputRepo,
                                    onValueChange = { inputRepo = it },
                                    label = { Text("Repo (owner/repo or URL)", color = colors.textSecondary) },
                                    placeholder = { Text("iptv-org/iptv", color = colors.textSecondary) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_github_repo"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = colors.accentGlow,
                                        unfocusedBorderColor = colors.glassBorder,
                                        focusedTextColor = colors.textPrimary,
                                        unfocusedTextColor = colors.textPrimary
                                    ),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (inputRepo.isNotBlank()) {
                                            scope.launch { importEngine.scanGitHubRepository(inputRepo) }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_scan_github_repo"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentGlow,
                                        contentColor = Color.Black
                                    ),
                                    enabled = !state.isScanning && inputRepo.isNotBlank()
                                ) {
                                    if (state.isScanning) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Crawling GitHub Tree...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    } else {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Scan Repository for Playlists", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Show discovered repo files
                    state.repoScanResult?.let { scanRes ->
                        item {
                            Text(
                                text = "DISCOVERED PLAYLISTS IN ${scanRes.owner}/${scanRes.repo} (${scanRes.playlists.size})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.accentGlow,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(scanRes.playlists) { file ->
                            CrystalGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch { importEngine.fetchFromUrl(file.rawDownloadUrl, file.name) }
                                    },
                                shape = RoundedCornerShape(14.dp),
                                backgroundColor = colors.backgroundSurface.copy(alpha = 0.65f),
                                borderColor = colors.glassBorder
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = colors.accentGlow, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                        Text(file.path, fontSize = 10.sp, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Button(
                                        onClick = { scope.launch { importEngine.fetchFromUrl(file.rawDownloadUrl, file.name) } },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentGlow.copy(alpha = 0.2f), contentColor = colors.accentGlow),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 3: FEATURED REPOS
                    item {
                        Text(
                            text = "COMMUNITY OPEN-SOURCE REPOSITORIES",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.accentGlow
                        )
                    }

                    items(importEngine.featuredStreamingRepos) { (repoSlug, description) ->
                        CrystalGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    inputRepo = repoSlug
                                    selectedTab = 1
                                    scope.launch { importEngine.scanGitHubRepository(repoSlug) }
                                },
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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.primaryGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(repoSlug, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.accentGlow)
                                    Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 11.sp)
                                }

                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = colors.accentGlow)
                            }
                        }
                    }
                }
            }

            // ACTIVE PREVIEW SECTION
            state.activePreviewPlaylist?.let { playlist ->
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    CrystalGlassCard(
                        modifier = Modifier.fillMaxWidth(),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.accentGlow
                                    )
                                    Text(
                                        text = "${playlist.entries.size} Channels/Feeds • ${playlist.totalDurationText}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.textSecondary
                                    )
                                }
                                IconButton(onClick = { importEngine.clearPreview() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Preview", tint = colors.textSecondary)
                                }
                            }

                            // Distinct Categories Chips
                            val categories = remember(playlist) {
                                val distinct = playlist.entries.map { it.metadata.groupTitle }.distinct()
                                listOf("All") + distinct
                            }

                            if (categories.size > 2) {
                                LazyRow(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(categories) { cat ->
                                        val isSel = cat == selectedCategoryFilter
                                        CrystalGlassCard(
                                            shape = RoundedCornerShape(10.dp),
                                            backgroundColor = if (isSel) colors.accentGlow.copy(alpha = 0.25f) else colors.backgroundSurface.copy(alpha = 0.4f),
                                            borderColor = if (isSel) colors.accentGlow else colors.glassBorder,
                                            modifier = Modifier.clickable { selectedCategoryFilter = cat }
                                        ) {
                                            Text(
                                                text = cat,
                                                fontSize = 11.sp,
                                                color = if (isSel) colors.accentGlow else colors.textSecondary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Action buttons: Health check & Save to Room
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { scope.launch { importEngine.runHealthCheckOnPreview() } },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.backgroundSurface,
                                        contentColor = Color(0xFF1DE9B6)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1DE9B6).copy(alpha = 0.5f)),
                                    enabled = !state.isCheckingHealth
                                ) {
                                    if (state.isCheckingHealth) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color(0xFF1DE9B6), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Check Health", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            importEngine.importToRoomDatabase(playlist, selectedCategoryFilter)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_save_playlist_room"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentGlow,
                                        contentColor = Color.Black
                                    )
                                ) {
                                    Icon(Icons.Default.QueueMusic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save to Room DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Filtered Preview Entries
                val filtered = if (selectedCategoryFilter == "All") playlist.entries else playlist.entries.filter { it.metadata.groupTitle.equals(selectedCategoryFilter, ignoreCase = true) }

                items(filtered) { entry ->
                    val health = state.healthCheckedResults[entry.id]
                    CrystalGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        backgroundColor = colors.backgroundSurface.copy(alpha = 0.6f),
                        borderColor = colors.glassBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(entry.metadata.streamType.badgeColor).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color(entry.metadata.streamType.badgeColor),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.metadata.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${entry.metadata.artist} • ${entry.metadata.streamType.label}",
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                    if (health != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(health.colorHex).copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(health.label, fontSize = 9.sp, color = Color(health.colorHex), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Play Test Button
                            IconButton(
                                onClick = {
                                    val testTrack = TrackItem(
                                        id = "test_${entry.streamUrl.hashCode()}",
                                        title = entry.metadata.title,
                                        artist = entry.metadata.artist,
                                        album = playlist.name,
                                        durationMs = if (entry.metadata.durationSec > 0) entry.metadata.durationSec * 1000L else 0L,
                                        uri = entry.streamUrl,
                                        isFavorite = false
                                    )
                                    onPlayTrack(testTrack)
                                }
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Test", tint = colors.accentGlow)
                            }
                        }
                    }
                }
            }
        }
    }
}
