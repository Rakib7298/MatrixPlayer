package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Reference 1: Midnight Echo Deep Dark Theme
val RefMidnightBg = Color(0xFF10131A)
val RefCardDark = Color(0xFF161B24)
val RefCardInnerDark = Color(0xFF13171F)
val RefCardBorder = Color(0xFF222938)
val RefPurpleGlow = Color(0xFF8B5CF6)
val RefPurpleLight = Color(0xFFA78BFA)
val RefBlueCyanGlow = Color(0xFF38BDF8)
val RefDeepBlueGlow = Color(0xFF2563EB)
val RefSliderTrack = Color(0xFF1E2533)
val RefTextWhite = Color(0xFFF8FAFC)
val RefTextMuted = Color(0xFF64748B)
val RefTextSecondary = Color(0xFF94A3B8)

val TextPrimary = RefTextWhite
val TextSecondary = RefTextSecondary
val TextTertiary = RefTextMuted

// Reference 2: Aqua Liquid Glass
val RefAquaOceanBg = Color(0xFF032B42)
val RefAquaGlassSurface = Color(0x66023D5E)
val RefAquaGlassBorder = Color(0x8038BDF8)
val RefAquaWaveActive = Color(0xFFFFFFFF)
val RefAquaWaveInactive = Color(0x66FFFFFF)

// Reference 3: Vision Glass Video Player
val RefVideoGlassPill = Color(0x4D2A2A2E)
val RefVideoGlassBorder = Color(0x40FFFFFF)
val RefVideoHighlight = Color(0xFFE2E8F0)

// Reference 4: 7-Band Equalizer & Knobs
val RefEq7TrackBg = Color(0xFF18181B)
val RefEq7SliderGlow = Color(0xFFA855F7)
val RefEq7CyanGlow = Color(0xFF06B6D4)
val RefEq7WhitePill = Color(0xFFF4F4F5)
val RefEq7KnobRing = Color(0xFF27272A)
val RefEq7KnobCenter = Color(0xFF121214)

// Reference 6: Poweramp-style 10-Band EQ & Purple Neon Glow
val RefPowerampBg = Color(0xFF0D0B18)
val RefPowerampSurface = Color(0xFF151226)
val RefPowerampBorder = Color(0xFF2B2245)
val RefPowerampOrangeGlow = Color(0xFFFF5722)
val RefPowerampNeonPurple = Color(0xFFB026FF)
val RefPowerampKnobDark = Color(0xFF1C1733)

// Reference 9: Lavender Crystal Glass
val RefLavenderBg = Color(0xFFDDD6FE)
val RefLavenderGlass = Color(0x4DFFFFFF)
val RefLavenderGlassBorder = Color(0x80FFFFFF)
val RefLavenderPurple = Color(0xFF7C3AED)
val RefLavenderLight = Color(0xFFC4B5FD)
val RefLavenderDark = Color(0xFF2E1065)

// Gradients
val RefArcGradient = Brush.sweepGradient(
    listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFF8B5CF6))
)

val RefSliderProgressGradient = Brush.horizontalGradient(
    listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
)

val RefEq7Gradient = Brush.verticalGradient(
    listOf(RefEq7SliderGlow, RefEq7CyanGlow)
)

val RefPowerampGradient = Brush.verticalGradient(
    listOf(RefPowerampNeonPurple, RefPowerampOrangeGlow)
)
