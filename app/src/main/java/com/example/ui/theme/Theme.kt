package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class MatrixThemeMode(val label: String, val subtitle: String) {
    MIDNIGHT_NEON("Midnight Neon", "Cyberpunk electric cyan & neon purple glow"),
    AQUA_LIQUID_GLASS("Aqua Liquid Glass", "Deep ocean cyan, translucent teal & aquatic flow"),
    DYNAMIC_ALBUM_ART("Dynamic Album Art", "Warm sunset magenta, solar amber & obsidian glow"),
    MATRIX_CYBER_GREEN("Matrix Cyber Green", "High-contrast digital emerald & cyber terminal dark")
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

private val DynamicAlbumArtColorScheme = darkColorScheme(
    primary = Color(0xFFF472B6),
    onPrimary = Color(0xFF0D0B14),
    secondary = Color(0xFFFB923C),
    background = Color(0xFF0D0B14),
    onBackground = Color.White,
    surface = Color(0xFF1C1924),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF282333),
    outline = Color(0xFF3B334C)
)

private val CyberGreenColorScheme = darkColorScheme(
    primary = Color(0xFF00FF66),
    onPrimary = Color(0xFF05100A),
    secondary = Color(0xFF10B981),
    background = Color(0xFF05100A),
    onBackground = Color.White,
    surface = Color(0xFF0A1F14),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF122E1F),
    outline = Color(0xFF1B4D33)
)

@Composable
fun MatrixPlayerTheme(
    themeMode: MatrixThemeMode = MatrixThemeMode.MIDNIGHT_NEON,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        MatrixThemeMode.AQUA_LIQUID_GLASS -> AquaColorScheme
        MatrixThemeMode.DYNAMIC_ALBUM_ART -> DynamicAlbumArtColorScheme
        MatrixThemeMode.MATRIX_CYBER_GREEN -> CyberGreenColorScheme
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
            glassCardBackground = Color(0xFF1C1924),
            glassBorder = Color(0xFF3B334C),
            backgroundDeep = Color(0xFF0D0B14),
            backgroundSurface = Color(0xFF1C1924),
            textPrimary = Color.White,
            textSecondary = Color(0xFFD4D4D8),
            textMuted = Color(0xFFA1A1AA),
            sliderTrack = Color(0xFF282333),
            progressRingGlow = Color(0xFFF472B6),
            waveformActive = Color(0xFFF472B6),
            waveformInactive = Color(0x40F472B6)
        )
        MatrixThemeMode.MATRIX_CYBER_GREEN -> MatrixThemeColors(
            primaryGradient = Brush.horizontalGradient(listOf(Color(0xFF00FF66), Color(0xFF10B981))),
            accentGlow = Color(0xFF00FF66),
            secondaryGlow = Color(0xFF10B981),
            glassCardBackground = Color(0xFF0A1F14),
            glassBorder = Color(0xFF1B4D33),
            backgroundDeep = Color(0xFF05100A),
            backgroundSurface = Color(0xFF0A1F14),
            textPrimary = Color.White,
            textSecondary = Color(0xFFA7F3D0),
            textMuted = Color(0xFF6EE7B7),
            sliderTrack = Color(0xFF122E1F),
            progressRingGlow = Color(0xFF00FF66),
            waveformActive = Color(0xFF00FF66),
            waveformInactive = Color(0x4000FF66)
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
