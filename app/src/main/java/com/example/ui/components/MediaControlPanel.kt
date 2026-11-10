package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMatrixColors

/**
 * MediaControlPanel created with the GlassSurface design language.
 *
 * Employs:
 * - Refractive liquid glass outer container with dual specular highlights
 * - Round glass blocks for playback controls (Play, Pause, Skip Next, Skip Previous, Shuffle/Repeat, Rewind/Fast Forward)
 * - Centered radial depth shadows and subtle background reflections behind each control block
 * - Soft, floating glass spacers with realistic drop shadows between every icon block
 */
@Composable
fun MediaControlPanel(
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    isShuffle: Boolean = false,
    isRepeatOne: Boolean = false,
    onToggleShuffle: (() -> Unit)? = null,
    onToggleRepeat: (() -> Unit)? = null,
    onSeekRelative: ((Long) -> Unit)? = null,
    isVideoMode: Boolean = false
) {
    val colors = LocalMatrixColors.current

    // Outer Liquid Glass Container with chromatic bevels and refractive sheen
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(36.dp),
        borderRadius = 36.dp,
        borderWidth = 1.4.dp,
        backgroundOpacity = 0.16f,
        hasCenteredShadow = true,
        backdropGlow = colors.accentGlow.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: Shuffle (Audio) or Fast Rewind -10s (Video)
            if (isVideoMode) {
                RoundGlassControlButton(
                    icon = Icons.Default.Replay10,
                    contentDescription = "Rewind 10s",
                    size = 48.dp,
                    iconSize = 22.dp,
                    onClick = { onSeekRelative?.invoke(-10000L) ?: onPrevious() },
                    tint = colors.textPrimary,
                    testTag = "btn_video_rewind_10"
                )
            } else {
                RoundGlassControlButton(
                    icon = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    size = 46.dp,
                    iconSize = 20.dp,
                    isActive = isShuffle,
                    activeGlow = colors.secondaryGlow,
                    onClick = { onToggleShuffle?.invoke() },
                    tint = if (isShuffle) colors.secondaryGlow else colors.textMuted,
                    testTag = "btn_shuffle"
                )
            }

            // Soft Floating Glass Spacer
            GlassFloatingSpacer(height = 24.dp)

            // Button 2: Skip Previous
            RoundGlassControlButton(
                icon = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                size = 50.dp,
                iconSize = 24.dp,
                onClick = onPrevious,
                tint = colors.textPrimary,
                testTag = "btn_prev"
            )

            // Soft Floating Glass Spacer
            GlassFloatingSpacer(height = 30.dp)

            // Button 3: Hero Center Play/Pause Round Glass Block
            HeroCenterPlayPauseButton(
                isPlaying = isPlaying,
                onClick = onPlayPause,
                testTag = "btn_play_pause"
            )

            // Soft Floating Glass Spacer
            GlassFloatingSpacer(height = 30.dp)

            // Button 4: Skip Next
            RoundGlassControlButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Next",
                size = 50.dp,
                iconSize = 24.dp,
                onClick = onNext,
                tint = colors.textPrimary,
                testTag = "btn_next"
            )

            // Soft Floating Glass Spacer
            GlassFloatingSpacer(height = 24.dp)

            // Button 5: Repeat (Audio) or Fast Forward +10s (Video)
            if (isVideoMode) {
                RoundGlassControlButton(
                    icon = Icons.Default.FastForward,
                    contentDescription = "Fast Forward 10s",
                    size = 48.dp,
                    iconSize = 22.dp,
                    onClick = { onSeekRelative?.invoke(10000L) ?: onNext() },
                    tint = colors.textPrimary,
                    testTag = "btn_video_ff_10"
                )
            } else {
                RoundGlassControlButton(
                    icon = if (isRepeatOne) Icons.Default.RepeatOne else Icons.Default.Repeat,
                    contentDescription = "Repeat",
                    size = 46.dp,
                    iconSize = 20.dp,
                    isActive = isRepeatOne,
                    activeGlow = colors.secondaryGlow,
                    onClick = { onToggleRepeat?.invoke() },
                    tint = if (isRepeatOne) colors.secondaryGlow else colors.textMuted,
                    testTag = "btn_repeat"
                )
            }
        }
    }
}

/**
 * Individual Round Glass Control Block with centered depth shadow,
 * dual-rim specular highlights, subtle background reflection, and spring touch bounce.
 */
@Composable
fun RoundGlassControlButton(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    isActive: Boolean = false,
    activeGlow: Color? = null,
    testTag: String = ""
) {
    val colors = LocalMatrixColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
        label = "round_block_scale"
    )

    val effectiveGlow = if (isActive) (activeGlow ?: colors.accentGlow) else colors.accentGlow.copy(alpha = 0.22f)

    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            isRoundBlock = true,
            hasCenteredShadow = true,
            backdropGlow = effectiveGlow,
            borderWidth = if (isActive) 1.6.dp else 1.1.dp,
            backgroundOpacity = if (isActive) 0.26f else 0.12f,
            specularTint = if (isActive) colors.accentGlow else Color.White
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Hero Center Play/Pause Round Glass Block with deep centered shadow,
 * outer glowing glass rim, refractive liquid backdrop, and spring bounce.
 */
@Composable
fun HeroCenterPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "btn_play_pause"
) {
    val colors = LocalMatrixColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMedium),
        label = "hero_play_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(76.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            modifier = Modifier.size(74.dp),
            shape = CircleShape,
            isRoundBlock = true,
            hasCenteredShadow = true,
            backdropGlow = colors.accentGlow.copy(alpha = 0.55f),
            borderWidth = 1.8.dp,
            backgroundOpacity = 0.28f,
            specularTint = Color.White
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
