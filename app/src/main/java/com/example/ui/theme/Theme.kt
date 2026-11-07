package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class MatrixThemeMode {
    MIDNIGHT_NEON,
    AQUA_LIQUID_GLASS,
    DYNAMIC_ALBUM_ART
}

data class MatrixThemeColors(
    val primaryGradient: Brush,
    val accentGlow: Color,
    val secondaryGlow: Color,
    val glassCardBackground: Color,
    val glassBorder: Color,
    val backgroundDeep: Color,
    val backgroundSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val sliderTrack: Color,
    val progressRingGlow: Color,
    val waveformActive: Color,
    val waveformInactive: Color
)

val LocalMatrixColors = staticCompositionLocalOf {
    MatrixThemeColors(
        primaryGradient = Brush.horizontalGradient(listOf(RefPurpleGlow, RefBlueCyanGlow)),
        accentGlow = RefPurpleLight,
        secondaryGlow = RefBlueCyanGlow,
        glassCardBackground = RefCardDark,
        glassBorder = RefCardBorder,
        backgroundDeep = RefMidnightBg,
        backgroundSurface = RefCardDark,
        textPrimary = RefTextWhite,
        textSecondary = RefTextSecondary,
        textMuted = RefTextMuted,
        sliderTrack = RefSliderTrack,
        progressRingGlow = RefBlueCyanGlow,
        waveformActive = RefPurpleLight,
        waveformInactive = RefTextMuted.copy(alpha = 0.35f)
    )
}

private val MidnightColorScheme = darkColorScheme(
    primary = RefPurpleLight,
    onPrimary = RefMidnightBg,
    secondary = RefBlueCyanGlow,
    background = RefMidnightBg,
    onBackground = RefTextWhite,
    surface = RefCardDark,
    onSurface = RefTextWhite,
    surfaceVariant = RefCardInnerDark,
    outline = RefCardBorder
)

private val AquaColorScheme = darkColorScheme(
    primary = RefBlueCyanGlow,
    onPrimary = RefAquaOceanBg,
    secondary = Color(0xFF14B8A6),
    background = RefAquaOceanBg,
    onBackground = Color.White,
    surface = RefAquaGlassSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF063A57),
    outline = RefAquaGlassBorder
)

@Composable
fun MatrixPlayerTheme(
    themeMode: MatrixThemeMode = MatrixThemeMode.MIDNIGHT_NEON,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        MatrixThemeMode.AQUA_LIQUID_GLASS -> AquaColorScheme
        else -> MidnightColorScheme
    }

    val customColors = when (themeMode) {
        MatrixThemeMode.MIDNIGHT_NEON -> MatrixThemeColors(
            primaryGradient = Brush.horizontalGradient(listOf(RefPurpleGlow, RefBlueCyanGlow)),
            accentGlow = RefPurpleLight,
            secondaryGlow = RefBlueCyanGlow,
            glassCardBackground = RefCardDark,
            glassBorder = RefCardBorder,
            backgroundDeep = RefMidnightBg,
            backgroundSurface = RefCardDark,
            textPrimary = RefTextWhite,
            textSecondary = RefTextSecondary,
            textMuted = RefTextMuted,
            sliderTrack = RefSliderTrack,
            progressRingGlow = RefBlueCyanGlow,
            waveformActive = RefPurpleLight,
            waveformInactive = RefTextMuted.copy(alpha = 0.4f)
        )
        MatrixThemeMode.AQUA_LIQUID_GLASS -> MatrixThemeColors(
            primaryGradient = Brush.horizontalGradient(listOf(RefBlueCyanGlow, Color(0xFF14B8A6))),
            accentGlow = RefBlueCyanGlow,
            secondaryGlow = Color(0xFF67E8F9),
            glassCardBackground = RefAquaGlassSurface,
            glassBorder = RefAquaGlassBorder,
            backgroundDeep = RefAquaOceanBg,
            backgroundSurface = RefAquaGlassSurface,
            textPrimary = Color.White,
            textSecondary = Color(0xFFBAE6FD),
            textMuted = Color(0xFF7DD3FC),
            sliderTrack = Color(0x330284C7),
            progressRingGlow = Color.White,
            waveformActive = Color.White,
            waveformInactive = Color(0x66FFFFFF)
        )
        MatrixThemeMode.DYNAMIC_ALBUM_ART -> MatrixThemeColors(
            primaryGradient = Brush.horizontalGradient(listOf(Color(0xFFE879F9), Color(0xFFFB923C))),
            accentGlow = Color(0xFFF472B6),
            secondaryGlow = Color(0xFFFB923C),
            glassCardBackground = Color(0xFF18181B),
            glassBorder = Color(0xFF27272A),
            backgroundDeep = Color(0xFF09090B),
            backgroundSurface = Color(0xFF18181B),
            textPrimary = RefTextWhite,
            textSecondary = Color(0xFFA1A1AA),
            textMuted = Color(0xFF71717A),
            sliderTrack = Color(0xFF27272A),
            progressRingGlow = Color(0xFFF472B6),
            waveformActive = Color(0xFFF472B6),
            waveformInactive = Color(0x40F472B6)
        )
    }

    CompositionLocalProvider(LocalMatrixColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
