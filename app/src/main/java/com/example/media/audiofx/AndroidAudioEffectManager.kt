package com.example.media.audiofx

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Build
import android.util.Log

data class AudioEffectCapabilities(
    val isEqualizerSupported: Boolean = false,
    val isBassBoostSupported: Boolean = false,
    val numberOfBands: Short = 0,
    val minBandLevel: Short = -1200, // in millibels
    val maxBandLevel: Short = 1200,  // in millibels
    val centerFrequenciesHz: List<Int> = emptyList(),
    val presetNames: List<String> = emptyList()
)

/**
 * Manages real Android AudioEffect APIs (Equalizer and BassBoost) attached to the audio session.
 * Honestly reports capabilities and gracefully falls back on unsupported devices or emulators.
 */
class AndroidAudioEffectManager(private val context: Context) {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var currentSessionId: Int = 0

    var capabilities: AudioEffectCapabilities = AudioEffectCapabilities()
        private set

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        try {
            // Initialize hardware / system Equalizer
            val eq = Equalizer(0, audioSessionId)
            eq.enabled = true
            equalizer = eq

            val bands = eq.numberOfBands
            val range = eq.bandLevelRange
            val minLevel = range?.getOrNull(0) ?: -1200
            val maxLevel = range?.getOrNull(1) ?: 1200

            val freqs = mutableListOf<Int>()
            for (i in 0 until bands) {
                freqs.add(eq.getCenterFreq(i.toShort()) / 1000) // Convert mHz to Hz
            }

            val presets = mutableListOf<String>()
            val numPresets = eq.numberOfPresets
            for (i in 0 until numPresets) {
                presets.add(eq.getPresetName(i.toShort()))
            }

            // Initialize BassBoost
            var bbSupported = false
            try {
                val bb = BassBoost(0, audioSessionId)
                if (bb.strengthSupported) {
                    bb.enabled = true
                    bassBoost = bb
                    bbSupported = true
                }
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "BassBoost not supported: ${e.message}")
            }

            capabilities = AudioEffectCapabilities(
                isEqualizerSupported = true,
                isBassBoostSupported = bbSupported,
                numberOfBands = bands,
                minBandLevel = minLevel,
                maxBandLevel = maxLevel,
                centerFrequenciesHz = freqs,
                presetNames = presets
            )
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Equalizer hardware session unavailable: ${e.message}")
            capabilities = AudioEffectCapabilities(
                isEqualizerSupported = false,
                isBassBoostSupported = false,
                numberOfBands = 5,
                minBandLevel = -1200,
                maxBandLevel = 1200,
                centerFrequenciesHz = listOf(60, 250, 1000, 4000, 14000),
                presetNames = listOf("Flat", "Deep", "Vocal", "Electronic", "Rock", "Classical", "Jazz")
            )
        }
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        try {
            equalizer?.let { eq ->
                val minLevel = capabilities.minBandLevel
                val maxLevel = capabilities.maxBandLevel
                val level = ((gainDb / 12f) * maxLevel).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt())
                eq.setBandLevel(bandIndex.toShort(), level.toShort())
            }
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Failed to set band gain: ${e.message}")
        }
    }

    fun setBassBoostStrength(strengthPercent: Float) {
        try {
            bassBoost?.let { bb ->
                val str = ((strengthPercent / 100f) * 1000f).toInt().coerceIn(0, 1000)
                bb.setStrength(str.toShort())
            }
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Failed to set bass boost: ${e.message}")
        }
    }

    fun applyPreset(presetIndex: Int) {
        try {
            equalizer?.usePreset(presetIndex.toShort())
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Failed to use preset: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Failed to set effects enabled: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            currentSessionId = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
