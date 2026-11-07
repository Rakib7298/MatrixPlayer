package com.example.media.subtitle

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout

private const val TAG = "GeminiSubtitleGen"

sealed interface AiSubtitleUiState {
    data object Idle : AiSubtitleUiState
    data object Generating : AiSubtitleUiState
    data class Success(
        val language: String,
        val srtContent: String,
        val cues: List<SubtitleCue>
    ) : AiSubtitleUiState
    data class Error(val message: String) : AiSubtitleUiState
}

class GeminiSubtitleGeneratorEngine {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val _state = MutableStateFlow<AiSubtitleUiState>(AiSubtitleUiState.Idle)
    val state: StateFlow<AiSubtitleUiState> = _state.asStateFlow()

    suspend fun generateTimeSyncedSubtitles(
        mediaTitle: String,
        targetLanguage: String,
        mediaDurationMs: Long = 180_000L
    ) {
        _state.value = AiSubtitleUiState.Generating
        try {
            val durationSec = (mediaDurationMs / 1000).coerceAtLeast(30)
            val prompt = """
                You are an expert AI captioning engine for audio and video streams.
                Generate a clean, time-synced SubRip Subtitle (SRT) track in $targetLanguage for media content titled "$mediaTitle".
                Total duration: $durationSec seconds.

                Rules:
                1. Output standard SRT format with sequential entry numbers (1, 2, 3...), timestamp lines (HH:MM:SS,mmm --> HH:MM:SS,mmm), and caption lines.
                2. Distribute the captions evenly across the $durationSec seconds.
                3. Keep caption lines concise, natural, and accurately translated.
                4. Output ONLY valid raw SRT text without any preamble, conversation, or markdown code blocks.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }

            val rawText = response.text.orEmpty().trim()
            val cleanSrt = rawText.replace("```srt", "").replace("```", "").trim()

            if (cleanSrt.isNotBlank()) {
                val cues = SubtitleParser.parseSrtOrVtt(cleanSrt)
                if (cues.isNotEmpty()) {
                    _state.value = AiSubtitleUiState.Success(targetLanguage, cleanSrt, cues)
                } else {
                    _state.value = AiSubtitleUiState.Error("AI generated output, but could not parse valid time-synced SRT cues.")
                }
            } else {
                _state.value = AiSubtitleUiState.Error("Gemini AI returned empty subtitle content.")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Subtitle generation timed out", e)
            _state.value = AiSubtitleUiState.Error("Gemini AI subtitle generation timed out after 60 seconds.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate AI subtitles", e)
            _state.value = AiSubtitleUiState.Error(e.localizedMessage ?: "Failed to generate AI subtitles.")
        }
    }

    fun resetState() {
        _state.value = AiSubtitleUiState.Idle
    }
}
