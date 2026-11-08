package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMatrixColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object VideoThumbnailCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // Use 1/8th of available memory for thumbnails
    val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }
}

@Composable
fun VideoThumbnailView(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val colors = LocalMatrixColors.current
    var thumbnailBitmap by remember(uri) { mutableStateOf(VideoThumbnailCache.memoryCache.get(uri)) }

    LaunchedEffect(uri) {
        if (thumbnailBitmap == null && uri.isNotBlank()) {
            thumbnailBitmap = loadVideoThumbnail(context, uri)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (thumbnailBitmap != null) {
            Image(
                bitmap = thumbnailBitmap!!.asImageBitmap(),
                contentDescription = "Video Thumbnail",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Elegant gradient fallback placeholder
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.primaryGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

private suspend fun loadVideoThumbnail(context: Context, uriString: String): Bitmap? = withContext(Dispatchers.IO) {
    // 1. Check in-memory cache first
    VideoThumbnailCache.memoryCache.get(uriString)?.let { return@withContext it }

    var bitmap: Bitmap? = null
    try {
        val uri = Uri.parse(uriString)

        // On Android Q+, try contentResolver.loadThumbnail for local MediaStore content
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && uriString.startsWith("content://")) {
            try {
                bitmap = context.contentResolver.loadThumbnail(uri, Size(512, 384), null)
            } catch (_: Exception) {}
        }

        // Fallback to MediaMetadataRetriever
        if (bitmap == null) {
            val retriever = MediaMetadataRetriever()
            try {
                if (uriString.startsWith("content://")) {
                    retriever.setDataSource(context, uri)
                } else if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    retriever.setDataSource(uriString, HashMap())
                } else {
                    retriever.setDataSource(context, uri)
                }
                bitmap = retriever.getFrameAtTime(1000000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
            } finally {
                try {
                    retriever.release()
                } catch (_: Exception) {}
            }
        }

        if (bitmap != null) {
            VideoThumbnailCache.memoryCache.put(uriString, bitmap)
        }
    } catch (e: Exception) {
        // Ignored, fallback will show
    }
    bitmap
}
