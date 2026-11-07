package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.media.visualizer.AudioReactiveFrame
import com.example.ui.theme.RefBlueCyanGlow
import com.example.ui.theme.RefEq7CyanGlow
import com.example.ui.theme.RefEq7SliderGlow
import com.example.ui.theme.RefPowerampNeonPurple
import com.example.ui.theme.RefPowerampOrangeGlow
import com.example.ui.theme.RefPurpleLight
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class EqMode(val label: String) {
    MODE_7_BAND_PRO("7-Band Pro"),
    MODE_10_BAND_STUDIO("10-Band Studio"),
    MODE_4_BAND("4-Band Neo")
}

data class GlassEqPreset(
    val name: String,
    val gains7: List<Float>,
    val gains10: List<Float>,
    val bassPercent: Float = 0.6f,
    val treblePercent: Float = 0.5f,
    val tag: String = "Preset"
)

val DefaultGlassPresets = listOf(
    GlassEqPreset("Custom", listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f), listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f), 0.6f, 0.5f, "User"),
    GlassEqPreset("Neon Cyber Bass", listOf(0.85f, 0.8f, 0.65f, 0.5f, 0.45f, 0.6f, 0.7f), listOf(0.9f, 0.85f, 0.75f, 0.6f, 0.5f, 0.45f, 0.5f, 0.55f, 0.65f, 0.7f), 0.85f, 0.6f, "Bass+"),
    GlassEqPreset("Crystal Liquid Vocal", listOf(0.4f, 0.45f, 0.6f, 0.75f, 0.85f, 0.65f, 0.55f), listOf(0.35f, 0.4f, 0.5f, 0.65f, 0.8f, 0.75f, 0.65f, 0.55f, 0.5f, 0.45f), 0.4f, 0.75f, "Vocal"),
    GlassEqPreset("Deep Ocean Sub", listOf(0.9f, 0.75f, 0.5f, 0.45f, 0.45f, 0.5f, 0.4f), listOf(0.95f, 0.85f, 0.7f, 0.55f, 0.45f, 0.4f, 0.4f, 0.45f, 0.5f, 0.45f), 0.95f, 0.4f, "Sub"),
    GlassEqPreset("Electronic Pulse", listOf(0.8f, 0.7f, 0.55f, 0.5f, 0.7f, 0.8f, 0.85f), listOf(0.8f, 0.75f, 0.6f, 0.5f, 0.45f, 0.6f, 0.7f, 0.8f, 0.85f, 0.9f), 0.75f, 0.8f, "Club"),
    GlassEqPreset("Acoustic Glass", listOf(0.45f, 0.5f, 0.55f, 0.6f, 0.6f, 0.65f, 0.75f), listOf(0.4f, 0.45f, 0.5f, 0.55f, 0.6f, 0.6f, 0.65f, 0.7f, 0.75f, 0.8f), 0.45f, 0.7f, "Air"),
    GlassEqPreset("Rock Concert", listOf(0.75f, 0.65f, 0.5f, 0.55f, 0.65f, 0.75f, 0.8f), listOf(0.75f, 0.7f, 0.6f, 0.5f, 0.55f, 0.65f, 0.7f, 0.75f, 0.8f, 0.85f), 0.7f, 0.75f, "Punch"),
    GlassEqPreset("3D Spatial Cinema", listOf(0.7f, 0.65f, 0.6f, 0.6f, 0.65f, 0.7f, 0.75f), listOf(0.7f, 0.65f, 0.6f, 0.6f, 0.65f, 0.65f, 0.7f, 0.75f, 0.8f, 0.8f), 0.8f, 0.8f, "Surround"),
    GlassEqPreset("Flat Pure", listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f), listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f), 0.5f, 0.5f, "Ref")
)

