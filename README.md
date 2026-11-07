# Matrix Player (Native Android)

A premium, offline-first native Android audio and video player built with Jetpack Compose, AndroidX Media3, Room, and modern Kotlin Coroutines/Flow, featuring the **Super Crystal Liquid Glass** aesthetic (70% transparent rounded control panels, glowing cyber borders, and tactile ribbed controls).

---

## Completed Phases

### Phase 1: Project Foundation, Theme System & Navigation
- Multi-theme engine: **Midnight Neon**, **Aqua Liquid Glass**, and **Dynamic Album Art**.
- Full navigation graph with 19 distinct screens and backstack handling.
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

### Phase 8: GitHub Raw Playlist Integration
- **GitHub M3U / M3U8 Parser (`GitHubPlaylistParser`)**:
  - Automatically normalizes standard GitHub web URLs (`github.com/.../blob/main/...`) into direct raw content endpoints (`raw.githubusercontent.com/.../main/...`).
  - Robust `#EXTM3U` and `#EXTINF` parser extracting track durations, titles, artists, `tvg-logo` artwork URLs, and `group-title` categories.
- **GitHub Playlist Manager (`GitHubPlaylistManager.kt`)**:
  - Fetches raw playlists asynchronously over network with error-handling and curated offline fallbacks.
  - Room database synchronization: creates persistent `PlaylistEntity` with custom accent colors and inserts parsed tracks into `MediaDao` (`MediaItemEntity`), linking them via `PlaylistItemEntity`.
  - Re-sync / Refresh capability to pull the latest versions of community playlists from GitHub repositories.
- **GitHub Playlists UI (`GitHubPlaylistScreen.kt`)**:
  - Super Crystal Liquid Glass control cards with paste-from-clipboard support.
  - Interactive parsed playlist preview showing track count, total calculated duration, sample track listings, and individual preview playback.
  - Curated open source community repositories (*Cyberwave & Synthwave 24/7 Mix*, *Lo-Fi Study & Chillhop Archive*, *Ambient Space & Deep Drone Feeds*, *Free Lossless Audio & FLAC Radio*).
  - Navigation integrations from Home Dashboard quick actions and Streaming Sources.

---

## Pending Phases

- **PHASE 9**: Testing, optimization, and release preparation.
