package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.lyrics.LyricsParser
import com.example.model.TrackItem
import com.example.ui.theme.LocalMatrixColors

@Composable
fun RealtimeLyricsView(
    track: TrackItem?,
    currentPositionMs: Long,
    lyricsContent: String?,
    isGenerating: Boolean,
    onGenerateWithAi: () -> Unit,
    onSeekToTimestamp: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val parsedLyrics = remember(lyricsContent, track) {
        if (!lyricsContent.isNullOrBlank()) {
            LyricsParser.parse(lyricsContent, track?.title ?: "Unknown", track?.artist ?: "Unknown")
        } else null
    }

    val activeIndex = parsedLyrics?.activeLineIndex(currentPositionMs) ?: -1
    val listState = rememberLazyListState()

    // Smooth auto-scroll to center the active lyric line
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            val targetScroll = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundDeep.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .testTag("realtime_lyrics_view")
    ) {
        // Header Row: Song Title + AI Assistant Button + Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.accentGlow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = colors.accentGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Real-Time Synchronized Lyrics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.accentGlow
                    )
                    Text(
                        text = "${track?.title ?: "No Track"} • ${track?.artist ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onGenerateWithAi,
                    enabled = !isGenerating,
                    modifier = Modifier.testTag("btn_ai_regenerate_lyrics")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colors.accentGlow,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Generate Lyrics",
                            tint = colors.accentGlow
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_lyrics")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Lyrics",
                        tint = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content: Lyrics List or AI Generation Prompt
        if (parsedLyrics != null && parsedLyrics.lines.isNotEmpty()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("lyrics_list"),
                contentPadding = PaddingValues(top = 40.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(parsedLyrics.lines) { index, line ->
                    val isActive = index == activeIndex
                    val isPast = index < activeIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isActive) colors.accentGlow.copy(alpha = 0.18f)
                                else Color.Transparent
                            )
                            .clickable { onSeekToTimestamp(line.timestampMs) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = line.text,
                            style = if (isActive) {
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp
                                )
                            } else {
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                )
                            },
                            color = when {
                                isActive -> colors.accentGlow
                                isPast -> colors.textPrimary.copy(alpha = 0.5f)
                                else -> colors.textSecondary
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (isActive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.accentGlow)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = line.timestampFormatted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Empty State: AI Lyric Assistant Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
                    borderColor = colors.accentGlow.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colors.accentGlow.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accentGlow,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "AI Lyric Assistant",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No synchronized lyrics found for \"${track?.title ?: "this song"}\".\nLet Gemini AI generate complete, time-synced real-time lyrics in online mode!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onGenerateWithAi,
                            enabled = !isGenerating,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGlow,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_generate_lyrics_action")
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.Black,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "AI Generating Synchronized Lyrics...",
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✨ Generate Live Lyrics with Gemini",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
