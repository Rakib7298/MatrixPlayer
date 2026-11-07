package com.example.media.subtitle

import androidx.compose.ui.graphics.Color

enum class SubtitleFormat(val extension: String, val mimeType: String) {
    SRT("srt", "application/x-subrip"),
    VTT("vtt", "text/vtt"),
    ASS("ass", "text/x-ssa"),
    SSA("ssa", "text/x-ssa")
}

data class SubtitleCue(
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

data class SubtitleTrackItem(
    val id: String,
    val language: String,
    val languageCode: String,
    val format: SubtitleFormat = SubtitleFormat.SRT,
    val isEmbedded: Boolean = false,
    val filePath: String = "",
    val encoding: String = "Universal (UTF-8)",
    val cues: List<SubtitleCue> = emptyList()
)

data class DualSubtitleSettings(
    // Upper / Primary / Target Language Subtitle
    val upperEnabled: Boolean = true,
    val upperLanguage: String = "English(cc)",
    val upperFontSizeSp: Int = 25,
    val upperColor: Color = Color.White,
    val upperDelayMs: Long = 0L,

    // Lower / Secondary / Mother Language Subtitle
    val lowerEnabled: Boolean = true,
    val lowerLanguage: String = "Bengali(বাংলা)",
    val lowerFontSizeSp: Int = 22,
    val lowerColor: Color = Color(0xFFEF4444), // Red/coral accent matching Reference 1cb5f32d
    val lowerDelayMs: Long = 0L,

    // General Styling & Sync
    val encoding: String = "Universal (UTF-8)",
    val backgroundOpacity: Float = 0.5f,
    val showTextShadow: Boolean = true,
    val textShadowRadius: Float = 3f,
    val verticalOffsetDp: Int = 0
)

object SubtitleParser {

    /**
     * Parses SRT or WebVTT content into a list of SubtitleCues.
     */
    fun parseSrtOrVtt(content: String): List<SubtitleCue> {
        val cues = mutableListOf<SubtitleCue>()
        val lines = content.lines()
        var i = 0

        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.contains("-->")) {
                val parts = line.split("-->")
                if (parts.size == 2) {
                    val startMs = parseTimestamp(parts[0].trim())
                    val endMs = parseTimestamp(parts[1].trim().split(" ")[0])
                    val textBuilder = StringBuilder()
                    i++
                    while (i < lines.size && lines[i].isNotBlank()) {
                        if (textBuilder.isNotEmpty()) textBuilder.append("\n")
                        textBuilder.append(lines[i].trim())
                        i++
                    }
                    if (startMs >= 0 && endMs > startMs) {
                        cues.add(SubtitleCue(startMs, endMs, textBuilder.toString()))
                    }
                }
            }
            i++
        }
        return cues
    }

    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val clean = timestamp.replace(',', '.')
            val parts = clean.split(":")
            if (parts.size == 3) {
                val hours = parts[0].toLong()
                val minutes = parts[1].toLong()
                val secondsParts = parts[2].split(".")
                val seconds = secondsParts[0].toLong()
                val millis = if (secondsParts.size > 1) secondsParts[1].padEnd(3, '0').take(3).toLong() else 0L
                (hours * 3600000L) + (minutes * 60000L) + (seconds * 1000L) + millis
            } else if (parts.size == 2) {
                val minutes = parts[0].toLong()
                val secondsParts = parts[1].split(".")
                val seconds = secondsParts[0].toLong()
                val millis = if (secondsParts.size > 1) secondsParts[1].padEnd(3, '0').take(3).toLong() else 0L
                (minutes * 60000L) + (seconds * 1000L) + millis
            } else -1L
        } catch (e: Exception) {
            -1L
        }
    }
}
