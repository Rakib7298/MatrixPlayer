package com.example.media.lyrics

data class LyricLine(
    val timestampMs: Long,
    val text: String
) {
    val timestampFormatted: String
        get() {
            val mins = (timestampMs / 1000) / 60
            val secs = (timestampMs / 1000) % 60
            return String.format("%02d:%02d", mins, secs)
        }
}

data class ParsedLyrics(
    val title: String,
    val artist: String,
    val lines: List<LyricLine>,
    val isTimeSynced: Boolean = true
) {
    fun activeLineIndex(currentPositionMs: Long): Int {
        if (lines.isEmpty()) return -1
        for (i in lines.indices.reversed()) {
            if (currentPositionMs >= lines[i].timestampMs) {
                return i
            }
        }
        return 0
    }
}

object LyricsParser {
    private val LRC_REGEX = """\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?](.*)""".toRegex()

    fun parse(rawText: String, title: String = "", artist: String = ""): ParsedLyrics {
        val lines = mutableListOf<LyricLine>()
        var hasTimestamps = false

        for (line in rawText.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val match = LRC_REGEX.find(trimmed)
            if (match != null) {
                hasTimestamps = true
                val mins = match.groupValues[1].toLongOrNull() ?: 0L
                val secs = match.groupValues[2].toLongOrNull() ?: 0L
                val msFraction = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                val totalMs = (mins * 60 + secs) * 1000 + msFraction
                val lyricText = match.groupValues[4].trim()
                if (lyricText.isNotEmpty()) {
                    lines.add(LyricLine(totalMs, lyricText))
                }
            } else if (!trimmed.startsWith("[")) {
                // Untimed line
                lines.add(LyricLine(lines.size * 4000L, trimmed))
            }
        }

        return ParsedLyrics(
            title = title,
            artist = artist,
            lines = lines.sortedBy { it.timestampMs },
            isTimeSynced = hasTimestamps
        )
    }
}
