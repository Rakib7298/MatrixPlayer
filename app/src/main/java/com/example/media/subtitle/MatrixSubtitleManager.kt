package com.example.media.subtitle

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class OnlineSubtitleResult(
    val id: String,
    val title: String,
    val language: String,
    val format: String,
    val downloads: Int,
    val rating: Float,
    val content: String
)

class MatrixSubtitleManager(private val context: Context) {

    private val _settings = MutableStateFlow(DualSubtitleSettings())
    val settings: StateFlow<DualSubtitleSettings> = _settings.asStateFlow()

    // Loaded cues for upper (Target language)
    private val _upperCues = MutableStateFlow<List<SubtitleCue>>(emptyList())
    val upperCues: StateFlow<List<SubtitleCue>> = _upperCues.asStateFlow()

    // Loaded cues for lower (Mother language)
    private val _lowerCues = MutableStateFlow<List<SubtitleCue>>(emptyList())
    val lowerCues: StateFlow<List<SubtitleCue>> = _lowerCues.asStateFlow()

    // Online search results
    private val _onlineResults = MutableStateFlow<List<OnlineSubtitleResult>>(emptyList())
    val onlineResults: StateFlow<List<OnlineSubtitleResult>> = _onlineResults.asStateFlow()

    init {
        // Pre-seed realistic dual subtitle cues matching UI Reference 1cb5f32d
        val defaultEnglishSrt = """
            1
            00:00:01,000 --> 00:00:06,000
            Matrix Player: Super Crystal Liquid Glass Subtitle Studio

            2
            00:00:06,500 --> 00:00:15,000
            The losers find problem in every answer but the winners find an answer in every problem.

            3
            00:00:16,000 --> 00:00:24,000
            Welcome to the future of high-precision audio and video playback on Android.

            4
            00:00:25,000 --> 00:00:35,000
            Enjoy lossless acoustic fidelity and ultra-smooth crystal visualizers.
        """.trimIndent()

        val defaultBengaliSrt = """
            1
            00:00:01,000 --> 00:00:06,000
            ম্যাট্রিক্স প্লেয়ার: সুপার ক্রিস্টাল লিকুইড গ্লাস সাবটাইটেল স্টুডিও

            2
            00:00:06,500 --> 00:00:15,000
            পরাজিতরা প্রতিটি উত্তরে সমস্যা খুঁজে পায় কিন্তু বিজয়ীরা প্রতিটি সমস্যায় উত্তর খুঁজে পায়।

            3
            00:00:16,000 --> 00:00:24,000
            অ্যান্ড্রয়েডে উচ্চ-মানের অডিও এবং ভিডিও প্লেব্যাকের জগতে স্বাগতম।

            4
            00:00:25,000 --> 00:00:35,000
            লবলেস অ্যাকোস্টিক ফিডেলিটি এবং ক্রিস্টাল ভিজ্যুয়ালাইজার উপভোগ করুন।
        """.trimIndent()

        // Initialize empty by default so video playback is clean without blocking text
        _upperCues.value = emptyList()
        _lowerCues.value = emptyList()

        // Seed online search catalog
        _onlineResults.value = listOf(
            OnlineSubtitleResult("sub1", "The Studio - Official CC (Full)", "English (cc)", "SRT", 14200, 4.9f, defaultEnglishSrt),
            OnlineSubtitleResult("sub2", "The Studio - Bengali Translation (বাংলা)", "Bengali (বাংলা)", "SRT", 8400, 4.8f, defaultBengaliSrt),
            OnlineSubtitleResult("sub3", "The Studio - Spanish (Español Latino)", "Spanish (Español)", "VTT", 11200, 4.7f, defaultEnglishSrt),
            OnlineSubtitleResult("sub4", "The Studio - Japanese (日本語 ASS Styled)", "Japanese (日本語)", "ASS", 6300, 4.9f, defaultEnglishSrt),
            OnlineSubtitleResult("sub5", "The Studio - French (Français)", "French (Français)", "SRT", 5100, 4.6f, defaultEnglishSrt)
        )
    }

    fun getCurrentUpperCue(videoPositionMs: Long): String? {
        if (!_settings.value.upperEnabled) return null
        val syncPos = videoPositionMs + _settings.value.upperDelayMs
        return _upperCues.value.firstOrNull { syncPos in it.startTimeMs..it.endTimeMs }?.text
    }

    fun getCurrentLowerCue(videoPositionMs: Long): String? {
        if (!_settings.value.lowerEnabled) return null
        val syncPos = videoPositionMs + _settings.value.lowerDelayMs
        return _lowerCues.value.firstOrNull { syncPos in it.startTimeMs..it.endTimeMs }?.text
    }

    fun setUpperLanguage(lang: String) {
        _settings.value = _settings.value.copy(upperLanguage = lang)
    }

    fun setLowerLanguage(lang: String) {
        _settings.value = _settings.value.copy(lowerLanguage = lang)
    }

    fun setUpperFontSize(sizeSp: Int) {
        _settings.value = _settings.value.copy(upperFontSizeSp = sizeSp.coerceIn(12, 48))
    }

    fun setLowerFontSize(sizeSp: Int) {
        _settings.value = _settings.value.copy(lowerFontSizeSp = sizeSp.coerceIn(12, 48))
    }

    fun setUpperColor(color: Color) {
        _settings.value = _settings.value.copy(upperColor = color)
    }

    fun setLowerColor(color: Color) {
        _settings.value = _settings.value.copy(lowerColor = color)
    }

    val aiGenerator = GeminiSubtitleGeneratorEngine()

    fun setUpperDelayMs(delayMs: Long) {
        _settings.value = _settings.value.copy(upperDelayMs = delayMs)
    }

    fun setLowerDelayMs(delayMs: Long) {
        _settings.value = _settings.value.copy(lowerDelayMs = delayMs)
    }

    fun adjustUpperDelay(deltaMs: Long) {
        _settings.value = _settings.value.copy(upperDelayMs = _settings.value.upperDelayMs + deltaMs)
    }

    fun adjustLowerDelay(deltaMs: Long) {
        _settings.value = _settings.value.copy(lowerDelayMs = _settings.value.lowerDelayMs + deltaMs)
    }

    fun toggleUpperEnabled() {
        _settings.value = _settings.value.copy(upperEnabled = !_settings.value.upperEnabled)
    }

    fun toggleLowerEnabled() {
        _settings.value = _settings.value.copy(lowerEnabled = !_settings.value.lowerEnabled)
    }

    fun setEncoding(encoding: String) {
        _settings.value = _settings.value.copy(encoding = encoding)
    }

    fun loadExternalSubtitle(content: String, isUpper: Boolean) {
        val parsed = SubtitleParser.parseSrtOrVtt(content)
        if (isUpper) {
            _upperCues.value = parsed
        } else {
            _lowerCues.value = parsed
        }
    }

    fun applyOnlineSubtitle(result: OnlineSubtitleResult, isUpper: Boolean) {
        loadExternalSubtitle(result.content, isUpper)
        if (isUpper) {
            setUpperLanguage(result.language)
        } else {
            setLowerLanguage(result.language)
        }
    }
}
