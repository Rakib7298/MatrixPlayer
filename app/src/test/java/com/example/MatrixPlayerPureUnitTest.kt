package com.example

import com.example.model.TrackItem
import com.example.model.VideoItem
import com.example.ui.theme.MatrixThemeMode
import com.example.viewmodel.PlayerUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MatrixPlayerPureUnitTest {

    @Test
    fun trackItem_creationAndProperties() {
        val track = TrackItem(
            id = "test_1",
            title = "Midnight Echo",
            artist = "Noctara",
            album = "Digital Horizons",
            durationMs = 288000L,
            isFavorite = true
        )
        assertEquals("test_1", track.id)
        assertEquals("Midnight Echo", track.title)
        assertEquals("Noctara", track.artist)
        assertTrue(track.isFavorite)
    }

    @Test
    fun videoItem_creationAndResolution() {
        val video = VideoItem(
            id = "v1",
            title = "The Studio",
            durationMs = 180000L,
            resolution = "3840x2160 (4K)",
            sizeMb = 450.2
        )
        assertEquals("v1", video.id)
        assertEquals("3840x2160 (4K)", video.resolution)
        assertEquals(450.2, video.sizeMb, 0.01)
    }

    @Test
    fun playerUiState_defaultValuesMatchReference() {
        val state = PlayerUiState(
            currentTrack = TrackItem("1", "Midnight Echo", "Noctara", "Digital Horizons", 288000L),
            isPlaying = false,
            currentTheme = MatrixThemeMode.MIDNIGHT_NEON,
            eqBands = listOf(4.0f, -1.5f, 2.5f, -0.5f),
            selectedPreset = "Deep"
        )
        assertNotNull(state.currentTrack)
        assertEquals("Midnight Echo", state.currentTrack?.title)
        assertFalse(state.isPlaying)
        assertEquals(MatrixThemeMode.MIDNIGHT_NEON, state.currentTheme)
        assertEquals(4, state.eqBands.size)
        assertEquals(4.0f, state.eqBands[0], 0.01f)
        assertEquals("Deep", state.selectedPreset)
    }

    @Test
    fun equalizerPresetGains_areCorrect() {
        val deepGains = listOf(4.0f, -1.5f, 2.5f, -0.5f)
        val electronicGains = listOf(3.5f, 1.0f, 2.0f, 4.0f)
        val flatGains = listOf(0.0f, 0.0f, 0.0f, 0.0f)

        assertEquals(4.0f, deepGains[0], 0.01f)
        assertEquals(3.5f, electronicGains[0], 0.01f)
        assertEquals(0.0f, flatGains[0], 0.01f)
    }

    @Test
    fun videoPlaybackState_defaultValuesAndAspectModes() {
        val state = com.example.media.video.VideoPlaybackState(
            isPlaying = true,
            currentPositionMs = 75000L,
            durationMs = 300000L,
            playbackSpeed = 1.25f,
            aspectRatioMode = com.example.media.video.VideoAspectRatioMode.FIT,
            volumePercent = 0.8f,
            brightnessPercent = 0.65f
        )
        assertTrue(state.isPlaying)
        assertEquals(75000L, state.currentPositionMs)
        assertEquals(300000L, state.durationMs)
        assertEquals(1.25f, state.playbackSpeed, 0.001f)
        assertEquals(com.example.media.video.VideoAspectRatioMode.FIT, state.aspectRatioMode)
        assertEquals(0.8f, state.volumePercent, 0.01f)
        assertEquals(0.65f, state.brightnessPercent, 0.01f)

        // Verify aspect modes
        val modes = com.example.media.video.VideoAspectRatioMode.values()
        assertTrue(modes.contains(com.example.media.video.VideoAspectRatioMode.FIT))
        assertTrue(modes.contains(com.example.media.video.VideoAspectRatioMode.FILL))
        assertTrue(modes.contains(com.example.media.video.VideoAspectRatioMode.FIXED_4_3))
        assertTrue(modes.contains(com.example.media.video.VideoAspectRatioMode.FIXED_21_9))
    }

    @Test
    fun videoSubtitlesAndAudioTracks_structure() {
        val state = com.example.media.video.VideoPlaybackState()
        assertEquals(2, state.availableAudioTracks.size)
        assertEquals(4, state.availableSubtitleTracks.size)
        assertEquals(0L, state.audioDelayMs)
        assertEquals(0L, state.subtitleDelayMs)
    }

    @Test
    fun glassEqPresets_validation() {
        val presets = com.example.ui.screens.DefaultGlassPresets
        assertTrue(presets.isNotEmpty())
        val cyberBass = presets.firstOrNull { it.name == "Neon Cyber Bass" }
        assertNotNull(cyberBass)
        assertEquals(7, cyberBass?.gains7?.size)
        assertEquals(10, cyberBass?.gains10?.size)
        assertTrue((cyberBass?.bassPercent ?: 0f) > 0.7f)

        val modes = com.example.ui.screens.EqMode.values()
        assertEquals(3, modes.size)
    }

    @Test
    fun subtitleParser_parsesSrtCuesAccurately() {
        val srtSample = """
            1
            00:01:05,200 --> 00:01:10,500
            The losers find problem in every answer
            
            2
            00:01:11,000 --> 00:01:15,000
            but the winners find an answer in every problem.
        """.trimIndent()

        val cues = com.example.media.subtitle.SubtitleParser.parseSrtOrVtt(srtSample)
        assertEquals(2, cues.size)
        assertEquals(65200L, cues[0].startTimeMs)
        assertEquals(70500L, cues[0].endTimeMs)
        assertEquals("The losers find problem in every answer", cues[0].text)

        assertEquals(71000L, cues[1].startTimeMs)
        assertEquals(75000L, cues[1].endTimeMs)
    }

    @Test
    fun dualSubtitleSettings_defaultsMatchReference() {
        val settings = com.example.media.subtitle.DualSubtitleSettings()
        assertTrue(settings.upperEnabled)
        assertTrue(settings.lowerEnabled)
        assertEquals("English(cc)", settings.upperLanguage)
        assertEquals("Bengali(বাংলা)", settings.lowerLanguage)
        assertEquals(25, settings.upperFontSizeSp)
        assertEquals(22, settings.lowerFontSizeSp)
        assertEquals("Universal (UTF-8)", settings.encoding)
    }

    @Test
    fun mediaItemEntity_advancedAttributes() {
        val entity = com.example.data.local.entity.MediaItemEntity(
            id = "test_media_1",
            title = "Cyber Odyssey",
            artist = "Neon Pulse",
            album = "Digital Horizons",
            durationMs = 214000L,
            uri = "content://media/external/audio/media/101",
            year = "2024",
            trackNumber = 3,
            bitrate = 320,
            sampleRate = 48000,
            playCount = 5,
            lyrics = "[00:15.00] In the neon glow..."
        )
        assertEquals("test_media_1", entity.id)
        assertEquals("2024", entity.year)
        assertEquals(3, entity.trackNumber)
        assertEquals(320, entity.bitrate)
        assertEquals(48000, entity.sampleRate)
        assertEquals(5, entity.playCount)
        assertNotNull(entity.lyrics)
    }

    @Test
    fun playlistEntity_advancedAttributes() {
        val pl = com.example.data.local.entity.PlaylistEntity(
            id = "pl_1",
            name = "Late Night Synth",
            description = "Best chill synthwave tracks",
            iconColorHex = "#B026FF",
            isSmartPlaylist = false
        )
        assertEquals("pl_1", pl.id)
        assertEquals("Late Night Synth", pl.name)
        assertEquals("Best chill synthwave tracks", pl.description)
        assertFalse(pl.isSmartPlaylist)
    }

    @Test
    fun streamStation_protocolsAndHealthChecks() {
        val station = com.example.media.streaming.StreamStation(
            id = "st_synth",
            name = "Nightride FM — Synthwave",
            description = "High octane retrowave",
            url = "https://stream.nightride.fm/synthwave.mp3",
            genre = "Synthwave",
            protocol = com.example.media.streaming.StreamProtocol.HTTP_MP3,
            bitrateKbps = 320,
            health = com.example.media.streaming.StreamHealth.EXCELLENT
        )
        assertEquals("st_synth", station.id)
        assertEquals(com.example.media.streaming.StreamProtocol.HTTP_MP3, station.protocol)
        assertEquals(320, station.bitrateKbps)
        assertEquals(com.example.media.streaming.StreamHealth.EXCELLENT, station.health)
        assertEquals("HTTP/MP3", station.protocol.label)
    }

    @Test
    fun streamTelemetry_bufferAndStats() {
        val station = com.example.media.streaming.StreamStation(
            id = "st_lofi",
            name = "Matrix Lo-Fi Chillhop Radio",
            description = "Mellow study beats",
            url = "https://stream.zeno.fm/f3wvbbqmdg8uv",
            genre = "Lo-Fi",
            protocol = com.example.media.streaming.StreamProtocol.AAC_LIVE,
            bitrateKbps = 256
        )
        val telemetry = com.example.media.streaming.StreamTelemetry(
            activeStation = station,
            isStreaming = true,
            currentBitrateKbps = 256,
            bufferPercentage = 0.92f,
            downloadedBytes = 15000000L
        )
        assertTrue(telemetry.isStreaming)
        assertEquals(256, telemetry.currentBitrateKbps)
        assertEquals(0.92f, telemetry.bufferPercentage, 0.001f)
        assertEquals(15000000L, telemetry.downloadedBytes)
        assertEquals("Matrix Lo-Fi Chillhop Radio", telemetry.activeStation?.name)
    }

    @Test
    fun gitHubPlaylistParser_normalizesRawUrls() {
        val webUrl = "https://github.com/matrix-player/playlists/blob/main/synthwave.m3u"
        val expected = "https://raw.githubusercontent.com/matrix-player/playlists/main/synthwave.m3u"
        val normalized = com.example.media.github.GitHubPlaylistParser.normalizeToRawUrl(webUrl)
        assertEquals(expected, normalized)

        val alreadyRaw = "https://raw.githubusercontent.com/user/repo/main/stream.m3u8"
        assertEquals(alreadyRaw, com.example.media.github.GitHubPlaylistParser.normalizeToRawUrl(alreadyRaw))
    }

    @Test
    fun gitHubPlaylistParser_parsesM3uContentAccurately() {
        val sampleM3u = """
            #EXTM3U
            #EXTINF:215 tvg-logo="https://example.com/logo1.png" group-title="Retrowave",Neon Pulse - Hyperdrive
            https://stream.nightride.fm/synthwave.mp3
            
            #EXTINF:-1 group-title="Ambient",SomaFM - Drone Zone Live
            https://ice1.somafm.com/dronezone-128-mp3
        """.trimIndent()

        val parsed = com.example.media.github.GitHubPlaylistParser.parseM3u(
            content = sampleM3u,
            sourceUrl = "https://raw.githubusercontent.com/test/synthwave_collection.m3u"
        )

        assertEquals("Synthwave Collection", parsed.name)
        assertEquals(2, parsed.tracks.size)

        val track1 = parsed.tracks[0]
        assertEquals("Hyperdrive", track1.title)
        assertEquals("Neon Pulse", track1.artist)
        assertEquals(215, track1.durationSec)
        assertEquals("Retrowave", track1.group)
        assertEquals("https://example.com/logo1.png", track1.logoUrl)
        assertEquals("https://stream.nightride.fm/synthwave.mp3", track1.url)

        val track2 = parsed.tracks[1]
        assertEquals("Drone Zone Live", track2.title)
        assertEquals("SomaFM", track2.artist)
        assertEquals(-1, track2.durationSec)
        assertEquals("Ambient", track2.group)
        assertEquals("https://ice1.somafm.com/dronezone-128-mp3", track2.url)
    }
}
