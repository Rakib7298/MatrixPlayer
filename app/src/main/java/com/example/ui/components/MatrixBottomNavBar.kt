package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors

data class BottomNavItem(
    val screen: Screen,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector,
    val label: String
)

@Composable
fun MatrixBottomNavBar(
    currentRoute: String,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    val navItems = listOf(
        BottomNavItem(Screen.Home, Icons.Filled.Home, Icons.Outlined.Home, "Home"),
        BottomNavItem(Screen.MusicLibrary, Icons.Filled.MusicNote, Icons.Outlined.MusicNote, "Music"),
        BottomNavItem(Screen.VideoLibrary, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, "Videos"),
        BottomNavItem(Screen.VisualizerStudio, Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, "Visuals"),
        BottomNavItem(Screen.Settings, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
    )

    CrystalGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(26.dp),
        backgroundColor = colors.backgroundSurface.copy(alpha = 0.92f),
        borderColor = colors.glassBorder,
        borderWidth = 1.dp,
        testTag = "matrix_bottom_nav"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentRoute == item.screen.route
                val icon = if (isSelected) item.filledIcon else item.outlinedIcon

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(item.screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("nav_${item.label.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accentGlow.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.label,
                            tint = if (isSelected) colors.accentGlow else colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) colors.accentGlow else colors.textSecondary
                    )
                }
            }
        }
    }
}
