package com.example.media.m3u

import android.content.Context
import com.example.data.repository.MediaRepository
import com.example.model.TrackItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ExtInfMetadata(
    val durationSec: Int = -1,
    val title: String = "",
    val artist: String = "Online Stream",
    val logoUrl: String? = null,
    val groupTitle: String = "General Stream",
    val tvgId: String? = null,
    val tvgName: String? = null,
    val streamType: M3uStreamType = M3uStreamType.AUDIO_STREAM,
    val rawAttributes: Map<String, String> = emptyMap()
)

enum class M3uStreamType(val label: String, val badgeColor: Long) {
    AUDIO_STREAM("Audio Stream", 0xFF10B981),
    HLS_M3U8("HLS Adaptive", 0xFF00F0FF),
    DASH_MPD("DASH Stream", 0xFFF59E0B),
    VIDEO_STREAM("Video Stream", 0xFFEC4899)
}

data class ParsedM3uEntry(
    val id: String = "m3u_${System.currentTimeMillis()}_${kotlin.random.Random.nextInt(1000, 9999)}",
    val streamUrl: String,
    val metadata: ExtInfMetadata,
    val isSelected: Boolean = true
) {
    val durationText: String
        get() = if (metadata.durationSec > 0) {
            val mins = metadata.durationSec / 60
            val secs = metadata.durationSec % 60
            String.format("%d:%02d", mins, secs)
        } else {
            "LIVE"
        }
}

data class ParsedM3uPlaylist(
    val name: String,
    val sourceUrlOrPath: String,
    val entries: List<ParsedM3uEntry>,
    val headerTags: List<String> = emptyList()
) {
    val selectedEntriesCount: Int get() = entries.count { it.isSelected }
    val totalEntriesCount: Int get() = entries.size
    val totalDurationText: String
        get() {
            val totalSec = entries.filter { it.isSelected && it.metadata.durationSec > 0 }.sumOf { it.metadata.durationSec }
            return if (totalSec > 0) {
                val hours = totalSec / 3600
                val mins = (totalSec % 3600) / 60
                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
            } else {
                "${selectedEntriesCount} Active Feeds"
            }
        }
}

object M3uParserService {

