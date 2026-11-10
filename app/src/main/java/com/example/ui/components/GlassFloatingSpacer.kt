package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalMatrixColors

/**
 * Soft, floating glass spacer with realistic centered drop shadow,
 * subtle specular vertical highlight, and frosted refraction.
 * Separates control icons and navigation menu items.
 */
@Composable
fun GlassFloatingSpacer(
    modifier: Modifier = Modifier,
    width: Dp = 2.dp,
    height: Dp = 26.dp,
    tint: Color? = null
) {
    val colors = LocalMatrixColors.current
    val effectiveTint = tint ?: colors.accentGlow

    Box(
        modifier = modifier
            .width(width + 4.dp)
            .height(height + 4.dp)
            .drawBehind {
                // Realistic centered soft drop shadow
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.height * 0.75f
                    ),
                    radius = size.height * 0.75f,
                    center = Offset(cx, cy)
                )

                // Colored ambient glow dot
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            effectiveTint.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.height * 0.5f
                    ),
                    radius = size.height * 0.5f,
                    center = Offset(cx, cy)
                )
            }
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f), // Top specular dot
                        colors.glassBorder.copy(alpha = 0.65f),
                        effectiveTint.copy(alpha = 0.35f),
                        Color.Transparent
                    )
                )
            )
            .border(
                width = 0.5.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    )
}
