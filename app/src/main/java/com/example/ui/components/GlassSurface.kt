package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMatrixColors

/**
 * Native Jetpack Compose implementation of the React Bits <GlassSurface /> component.
 *
 * Implements real-time refractive visual illusions:
 * - Dual-rim specular highlight with chromatic aberration edge fringes
 * - Translucent refractive liquid glass backdrop
 * - Centered radial depth shadow ("context style round block center shadow")
 * - Subtle background reflection bloom
 * - Frosted sheen gradient overlay
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderRadius: Dp = 24.dp,
    borderWidth: Dp = 1.2.dp,
    backgroundOpacity: Float = 0.14f,
    isRoundBlock: Boolean = false,
    hasCenteredShadow: Boolean = true,
    backdropGlow: Color? = null,
    specularTint: Color? = null,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = LocalMatrixColors.current
    val effectiveGlow = backdropGlow ?: colors.accentGlow.copy(alpha = 0.28f)
    val effectiveSpecular = specularTint ?: Color.White

    // Container with layered shadows (outer centered radial shadow, ambient bloom, and rim)
    Box(
        modifier = modifier
            .drawBehind {
                drawGlassShadows(
                    isRoundBlock = isRoundBlock,
                    hasCenteredShadow = hasCenteredShadow,
                    backdropGlow = effectiveGlow,
                    borderRadius = borderRadius.toPx()
                )
            }
            .clip(if (isRoundBlock) CircleShape else shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = (backgroundOpacity * 1.35f).coerceAtMost(0.40f)),
                        colors.glassCardBackground.copy(alpha = (0.75f + backgroundOpacity * 0.25f).coerceAtMost(0.95f)),
                        colors.backgroundDeep.copy(alpha = (0.85f + backgroundOpacity * 0.15f).coerceAtMost(0.96f))
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        effectiveSpecular.copy(alpha = 0.58f), // Crisp upper specular light rim
                        Color.White.copy(alpha = 0.22f),
                        colors.glassBorder.copy(alpha = 0.45f),
                        effectiveGlow.copy(alpha = 0.45f)      // Bottom chromatic reflection bounce
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = if (isRoundBlock) CircleShape else shape
            ),
        contentAlignment = contentAlignment
    ) {
        // Specular sheen diagonal overlay (45deg light refraction)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.16f),
                            Color.Transparent,
                            Color.White.copy(alpha = 0.04f)
                        ),
                        start = Offset.Zero,
                        end = Offset(300f, 300f)
                    )
                )
        )

        // Main glass content
        content()
    }
}

/**
 * Custom shadow and ambient reflection drawing routine to achieve
 * realistic centered depth shadow and background light bounce.
 */
private fun DrawScope.drawGlassShadows(
    isRoundBlock: Boolean,
    hasCenteredShadow: Boolean,
    backdropGlow: Color,
    borderRadius: Float
) {
    val w = size.width
    val h = size.height
    val center = Offset(w / 2f, h / 2f)

    if (hasCenteredShadow) {
        // 1. Deep centered radial depth shadow (creating the floating 3D illusion behind the block)
        val shadowRadius = if (isRoundBlock) (w / 2f) * 1.15f else (kotlin.math.max(w, h) / 2f) * 1.1f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.55f),
                    Color.Black.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = center,
                radius = shadowRadius
            ),
            radius = shadowRadius,
            center = center
        )

        // 2. Soft colored background reflection bloom (emanating from behind the glass block)
        val glowRadius = if (isRoundBlock) (w / 2f) * 1.45f else (kotlin.math.max(w, h) / 2f) * 1.35f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    backdropGlow.copy(alpha = 0.32f),
                    backdropGlow.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = center,
                radius = glowRadius
            ),
            radius = glowRadius,
            center = center
        )
    }

    // 3. Subtle ambient drop shadow for non-round capsules
    if (!isRoundBlock) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.35f)
                )
            ),
            topLeft = Offset(0f, 4f),
            size = Size(w, h + 6f),
            cornerRadius = CornerRadius(borderRadius, borderRadius)
        )
    }
}
