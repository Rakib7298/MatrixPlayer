# Matrix Player (Native Android)

A premium, offline-first native Android audio and video player built with Jetpack Compose, AndroidX Media3, Room, and modern Kotlin Coroutines/Flow, featuring the **Super Crystal Liquid Glass** aesthetic (70% transparent rounded control panels, glowing cyber borders, and tactile ribbed controls).

---

## Completed Phases & Advanced AI Features

### Phase 1: Project Foundation, Theme System & Navigation
- Multi-theme engine: **Midnight Neon**, **Aqua Liquid Glass**, and **Dynamic Album Art**.
- Full navigation graph with 21 distinct screens and backstack handling.
- Super Crystal Liquid Glass components (`CrystalGlassCard`, `GlassVolumeSlider`, `RibbedDialKnob`, custom glowing top app bars).

### Phase 2: Local Media Discovery, Room Database & Offline Library
- MediaStore query engine (`LocalMediaScanner.kt`) extracting audio, video, album artwork URIs, bitrates, sample rates, duration, and year.
- Room database v2 (`MatrixDatabase.kt`) with 7 entities: `MediaItemEntity`, `PlaylistEntity`, `PlaylistItemEntity`, `PlaybackHistoryEntity`, `EqualizerPresetEntity`, `AudioBookmarkEntity`, and `PlaybackQueueEntity`.
- Media repository with reactive Flow streams for tracks, videos, favorites, playback history, and playlists.

### Phase 3: Real Audio Playback & Background Service
- Media3 `MediaSessionService` implementation (`MatrixMediaPlaybackService.kt`).
- Audio Focus management (`handleAudioFocus = true`) and headphone disconnect pause (`handleAudioBecomingNoisy = true`).
- Foreground playback notification with custom transport actions (`ACTION_FAVORITE`, `ACTION_SHUFFLE`, `ACTION_REPEAT`).
- CPU wake lock support (`C.WAKE_MODE_LOCAL`) and `WAKE_LOCK` permission in `AndroidManifest.xml`.

### Phase 4: Ten Visualizers & EQ Studio
- Hardware-backed Android AudioEffect engine (`AndroidAudioEffectManager.kt`) driving `Equalizer` and `BassBoost`.
- Real-time FFT audio visualizer engine (`RealAudioVisualizerEngine.kt`) extracting 64-band frequency magnitudes and raw waveform byte arrays.
- 10 real audio-reactive visualizers: Matrix Cyber Bar Spectrum, Circular Hologram Tunnel, Liquid Plasma Wave, Neon Hex Matrix, Floating Bubble Orbs, Neon Oscilloscope, Starburst Galaxy, Cyber Grid Terrain, Equalizer Ribbed Columns, and Glass VU Meter.
- Super Advanced Glass Equalizer Studio: 7-Band Pro, 10-Band Studio with live FFT analyzer, and 4-Band Neo with rotary knobs and glass presets.

### Phase 5: Video Playback & Gestures
- ExoPlayer-powered video engine (`MatrixVideoEngine.kt`) with 70% transparent Super Crystal Liquid Glass floating controls.
- Gesture controls: vertical swipe on left for brightness, vertical swipe on right for volume, double-tap left/right for 10-second seek, and pinch-to-zoom (1.0x to 3.5x).
- Aspect ratio modes (Fit, Zoom/Crop, Stretch/Fill, 4:3, 21:9), variable playback speeds (0.25x to 4.0x), and screen lock.

### Phase 6: Subtitle Engine & Dual Language Overlay
- Comprehensive subtitle parser (`SubtitleParser.kt`) supporting SRT, VTT, ASS, and SSA formats with millisecond-precision timing.
- Dual-language live video overlay: Upper Target Language (English CC) and Lower Mother Language (Bengali / Custom).
- Subtitle synchronization with ±50ms step adjustment, custom font sizes, text colors, and background opacity.
- Subtitle Manager screen with local file picker and simulated OpenSubtitles online search catalog.

### Phase 7: Playlists & Streaming
- Room-backed playlist manager (`PlaylistsScreen.kt`) with custom playlist creation (name, description, 6 accent colors), song inspection, track adding/removal, and "Play All" queueing.
- Streaming engine (`StreamModels.kt`, `StreamingManager.kt`, `StreamingSourcesScreen.kt`):
  - Supports HTTP/HTTPS MP3 streams, HLS (`.m3u8`), AAC Live, DASH (`.mpd`), and direct stream inputs.
  - Live Telemetry Control Panel: Active station, live bitrate monitor (kbps), glowing buffer status bar, downloaded data meter, and latency ping tester.
  - Custom stream direct connect panel with URL input, protocol auto-detection chips, and ping health validation.
  - Curated preset stations: Nightride FM Synthwave & Chillsynth, Matrix Lo-Fi Chillhop Radio, SomaFM Groove Salad, Drone Zone, DEF CON Radio, and MUX Live HLS.
  - Station bookmarking and filter tabs.

### Phase 8: GitHub Raw Playlist Integration & M3U Parser Service
- **Robust M3U/M3U8 Parser Service (`M3uParserService.kt`)**:
  - Extracts `#EXTINF` metadata: track duration, title, artist, `tvg-logo` artwork URLs, `group-title` categories, `tvg-id`, `tvg-name`, and stream type detection (`HLS_M3U8`, `AUDIO_STREAM`, `DASH_MPD`, `VIDEO_STREAM`).
  - Normalizes raw GitHub URLs (`github.com/.../blob/main/...` -> `raw.githubusercontent.com/.../main/...`).
- **Interactive M3U Entry Inspector Component (`M3uPreviewDialog.kt`)**:
  - Preview UI dialog letting users select/deselect individual tracks, edit title/artist/group metadata in-line, test stream playback, pick playlist accent color, and commit selected entries to Room database.
- **GitHub Playlist Manager (`GitHubPlaylistManager.kt` & `GitHubPlaylistScreen.kt`)**:
  - Asynchronously fetches raw playlists over network with error-handling and curated Cyberpunk, Lo-Fi, Ambient, and Lossless FLAC presets.
  - Room database synchronization and re-sync capabilities.

### Phase 9: AI Chatbot & Advanced Glass Link Health Checker Pro
- **Matrix AI Chatbot Studio (`MatrixAiChatEngine.kt` & `AiChatScreen.kt`)**:
  - Multi-turn Gemini AI thread supporting `gemini-3.8-flash` (balanced), `gemini-3.5-flash-lite` (fast), and `gemini-4-argon` (pro reasoning).
  - Context-aware media assistant: summarizes active audio/video tracks, recommends similar music/videos, generates dual-language subtitle tracks, sources fresh M3U/M3U8 streams, and discovers open-source GitHub media repos.
  - Super Crystal Liquid Glass chat interface with quick action prompt chips, model selector bar, and scrollable message thread.
- **Advanced Glass Link Health Checker Pro (`LinkHealthCheckerEngine.kt` & `LinkHealthCheckerScreen.kt`)**:
  - Probe single or batch stream URLs for network latency (ms), HTTP response status codes, server headers, and MIME content-types.
  - Health rating badges (*Ultra Low Latency 12ms*, *Healthy / Active 35ms*, *Degraded*, *Offline*).
  - One-tap export to import healthy stream links into Room database or launch playback in Matrix Player.

---

## Release Verification
- Verified JVM unit test suite: `gradle :app:testDebugUnitTest` executed with **BUILD SUCCESSFUL** (20 tests passing).
- Verified applet compilation: `compile_applet` passed cleanly.
