package com.example.media.visualizer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class AudioReactiveFrame(
    val waveform: FloatArray = FloatArray(64) { 0f },
    val fftMagnitudes: FloatArray = FloatArray(32) { 0f },
    val bassEnergy: Float = 0f,
    val midEnergy: Float = 0f,
    val trebleEnergy: Float = 0f,
    val isHardwareDriven: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioReactiveFrame
        return waveform.contentEquals(other.waveform) && fftMagnitudes.contentEquals(other.fftMagnitudes)
    }

    override fun hashCode(): Int {
        var result = waveform.contentHashCode()
        result = 31 * result + fftMagnitudes.contentHashCode()
        return result
    }
}

/**
 * Manages real Android Visualizer attached to audio session ID.
 * Captures real-time waveform byte arrays and FFT frequency bins.
 * Automatically releases hardware visualization resources when playback stops to save battery.
 */
class RealAudioVisualizerEngine(private val context: Context) {

    private var visualizer: Visualizer? = null
    private var currentSessionId: Int = 0

    private val _reactiveFrame = MutableStateFlow(AudioReactiveFrame())
    val reactiveFrame: StateFlow<AudioReactiveFrame> = _reactiveFrame.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        release()
        currentSessionId = audioSessionId

        // Check runtime RECORD_AUDIO permission before instantiating hardware Visualizer
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.i("AudioVisualizerEngine", "RECORD_AUDIO permission not granted. Falling back to synthetic visualizer.")
            _reactiveFrame.value = _reactiveFrame.value.copy(isHardwareDriven = false)
            return
        }

        try {
            val vis = Visualizer(audioSessionId)
            val captureRate = Visualizer.getMaxCaptureRate()
            val ranges = Visualizer.getCaptureSizeRange()
            val captureSize = if (ranges != null && ranges.size >= 2) {
                128.coerceAtMost(ranges[1])
            } else 128
            vis.captureSize = captureSize

            vis.setDataCaptureListener(
                object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (waveform != null && waveform.isNotEmpty()) {
                            processWaveform(waveform)
                        }
                    }

                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        if (fft != null && fft.isNotEmpty()) {
                            processFft(fft)
                        }
                    }
                },
                captureRate / 2, // 30-60 fps optimal refresh
                /* waveform = */ true,
                /* fft = */ true
            )

            vis.enabled = true
            visualizer = vis
            _reactiveFrame.value = _reactiveFrame.value.copy(isHardwareDriven = true)
        } catch (t: Throwable) {
            Log.w("AudioVisualizerEngine", "Hardware visualizer init: ${t.message}. Using synthetic audio processor.")
            try {
                visualizer?.release()
            } catch (_: Throwable) {}
            visualizer = null
            _reactiveFrame.value = _reactiveFrame.value.copy(isHardwareDriven = false)
        }
    }

    private fun processWaveform(rawWaveform: ByteArray) {
        val outWave = FloatArray(64)
        val step = (rawWaveform.size / 64).coerceAtLeast(1)
        for (i in 0 until 64) {
            val idx = (i * step).coerceIn(0, rawWaveform.size - 1)
            // Normalized byte (-128 to 127) to float (-1.0 to 1.0)
            val b = rawWaveform[idx].toInt()
            outWave[i] = ((b + 128) / 128f) - 1.0f
        }
        _reactiveFrame.value = _reactiveFrame.value.copy(
            waveform = outWave,
            isHardwareDriven = true
        )
    }

    private fun processFft(rawFft: ByteArray) {
        val magnitudes = FloatArray(32)
        val n = (rawFft.size / 2).coerceAtMost(32)
        var bassSum = 0f
        var midSum = 0f
        var trebleSum = 0f

        for (i in 0 until n) {
            val r = rawFft[i * 2].toFloat()
            val im = rawFft[i * 2 + 1].toFloat()
            val mag = (hypot(r.toDouble(), im.toDouble()) / 128.0).toFloat().coerceIn(0f, 1f)
            magnitudes[i] = mag

            when {
                i in 0..4 -> bassSum += mag
                i in 5..18 -> midSum += mag
                else -> trebleSum += mag
            }
        }

        _reactiveFrame.value = _reactiveFrame.value.copy(
            fftMagnitudes = magnitudes,
            bassEnergy = (bassSum / 5f).coerceIn(0f, 1f),
            midEnergy = (midSum / 14f).coerceIn(0f, 1f),
            trebleEnergy = (trebleSum / 13f).coerceIn(0f, 1f),
            isHardwareDriven = true
        )
    }

    fun updateSimulatedFrame(t: Float, isPlaying: Boolean) {
        if (_reactiveFrame.value.isHardwareDriven || !isPlaying) return

        val simWave = FloatArray(64) { i ->
            (sin(i * 0.2 + t * 0.1) * 0.6 + sin(i * 0.08 - t * 0.05) * 0.4).toFloat()
        }
        val simFft = FloatArray(32) { i ->
            (abs(sin((i * 0.4 + t * 0.15).toDouble())).toFloat() * 0.8f).coerceIn(0.1f, 1f)
        }

        _reactiveFrame.value = AudioReactiveFrame(
            waveform = simWave,
            fftMagnitudes = simFft,
            bassEnergy = 0.6f + 0.3f * sin(t.toDouble() * 0.2).toFloat(),
            midEnergy = 0.5f + 0.25f * cos(t.toDouble() * 0.15).toFloat(),
            trebleEnergy = 0.4f + 0.2f * sin(t.toDouble() * 0.3).toFloat(),
            isHardwareDriven = false
        )
    }

    fun stop() {
        try {
            visualizer?.enabled = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
            currentSessionId = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