    /**
     * Parses raw string content of an M3U or M3U8 playlist.
     * Extracts #EXTINF attributes (tvg-logo, tvg-id, tvg-name, group-title),
     * track duration, title, artist, and stream URL.
     */
    fun parseM3uContent(content: String, sourceName: String = "M3U Playlist"): ParsedM3uPlaylist {
        val entries = mutableListOf<ParsedM3uEntry>()
        val headerTags = mutableListOf<String>()
        val lines = content.lines()

        var currentMetadata = ExtInfMetadata()
        var hasPendingInf = false

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTM3U", ignoreCase = true)) {
                headerTags.add(line)
            } else if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                currentMetadata = parseExtInfLine(line)
                hasPendingInf = true
            } else if (line.startsWith("#EXTGRP:", ignoreCase = true)) {
                val group = line.substringAfter(":").trim()
                currentMetadata = currentMetadata.copy(groupTitle = group)
            } else if (!line.startsWith("#")) {
                val streamUrl = line
                if (isSupportedStreamUrl(streamUrl)) {
                    val detectedType = detectStreamType(streamUrl)
                    val finalMeta = if (!hasPendingInf) {
                        ExtInfMetadata(
                            title = extractTitleFromUrl(streamUrl),
                            artist = "Direct Feed",
                            streamType = detectedType
                        )
                    } else {
                        currentMetadata.copy(streamType = detectedType)
                    }

                    entries.add(
                        ParsedM3uEntry(
                            streamUrl = streamUrl,
                            metadata = finalMeta,
                            isSelected = true
                        )
                    )
                }

                // Reset for next entry
                currentMetadata = ExtInfMetadata()
                hasPendingInf = false
            }
        }

        return ParsedM3uPlaylist(
            name = derivePlaylistName(sourceName),
            sourceUrlOrPath = sourceName,
            entries = entries,
            headerTags = headerTags
        )
    }

    /**
     * Helper to parse detailed #EXTINF line:
     * e.g. #EXTINF:-1 tvg-id="id1" tvg-name="Stream 1" tvg-logo="http://..." group-title="EDM",Artist - Track Title
     */
    private fun parseExtInfLine(line: String): ExtInfMetadata {
        val payload = line.substringAfter("#EXTINF:").substringAfter("#extinf:").trim()
        val commaIdx = payload.indexOf(',')

        val metaHeader = if (commaIdx != -1) payload.substring(0, commaIdx).trim() else payload
        val titleText = if (commaIdx != -1) payload.substring(commaIdx + 1).trim() else "M3U Stream"

        // Duration token
        val durationToken = metaHeader.split("\\s+".toRegex()).firstOrNull() ?: "-1"
        val durationSec = durationToken.toIntOrNull() ?: -1

        val attributes = mutableMapOf<String, String>()
        val attrRegex = """([a-zA-Z0-9_\-]+)="([^"]*)"""".toRegex()
        for (match in attrRegex.findAll(metaHeader)) {
            attributes[match.groupValues[1]] = match.groupValues[2]
        }

        val groupTitle = attributes["group-title"] ?: attributes["group"] ?: "M3U Streams"
        val tvgLogo = attributes["tvg-logo"]
        val tvgId = attributes["tvg-id"]
        val tvgName = attributes["tvg-name"]

        // Parse artist and title from comma segment
        val (artist, title) = if (titleText.contains(" - ")) {
            val parts = titleText.split(" - ", limit = 2)
            parts[0].trim() to parts[1].trim()
        } else {
            (tvgName ?: "Online Stream") to titleText
        }

        return ExtInfMetadata(
            durationSec = durationSec,
            title = if (title.isNotBlank()) title else "Stream Track",
            artist = artist,
            logoUrl = tvgLogo,
            groupTitle = groupTitle,
            tvgId = tvgId,
            tvgName = tvgName,
            rawAttributes = attributes
        )
    }

    private fun detectStreamType(url: String): M3uStreamType {
        val lower = url.lowercase()
        return when {
            lower.contains(".m3u8") || lower.contains("hls") -> M3uStreamType.HLS_M3U8
            lower.contains(".mpd") || lower.contains("dash") -> M3uStreamType.DASH_MPD
            lower.contains(".mp4") || lower.contains(".mkv") || lower.contains("video") -> M3uStreamType.VIDEO_STREAM
            else -> M3uStreamType.AUDIO_STREAM
        }
    }

    private fun isSupportedStreamUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.startsWith("http://", ignoreCase = true) ||
               trimmed.startsWith("https://", ignoreCase = true) ||
               trimmed.startsWith("content://", ignoreCase = true) ||
               trimmed.startsWith("file://", ignoreCase = true)
    }

    private fun extractTitleFromUrl(url: String): String {
        val filename = url.substringAfterLast("/").substringBefore("?")
        return if (filename.isNotBlank()) {
            filename.removeSuffix(".mp3").removeSuffix(".m3u8").removeSuffix(".aac")
                .replace('_', ' ').replace('-', ' ')
        } else {
            "Custom Stream Track"
        }
    }

    private fun derivePlaylistName(sourceName: String): String {
        val clean = sourceName.substringAfterLast("/").substringAfterLast("\\").substringBefore("?")
        return if (clean.isNotBlank()) {
            clean.removeSuffix(".m3u8").removeSuffix(".m3u")
                .replace('_', ' ').replace('-', ' ')
                .split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
        } else {
            "M3U Playlist Import"
        }
    }

    /**
     * Converts a list of selected ParsedM3uEntry items into Room database models
     * and saves them persistently.
     */
    suspend fun saveEntriesToRoomDatabase(
        playlistName: String,
        entries: List<ParsedM3uEntry>,
        repository: MediaRepository,
        iconColorHex: String = "#00F0FF"
    ): String = withContext(Dispatchers.IO) {
        val playlistId = repository.createPlaylist(name = playlistName, iconColorHex = iconColorHex)
        val selectedOnly = entries.filter { it.isSelected }

        for (entry in selectedOnly) {
            val trackId = "m3u_${entry.streamUrl.hashCode()}_${System.currentTimeMillis() % 10000}"
            val trackItem = TrackItem(
                id = trackId,
                title = entry.metadata.title,
                artist = entry.metadata.artist,
                album = playlistName,
                durationMs = if (entry.metadata.durationSec > 0) entry.metadata.durationSec * 1000L else 0L,
                uri = entry.streamUrl,
                albumArtUri = entry.metadata.logoUrl,
                isFavorite = false
            )
            repository.populateInitialDataIfEmpty(listOf(trackItem), emptyList())
            repository.addTrackToPlaylist(playlistId, trackId)
        }
        playlistId
    }
}
