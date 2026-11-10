package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class ThumbnailMediaType {
    CHANNEL_STREAM,
    VIDEO,
    AUDIO,
    PLAYLIST,
    GITHUB_REPO
}

/**
 * Modern Media Thumbnail & Channel Icon Component.
 * Seamlessly displays:
 * 1. Remote Channel Logos (tvg-logo from M3U / Web URLs) via Coil with caching & crossfade.
 * 2. Local Video thumbnails via VideoThumbnailCache & MediaMetadataRetriever.
 * 3. Audio Album Art or chromatic fallback badges.
 * 4. Distinctive gradient glass fallback with first-letter monogram and stream type badge.
 */
@Composable
fun MediaThumbnailView(
    url: String?,
    title: String,
    modifier: Modifier = Modifier,
    mediaType: ThumbnailMediaType = ThumbnailMediaType.CHANNEL_STREAM,
    shape: Shape = RoundedCornerShape(10.dp),
    showLiveBadge: Boolean = false,
    badgeText: String? = null,
    contentScale: ContentScale = ContentScale.Crop
) {
    val colors = LocalMatrixColors.current
    val context = LocalContext.current

    // Determine if URL is a remote web image/logo or local video URI
    val isWebUrl = remember(url) {
        !url.isNullOrBlank() && (url.startsWith("http://") || url.startsWith("https://"))
    }
    val isLocalVideo = remember(url, mediaType) {
        !url.isNullOrBlank() && (mediaType == ThumbnailMediaType.VIDEO || url.endsWith(".mp4") || url.endsWith(".mkv") || url.contains("video"))
    }

    // Dynamic gradient palette based on title hash for unique visual identity
    val dynamicGradient = remember(title, mediaType) {
        val hash = title.hashCode()
        when (kotlin.math.abs(hash) % 4) {
            0 -> Brush.linearGradient(listOf(Color(0xFF00F0FF), Color(0xFF0051FF)))
            1 -> Brush.linearGradient(listOf(Color(0xFFB026FF), Color(0xFFFF2A85)))
            2 -> Brush.linearGradient(listOf(Color(0xFF1DE9B6), Color(0xFF00B0FF)))
            else -> Brush.linearGradient(listOf(Color(0xFFFF9100), Color(0xFFFF1744)))
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.backgroundDeep)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        colors.glassBorder.copy(alpha = 0.5f),
                        colors.accentGlow.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 1. Remote Channel Logo / Web Image (M3U tvg-logo, Album Art)
            isWebUrl -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$title logo",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = colors.accentGlow,
                                strokeWidth = 1.5.dp
                            )
                        }
                    },
                    error = {
                        FallbackChromaticBadge(
                            title = title,
                            mediaType = mediaType,
                            gradient = dynamicGradient
                        )
                    },
                    success = {
                        SubcomposeAsyncImageContent(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }

            // 2. Local Video Thumbnail via VideoThumbnailView logic
            isLocalVideo && !url.isNullOrBlank() -> {
                var localBitmap by remember(url) {
                    mutableStateOf(VideoThumbnailCache.memoryCache.get(url))
                }

                LaunchedEffect(url) {
                    if (localBitmap == null) {
                        localBitmap = loadVideoFrameThumbnail(context, url)
                    }
                }

                if (localBitmap != null) {
                    Image(
                        bitmap = localBitmap!!.asImageBitmap(),
                        contentDescription = "$title thumbnail",
                        contentScale = contentScale,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    FallbackChromaticBadge(
                        title = title,
                        mediaType = mediaType,
                        gradient = dynamicGradient
                    )
                }
            }

            // 3. Fallback Chromatic Glass Badge
            else -> {
                FallbackChromaticBadge(
                    title = title,
                    mediaType = mediaType,
                    gradient = dynamicGradient
                )
            }
        }

        // Live / Format Tag Overlay Badge (e.g. "LIVE", "HLS", "4K")
        if (showLiveBadge || !badgeText.isNullOrBlank()) {
            val text = badgeText ?: "LIVE"
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (text.equals("LIVE", ignoreCase = true)) Color(0xFFEF4444).copy(alpha = 0.9f)
                        else colors.backgroundDeep.copy(alpha = 0.85f)
                    )
                    .border(
                        0.5.dp,
                        if (text.equals("LIVE", ignoreCase = true)) Color(0xFFFF5252) else colors.accentGlow.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun FallbackChromaticBadge(
    title: String,
    mediaType: ThumbnailMediaType,
    gradient: Brush
) {
    val initialLetter = remember(title) {
        val clean = title.trim().filter { it.isLetterOrDigit() }
        if (clean.isNotEmpty()) clean.take(2).uppercase() else "TV"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        // Specular glow dot
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(gradient)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            when (mediaType) {
                ThumbnailMediaType.CHANNEL_STREAM -> Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                ThumbnailMediaType.VIDEO -> Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                ThumbnailMediaType.AUDIO -> Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                ThumbnailMediaType.PLAYLIST -> Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                ThumbnailMediaType.GITHUB_REPO -> Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Monogram initial overlay at bottom
        Text(
            text = initialLetter,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = 0.85f)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        )
    }
}

private suspend fun loadVideoFrameThumbnail(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
    VideoThumbnailCache.memoryCache.get(uriString)?.let { return@withContext it }
    var bitmap: Bitmap? = null
    try {
        val uri = Uri.parse(uriString)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            bitmap = try {
                context.contentResolver.loadThumbnail(uri, android.util.Size(160, 160), null)
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap == null) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    retriever.setDataSource(uriString, HashMap())
                } else {
                    retriever.setDataSource(context, uri)
                }
                bitmap = retriever.getFrameAtTime(1000000L, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            } finally {
                retriever.release()
            }
        }
        if (bitmap != null) {
            VideoThumbnailCache.memoryCache.put(uriString, bitmap)
        }
    } catch (e: Exception) {
        // Fallback gracefully
    }
    bitmap
}