val Eq7FrequenciesList = listOf("60 HZ", "30 HZ", "15 HZ", "500 HZ", "1K HZ", "8K HZ", "16K HZ")
val Eq10FrequenciesList = listOf("31", "62", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")

/**
 * Super Advanced Modern Glass Style Equalizer Screen:
 * Clones the exact visual aesthetics of User Reference 1cffa135 (7-band with white ribbed pill knobs & rotary dials)
 * and Reference b0c765f1 (Poweramp 10-band studio view with live audio spectrum and tone controls).
 *
 * Implements:
 * - 7-Band Pro (UI Reference 1cffa135), 10-Band Studio (UI Reference b0c765f1), and 4-Band Neo modes.
 * - Live animated FFT frequency spectrum analyzer beneath sliders.
 * - Tactile 3D crystal glass sliders with glowing liquid fills and white ribbed capsule thumbs.
 * - Master Preamp slider with cyan/purple gradient fill.
 * - 3D Rotary Knobs: Bass Boost, Virtualizer 3D Surround, Treble Clarity, and Bypass Power push button.
 * - Modern Glass Presets row with custom preset creation and save modal dialog.
 * - Limiter / Tone toggles and animated Reset All.
 */
@Composable
fun EqStudioScreen(
    bands: List<Float>,
    bassBoost: Float,
    selectedPreset: String,
    onBandChange: (Int, Float) -> Unit,
    onBassBoostChange: (Float) -> Unit,
    onSelectPreset: (String) -> Unit,
    onBack: () -> Unit,
    reactiveFrame: AudioReactiveFrame? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var eqMode by remember { mutableStateOf(EqMode.MODE_7_BAND_PRO) }
    var selectedPresetName by remember { mutableStateOf(selectedPreset.ifEmpty { "Custom" }) }

    // Preamp / Master Gain
    var preampGainVal by remember { mutableFloatStateOf(0.5f) } // 0.0 to 1.0 (corresponds to -6dB to +6dB)

    // Rotary Knobs
    var bassKnobValue by remember { mutableFloatStateOf((bassBoost / 100f).coerceIn(0f, 1f)) }
    var virtualizerKnobValue by remember { mutableFloatStateOf(0.65f) }
    var trebleKnobValue by remember { mutableFloatStateOf(0.5f) }
    var isBypassed by remember { mutableStateOf(false) }
    var isLimiterEnabled by remember { mutableStateOf(true) }

    // 7-Band Gains (Reference 1cffa135)
    val bandGains7 = remember {
        mutableStateListOf(0.35f, 0.75f, 0.55f, 0.2f, 0.85f, 0.45f, 0.65f)
    }

    // 10-Band Gains (Reference b0c765f1)
    val bandGains10 = remember {
        mutableStateListOf(0.5f, 0.78f, 0.78f, 0.65f, 0.5f, 0.4f, 0.4f, 0.5f, 0.65f, 0.55f)
    }

    // 4-Band Gains
    val bandGains4 = remember {
        mutableStateListOf(0.7f, 0.4f, 0.65f, 0.45f)
    }

    // Custom Presets List
    val presetList = remember {
        mutableStateListOf<GlassEqPreset>().apply { addAll(DefaultGlassPresets) }
    }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetNameInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C14)) // Deep midnight obsidian background
            .testTag("eq_studio_screen")
    ) {
        // Ambient Liquid Glass Glow Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Cyan ambient refraction top-left
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(RefBlueCyanGlow.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(w * 0.15f, h * 0.2f),
                    radius = w * 0.6f
                )
            )

            // Purple neon refraction bottom-right
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(RefPowerampNeonPurple.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(w * 0.85f, h * 0.75f),
                    radius = w * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // TOP BAR: [< Back] Title with Subtitle & Reset Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back icon button with glass pill
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0x331E2235))
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable { onBack() }
                        .testTag("eq_back_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Center Title & Mode Switcher Pill
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Equalizer",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mode Switcher Glass Capsule: [7-Band Pro] [10-Band] [4-Band]
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xB3111422))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        EqMode.values().forEach { mode ->
                            val isSelected = eqMode == mode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.horizontalGradient(
                                                listOf(
                                                    RefBlueCyanGlow.copy(alpha = 0.35f),
                                                    RefPurpleLight.copy(alpha = 0.35f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                        }
                                    )
                                    .border(
                                        if (isSelected) 1.dp else 0.dp,
                                        if (isSelected) RefBlueCyanGlow.copy(alpha = 0.6f) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { eqMode = mode }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFFA1A1AA)
                                )
                            }
                        }
                    }
                }

                // Reset All Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x331E2235))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp))
                        .clickable {
                            bandGains7.indices.forEach { bandGains7[it] = 0.5f }
                            bandGains10.indices.forEach { bandGains10[it] = 0.5f }
                            bandGains4.indices.forEach { bandGains4[it] = 0.5f }
                            preampGainVal = 0.5f
                            selectedPresetName = "Flat Pure"
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset All",
                        tint = RefBlueCyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Reset All",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // PRESETS ROW (Exact style of User Reference 1cffa135)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )

                    Text(
                        text = "+ New Custom",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RefBlueCyanGlow,
                        modifier = Modifier.clickable {
                            newPresetNameInput = "Glass Custom ${presetList.size + 1}"
                            showSavePresetDialog = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Horizontal scrollable preset pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    presetList.forEach { preset ->
                        val isSelected = preset.name == selectedPresetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0x5500F0FF),
                                                Color(0x55B026FF)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(Color(0x33161A28), Color(0x33161A28))
                                        )
                                    }
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) {
                                        Brush.horizontalGradient(
                                            listOf(RefBlueCyanGlow, RefPurpleLight)
                                        )
                                    } else {
                                        Brush.linearGradient(listOf(Color(0x2BFFFFFF), Color(0x10FFFFFF)))
                                    },
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    selectedPresetName = preset.name
                                    // Apply gains to 7-band
                                    preset.gains7.forEachIndexed { idx, v ->
                                        if (idx in bandGains7.indices) bandGains7[idx] = v
                                    }
                                    // Apply gains to 10-band
                                    preset.gains10.forEachIndexed { idx, v ->
                                        if (idx in bandGains10.indices) bandGains10[idx] = v
                                    }
                                    bassKnobValue = preset.bassPercent
                                    trebleKnobValue = preset.treblePercent
                                    onSelectPreset(preset.name)
                                }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(RefBlueCyanGlow)
                                    )
                                }
                                Text(
                                    text = preset.name,
                                    fontSize = if (isSelected) 13.sp else 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            // MAIN EQUALIZER FREQUENCY BANDS CARD (Super Crystal Liquid Glass container)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xB30F1322)) // 70% Super Crystal Liquid Glass
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x66FFFFFF),
                                Color(0x2600F0FF),
                                Color(0x33B026FF),
                                Color(0x55FFFFFF)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    when (eqMode) {
                        EqMode.MODE_7_BAND_PRO -> {
                            // UI REFERENCE 1cffa135: 7-BAND PRO VIEW
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Eq7FrequenciesList.forEachIndexed { idx, freqLabel ->
                                    val progressVal = bandGains7.getOrElse(idx) { 0.5f }
                                    val dbValue = (progressVal - 0.5f) * 24f // -12dB to +12dB

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Numeric dB gain text
                                        Text(
                                            text = if (dbValue >= 0) "+${String.format("%.1f", dbValue)}" else String.format("%.1f", dbValue),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (dbValue > 0) RefBlueCyanGlow else Color(0xFF94A3B8)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Vertical Track trough with rounded capsule thumb
                                        Box(
                                            modifier = Modifier
                                                .width(28.dp)
                                                .height(175.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFF1B1D2A)),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            // Liquid Gradient Fill (Cyan base transitioning to Purple at top)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(progressVal)
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                RefEq7SliderGlow,
                                                                RefEq7CyanGlow
                                                            )
                                                        )
                                                    )
                                            )

                                            // Tactile White Ribbed Pill Knob Capsule (Cloned from Reference 1cffa135)
                                            val topPadding = ((1f - progressVal) * 140).dp
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .padding(top = topPadding)
                                                    .size(width = 26.dp, height = 34.dp)
                                                    .clip(RoundedCornerShape(13.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(13.dp))
                                                    .pointerInput(Unit) {
                                                        detectDragGestures { change, dragAmount ->
                                                            change.consume()
                                                            val delta = -dragAmount.y / 150f
                                                            val newGain = (bandGains7[idx] + delta).coerceIn(0.05f, 0.95f)
                                                            bandGains7[idx] = newGain
                                                            selectedPresetName = "Custom"
                                                            onBandChange(idx, (newGain - 0.5f) * 24f)
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                // 4 Horizontal Tactile Grip Lines
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.5.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    repeat(4) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(width = 13.dp, height = 1.5.dp)
                                                                .clip(RoundedCornerShape(1.dp))
                                                                .background(Color(0xFFCBD5E1))
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Frequency Label
                                        Text(
                                            text = freqLabel,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        EqMode.MODE_10_BAND_STUDIO -> {
                            // UI REFERENCE b0c765f1: 10-BAND STUDIO WITH LIVE SPECTRUM
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Eq10FrequenciesList.forEachIndexed { idx, freqLabel ->
                                        val pVal = bandGains10.getOrElse(idx) { 0.5f }
                                        val dbVal = (pVal - 0.5f) * 24f

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (dbVal >= 0) "+${String.format("%.1f", dbVal)}" else String.format("%.1f", dbVal),
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (dbVal > 0) RefPowerampOrangeGlow else Color(0xFFA78BFA)
                                            )

                                            Spacer(modifier = Modifier.height(3.dp))

                                            Box(
                                                modifier = Modifier
                                                    .width(20.dp)
                                                    .height(150.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color(0xFF161326)),
                                                contentAlignment = Alignment.BottomCenter
                                            ) {
                                                // Studio Orange-Purple Gradient Fill
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .fillMaxHeight(pVal)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    RefPowerampNeonPurple,
                                                                    RefPowerampOrangeGlow
                                                                )
                                                            )
                                                        )
                                                )

                                                // Thumb Capsule
                                                val topPad = ((1f - pVal) * 122).dp
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopCenter)
                                                        .padding(top = topPad)
                                                        .size(width = 18.dp, height = 26.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF2E2452))
                                                        .border(1.dp, RefPowerampNeonPurple, RoundedCornerShape(8.dp))
                                                        .pointerInput(Unit) {
                                                            detectDragGestures { change, dragAmount ->
                                                                change.consume()
                                                                val delta = -dragAmount.y / 130f
                                                                val nVal = (bandGains10[idx] + delta).coerceIn(0.05f, 0.95f)
                                                                bandGains10[idx] = nVal
                                                                selectedPresetName = "Custom"
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(width = 8.dp, height = 2.dp)
                                                            .background(Color.White)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Text(
                                                text = freqLabel,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFC4B5FD)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Live Animated FFT Frequency Spectrum Visualizer Bars (from Reference b0c765f1!)
                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x33000000))
                                ) {
                                    val barCount = 36
                                    val barWidth = size.width / (barCount * 1.4f)
                                    val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)

                                    for (i in 0 until barCount) {
                                        val fftMag = reactiveFrame?.fftMagnitudes?.getOrNull(i % (reactiveFrame.fftMagnitudes.size.coerceAtLeast(1))) ?: 0.3f
                                        val waveFactor = kotlin.math.abs(sin((i * 0.25f + (System.currentTimeMillis() % 2000) / 300f).toDouble())).toFloat()
                                        val dynamicHeight = (fftMag * 0.7f + waveFactor * 0.3f).coerceIn(0.1f, 1f) * size.height

                                        val x = i * (barWidth + spacing)
                                        val y = size.height - dynamicHeight

                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                listOf(RefPowerampOrangeGlow, RefPowerampNeonPurple, RefBlueCyanGlow)
                                            ),
                                            topLeft = Offset(x, y),
                                            size = Size(barWidth, dynamicHeight),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }

                        EqMode.MODE_4_BAND -> {
                            // 4-BAND NEO PARAMETRIC VIEW
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                listOf("60 Hz\nSub", "250 Hz\nBass", "1 kHz\nMid", "4 kHz\nTreble").forEachIndexed { idx, label ->
                                    val pVal = bandGains4.getOrElse(idx) { 0.5f }
                                    val dbVal = (pVal - 0.5f) * 24f

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (dbVal >= 0) "+${String.format("%.1f", dbVal)} dB" else "${String.format("%.1f", dbVal)} dB",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = RefBlueCyanGlow
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Box(
                                            modifier = Modifier
                                                .width(36.dp)
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(Color(0xFF1B1D2A)),
                                            contentAlignment = Alignment.BottomCenter
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .fillMaxHeight(pVal)
                                                    .background(
                                                        Brush.verticalGradient(listOf(RefBlueCyanGlow, RefPurpleLight))
                                                    )
                                            )

                                            val topPad = ((1f - pVal) * 116).dp
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopCenter)
                                                    .padding(top = topPad)
                                                    .size(width = 34.dp, height = 34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .border(2.dp, RefBlueCyanGlow, CircleShape)
                                                    .pointerInput(Unit) {
                                                        detectDragGestures { change, dragAmount ->
                                                            change.consume()
                                                            val delta = -dragAmount.y / 120f
                                                            bandGains4[idx] = (bandGains4[idx] + delta).coerceIn(0.05f, 0.95f)
                                                            selectedPresetName = "Custom"
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(RefBlueCyanGlow)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // HORIZONTAL MASTER PREAMP / GAIN TRACK (UI Reference 1cffa135)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xB3111422))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Preamp Gain",
                        tint = RefBlueCyanGlow,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Gradient Volume Slider Track with White Ribbed Thumb
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1B1D2A))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    preampGainVal = (offset.x / size.width).coerceIn(0.05f, 0.95f)
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    preampGainVal = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                }
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Cyan to Purple Gradient Fill
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = preampGainVal)
                                .height(24.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(RefEq7CyanGlow, RefEq7SliderGlow)
                                    )
                                )
                        )

                        // White Tactile Capsule Thumb
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = (preampGainVal * 250).coerceIn(0f, 250f).dp)
                                .size(width = 30.dp, height = 22.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(3) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 1.5.dp, height = 10.dp)
                                            .background(Color(0xFFCBD5E1))
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    val preampDb = (preampGainVal - 0.5f) * 12f
                    Text(
                        text = if (preampDb >= 0) "+${String.format("%.1f", preampDb)} dB" else "${String.format("%.1f", preampDb)} dB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.width(52.dp)
                    )
                }
            }

            // ROTARY KNOBS SECTION: Bass Boost, 3D Surround, Treble, and Bypass Button
            // (Cloned directly from User Reference 1cffa135)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xB3111422))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(vertical = 14.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Knob 1: Bass Boost
                GlassRotaryKnob(
                    label = "Bass Boost",
                    value = bassKnobValue,
                    activeColor = RefEq7CyanGlow,
                    onValueChange = { newVal ->
                        bassKnobValue = newVal
                        onBassBoostChange(newVal * 100f)
                    }
                )

                // Knob 2: 3D Surround Virtualizer
                GlassRotaryKnob(
                    label = "3D Surround",
                    value = virtualizerKnobValue,
                    activeColor = RefBlueCyanGlow,
                    onValueChange = { newVal ->
                        virtualizerKnobValue = newVal
                    }
                )

                // Knob 3: Treble Clarity
                GlassRotaryKnob(
                    label = "Treble Boost",
                    value = trebleKnobValue,
                    activeColor = RefEq7SliderGlow,
                    onValueChange = { newVal ->
                        trebleKnobValue = newVal
                    }
                )

                // Knob 4 / Push Button: Bypass Button (Circular with "PRESS" & LED Ring)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(66.dp)
                            .clip(CircleShape)
                            .background(if (isBypassed) Color(0xFF2E2438) else Color(0xFF141622))
                            .border(
                                2.dp,
                                if (isBypassed) Color(0xFFF43F5E) else Color(0x33FFFFFF),
                                CircleShape
                            )
                            .clickable { isBypassed = !isBypassed },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Bypass",
                                tint = if (isBypassed) Color(0xFFF43F5E) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBypassed) "BYPASS" else "PRESS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBypassed) Color(0xFFF43F5E) else Color(0xFFA1A1AA)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBypassed) "Bypassed" else "Active",
                        fontSize = 11.sp,
                        color = if (isBypassed) Color(0xFFF43F5E) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // STUDIO TOGGLES & LIMITER STATUS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Limiter Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isLimiterEnabled) Color(0x334ADE80) else Color(0x221E2235))
                        .border(1.dp, if (isLimiterEnabled) Color(0x664ADE80) else Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .clickable { isLimiterEnabled = !isLimiterEnabled }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isLimiterEnabled) Color(0xFF4ADE80) else Color(0xFF71717A))
                    )
                    Text(
                        text = if (isLimiterEnabled) "Anti-Clipping Limiter: ON" else "Limiter: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLimiterEnabled) Color(0xFF4ADE80) else Color(0xFFA1A1AA)
                    )
                }

                // Tone Engine Tag
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x331E2235))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, tint = RefBlueCyanGlow, modifier = Modifier.size(15.dp))
                    Text(
                        text = "32-bit Floating Precision",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            // SOLID WHITE / HIGH-CONTRAST GLASS BUTTON: [ 💾 Save Preset ]
            // (Cloned directly from User Reference 1cffa135)
            Button(
                onClick = {
                    newPresetNameInput = "My Custom Glass"
                    showSavePresetDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("btn_save_preset"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Preset",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // SAVE PRESET CRYSTAL GLASS DIALOG
        if (showSavePresetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.70f))
                    .clickable { showSavePresetDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xE6141828))
                        .border(
                            1.5.dp,
                            Brush.linearGradient(
                                listOf(Color(0x80FFFFFF), Color(0x4000F0FF), Color(0x50B026FF))
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .clickable(enabled = false) { }
                        .padding(22.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Save Equalizer Preset",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showSavePresetDialog = false }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Name your custom glass frequency curve:",
                            fontSize = 12.sp,
                            color = Color(0xFFA1A1AA)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newPresetNameInput,
                            onValueChange = { newPresetNameInput = it },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RefBlueCyanGlow,
                                unfocusedBorderColor = Color(0x44FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cancel",
                                color = Color(0xFFA1A1AA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showSavePresetDialog = false }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(RefBlueCyanGlow, RefPurpleLight)
                                        )
                                    )
                                    .clickable {
                                        if (newPresetNameInput.isNotBlank()) {
                                            val created = GlassEqPreset(
                                                name = newPresetNameInput.trim(),
                                                gains7 = bandGains7.toList(),
                                                gains10 = bandGains10.toList(),
                                                bassPercent = bassKnobValue,
                                                treblePercent = trebleKnobValue,
                                                tag = "User"
                                            )
                                            presetList.add(created)
                                            selectedPresetName = created.name
                                        }
                                        showSavePresetDialog = false
                                    }
                                    .padding(horizontal = 18.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Super Crystal Glass Rotary Knob:
 * Renders rotary tick marks around the circle perimeter, glowing active arc,
 * center metallic/acrylic cap with specular reflection, and rotating needle dot.
 */
@Composable
fun GlassRotaryKnob(
    label: String,
    value: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(Color(0xFF131522))
                .border(1.5.dp, Color(0xFF272A3C), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount.y / 110f
                        onValueChange((value + delta).coerceIn(0f, 1f))
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 3.dp.toPx()
                val radius = size.minDimension / 2 - 8.dp.toPx()
                val cx = size.width / 2
                val cy = size.height / 2

                // Tick marks around perimeter (from 135 deg to 405 deg)
                val totalTicks = 16
                for (i in 0..totalTicks) {
                    val angleDeg = 135f + (270f * (i.toFloat() / totalTicks))
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val rInner = radius + 2.dp.toPx()
                    val rOuter = radius + 6.dp.toPx()
                    val p1 = Offset(cx + rInner * cos(angleRad).toFloat(), cy + rInner * sin(angleRad).toFloat())
                    val p2 = Offset(cx + rOuter * cos(angleRad).toFloat(), cy + rOuter * sin(angleRad).toFloat())
                    drawLine(
                        color = if (i.toFloat() / totalTicks <= value) activeColor else Color(0x33FFFFFF),
                        start = p1,
                        end = p2,
                        strokeWidth = 1.2.dp.toPx()
                    )
                }

                // Inactive Track Arc
                drawArc(
                    color = Color(0x22FFFFFF),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active Glowing Arc
                drawArc(
                    color = activeColor,
                    startAngle = 135f,
                    sweepAngle = 270f * value,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Rotating Needle Indicator Dot
                val indicatorAngleRad = Math.toRadians((135f + 270f * value).toDouble())
                val dotR = radius - 7.dp.toPx()
                val dotX = cx + dotR * cos(indicatorAngleRad).toFloat()
                val dotY = cy + dotR * sin(indicatorAngleRad).toFloat()

                drawCircle(Color.White, radius = 3.5.dp.toPx(), center = Offset(dotX, dotY))
                drawCircle(activeColor.copy(alpha = 0.5f), radius = 6.dp.toPx(), center = Offset(dotX, dotY))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFFA1A1AA),
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "${(value * 100).toInt()}%",
            fontSize = 10.sp,
            color = activeColor,
            fontWeight = FontWeight.Bold
        )
    }
}
