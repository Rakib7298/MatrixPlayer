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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.ai.AiModelOption
import com.example.media.ai.ChatMessage
import com.example.media.ai.MatrixAiChatEngine
import com.example.media.ai.MessageSender
import com.example.model.TrackItem
import com.example.model.VideoItem
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.launch

@Composable
fun AiChatScreen(
    aiEngine: MatrixAiChatEngine,
    currentTrack: TrackItem?,
    currentVideo: VideoItem?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by aiEngine.messages.collectAsState()
    val isGenerating by aiEngine.isGenerating.collectAsState()
    val selectedModel by aiEngine.selectedModel.collectAsState()

    var inputPrompt by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "🎵 Summarize Current Media" to "Summarize the currently playing audio/video content and extract key themes.",
        "🔍 Find Similar Content" to "Recommend similar audio tracks, artists, and videos based on my current playing track.",
        "💬 Generate Subtitles" to "Generate dual-language subtitle cues and English/Bengali transcriptions.",
        "📡 Source M3U / M3U8 Streams" to "Find fresh M3U / M3U8 live radio stream URLs and open-source GitHub media repos.",
        "⚡ Link Health Scan" to "Advise on stream link health, HTTP codes, and buffer latency diagnosis."
    )

    // Scroll to latest message on change
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_chat_screen")
    ) {
        MatrixTopAppBar(
            title = "Matrix AI Studio Assistant",
            onBackClick = onBack
        )

        // Model Selector Header Pill Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Model:", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)

            AiModelOption.values().forEach { option ->
                val isSelected = selectedModel == option
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) Color(option.badgeColor) else colors.backgroundSurface.copy(alpha = 0.6f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.White else colors.glassBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { aiEngine.selectModel(option) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                        color = if (isSelected) colors.backgroundDeep else colors.textPrimary
                    )
                }
            }

            IconButton(
                onClick = { aiEngine.clearChatHistory() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = colors.textSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Active Playing Media Pill Indicator
        (currentTrack ?: currentVideo)?.let { media ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.backgroundSurface.copy(alpha = 0.5f))
                    .border(1.dp, colors.glassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (currentTrack != null) Icons.Default.MusicNote else Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = colors.accentGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Context Active: ${currentTrack?.title ?: currentVideo?.title}",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.accentGlow,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Chat Messages Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == MessageSender.USER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.primaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    CrystalGlassCard(
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .testTag("chat_bubble_${msg.id}"),
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isUser) 18.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 18.dp
                        ),
                        backgroundColor = if (isUser)
                            colors.accentGlow.copy(alpha = 0.25f)
                        else
                            colors.backgroundSurface.copy(alpha = 0.85f),
                        borderColor = if (isUser) colors.accentGlow else colors.glassBorder
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (!isUser) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Matrix AI (${msg.modelName})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = colors.accentGlow
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            Text(
                                text = msg.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary
                            )
                        }
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accentGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = colors.backgroundDeep, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Quick Action Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { (label, promptText) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.backgroundSurface.copy(alpha = 0.7f))
                        .border(1.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            aiEngine.sendMessage(promptText, currentTrack, currentVideo)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Prompt Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputPrompt,
                onValueChange = { inputPrompt = it },
                placeholder = { Text("Ask Matrix AI (summary, similar, M3U, subtitles)...", color = colors.textSecondary, fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_prompt_input"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentGlow,
                    unfocusedBorderColor = colors.glassBorder,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputPrompt.isNotBlank()) {
                        val text = inputPrompt
                        inputPrompt = ""
                        aiEngine.sendMessage(text, currentTrack, currentVideo)
                    }
                },
                enabled = !isGenerating && inputPrompt.isNotBlank(),
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputPrompt.isNotBlank()) colors.accentGlow else colors.backgroundSurface.copy(alpha = 0.6f))
                    .testTag("btn_send_ai_prompt")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = colors.backgroundDeep, strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (inputPrompt.isNotBlank()) colors.backgroundDeep else colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
