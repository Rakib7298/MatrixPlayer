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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Ultra-sleek Liquid Glass Floating Navigation Bar with Netflix-poster ambient
 * reflection optics, specular rim refraction, spring-animated sliding pill,
 * and reactive tab interaction.
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
        // Dynamic Netflix-Poster Ambient Specular Back-Reflection
        // Casts a diffused radiant reflection bloom under the glass dock aligned with active tab
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
        ) {
            val totalWidth = size.width
            val tabWidth = totalWidth / navItems.size
            val activeCenterX = tabWidth * selectedIndex + (tabWidth / 2f)

            // Radiant ambient beam (poster / artwork reflection onto the surface)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentGlow.copy(alpha = 0.38f),
                        colors.secondaryGlow.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(activeCenterX, size.height * 0.5f),
                    radius = tabWidth * 1.35f
                ),
                radius = tabWidth * 1.35f,
                center = Offset(activeCenterX, size.height * 0.5f)
            )

            // Bottom ambient floor reflection
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        colors.accentGlow.copy(alpha = 0.12f),
                        colors.secondaryGlow.copy(alpha = 0.22f)
                    )
                ),
                topLeft = Offset(activeCenterX - tabWidth * 0.8f, size.height * 0.65f),
                size = Size(tabWidth * 1.6f, size.height * 0.35f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
            )
        }

        // Main Liquid Glass Floating Dock
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.glassCardBackground.copy(alpha = 0.82f),
                            colors.backgroundDeep.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.42f), // Specular light rim on top edge
                            colors.glassBorder.copy(alpha = 0.55f),
                            colors.accentGlow.copy(alpha = 0.35f) // Subtle colored bounce at bottom rim
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
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

            // Sliding Liquid Glass Active Pill with Glowing Specular Halo
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffsetDp)
                    .width(tabWidthDp)
                    .height(58.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.accentGlow.copy(alpha = 0.28f),
                                colors.secondaryGlow.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                colors.accentGlow.copy(alpha = 0.65f),
                                Color.White.copy(alpha = 0.25f),
                                colors.secondaryGlow.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // Navigation Tab Items Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()

                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.88f else if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMedium),
                        label = "tab_scale"
                    )

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) colors.accentGlow else colors.textSecondary.copy(alpha = 0.75f),
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "tab_icon_tint"
                    )

                    val textTint by animateColorAsState(
                        targetValue = if (isSelected) Color.White else colors.textMuted,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                        label = "tab_text_tint"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onNavigate(item.screen) }
                            .padding(vertical = 4.dp)
                            .testTag("nav_${item.label.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Active icon subtle glow backdrop
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(colors.accentGlow.copy(alpha = 0.35f))
                                )
                            }
                            Icon(
                                imageVector = if (isSelected) item.filledIcon else item.outlinedIcon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = textTint,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
