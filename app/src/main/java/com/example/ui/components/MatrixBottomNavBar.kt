package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors

data class BottomNavItem(
    val screen: Screen,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String
)

/**
 * Floating GlassSurface-inspired MatrixBottomNavBar.
 *
 * Features:
 * - Subtle translucent blur / frosted glass backdrop with soft centered depth shadow
 * - Floating pill-shaped glass container active indicator with a soft glowing inner border
 * - Refined icon scale, 1x smaller crisp typography, and touch spring response
 * - Soft, floating glass spacers with realistic drop shadows between navigation tabs
 */
@Composable
fun MatrixBottomNavBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    activeMediaTitle: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    val navItems = remember {
        listOf(
            BottomNavItem(Screen.Home, Icons.Filled.Home, Icons.Outlined.Home, "Home"),
            BottomNavItem(Screen.MusicLibrary, Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "Music"),
            BottomNavItem(Screen.VideoLibrary, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, "Videos"),
            BottomNavItem(Screen.VisualizerStudio, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "Visuals"),
            BottomNavItem(Screen.Settings, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
        )
    }

    val selectedIndex = navItems.indexOfFirst { it.screen.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .navigationBarsPadding()
            .testTag("matrix_bottom_nav"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Diffused Floor Reflection & Glow Layer
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        ) {
            val totalWidth = size.width
            val tabWidth = totalWidth / navItems.size
            val activeCenterX = tabWidth * selectedIndex + (tabWidth / 2f)

            // Radiant ambient beam (reflects active tab accent)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentGlow.copy(alpha = 0.32f),
                        colors.secondaryGlow.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(activeCenterX, size.height * 0.5f),
                    radius = tabWidth * 1.25f
                ),
                radius = tabWidth * 1.25f,
                center = Offset(activeCenterX, size.height * 0.5f)
            )

            // Soft horizontal ambient floor streak
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        colors.accentGlow.copy(alpha = 0.14f)
                    )
                ),
                topLeft = Offset(activeCenterX - tabWidth * 0.7f, size.height * 0.65f),
                size = Size(tabWidth * 1.4f, size.height * 0.35f),
                cornerRadius = CornerRadius(14f, 14f)
            )
        }

        // Floating GlassSurface Container with Subtle Translucent Frosted Blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Soft centered depth shadow
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = size.width * 0.55f
                        ),
                        topLeft = Offset(0f, 6f),
                        size = Size(size.width, size.height + 8f),
                        cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx())
                    )
                }
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.12f), // Frosted top highlight
                            colors.glassCardBackground.copy(alpha = 0.82f),
                            colors.backgroundDeep.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.45f), // Specular light rim on top edge
                            colors.glassBorder.copy(alpha = 0.60f),
                            colors.accentGlow.copy(alpha = 0.30f)
                        )
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                val totalTabCount = navItems.size
                val tabWidthDp = maxWidth / totalTabCount
                val indicatorOffsetDp by animateDpAsState(
                    targetValue = tabWidthDp * selectedIndex,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = 380f
                    ),
                    label = "indicator_slide"
                )

                // Pill-shaped Glass Container Indicator with Soft Glowing Inner Border
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffsetDp)
                        .width(tabWidthDp)
                        .height(54.dp)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .drawBehind {
                            // Centered radial bloom behind active pill
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        colors.accentGlow.copy(alpha = 0.30f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = size.width * 0.85f
                                ),
                                radius = size.width * 0.85f,
                                center = Offset(cx, cy)
                            )
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    colors.accentGlow.copy(alpha = 0.22f),
                                    colors.secondaryGlow.copy(alpha = 0.10f),
                                    Color.Transparent
                                )
                            )
                        )
                        // Soft glowing outer border
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.60f),
                                    colors.accentGlow.copy(alpha = 0.75f),
                                    colors.secondaryGlow.copy(alpha = 0.50f)
                                )
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                ) {
                    // Soft Glowing Inner Border (Inset 1.5dp)
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val insetPx = 1.5.dp.toPx()
                        val innerRadius = 20.dp.toPx()
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colors.accentGlow.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.35f),
                                    colors.secondaryGlow.copy(alpha = 0.40f)
                                ),
                                start = Offset.Zero,
                                end = Offset(size.width, size.height)
                            ),
                            topLeft = Offset(insetPx, insetPx),
                            size = Size(size.width - insetPx * 2f, size.height - insetPx * 2f),
                            cornerRadius = CornerRadius(innerRadius, innerRadius),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                    }
                }

                // Row of Navigation Tabs separated by Glass Floating Spacers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = index == selectedIndex
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()

                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.88f else if (isSelected) 1.04f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                            label = "tab_scale"
                        )

                        val iconTint by animateColorAsState(
                            targetValue = if (isSelected) colors.accentGlow else colors.textSecondary.copy(alpha = 0.75f),
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "tab_icon_tint"
                        )

                        val textTint by animateColorAsState(
                            targetValue = if (isSelected) Color.White else colors.textMuted,
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "tab_text_tint"
                        )

                        // Nav Tab Item
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .scale(scale)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null
                                ) { onNavigate(item.screen) }
                                .padding(vertical = 3.dp)
                                .testTag("nav_${item.label.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.size(26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = textTint,
                                maxLines = 1
                            )
                        }

                        // Soft Floating Glass Spacer with drop shadow between menu icons
                        if (index < navItems.size - 1) {
                            GlassFloatingSpacer(
                                height = 20.dp,
                                tint = if (index == selectedIndex || index + 1 == selectedIndex)
                                    colors.accentGlow
                                else
                                    colors.textMuted.copy(alpha = 0.25f)
                            )
                        }
                    }
                }
            }
        }
    }
}
