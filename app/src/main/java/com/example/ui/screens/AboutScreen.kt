package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.theme.LocalMatrixColors

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("about_screen")
    ) {
        MatrixTopAppBar(
            title = "About Matrix Player",
            onBackClick = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(colors.primaryGradient)
                        .border(2.dp, colors.accentGlow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Logo",
                        tint = colors.backgroundDeep,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "MATRIX PLAYER",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    ),
                    color = colors.textPrimary
                )

                Text(
                    text = "v1.0.0 (Production Architecture)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accentGlow
                )
            }

            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.85f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "ARCHITECTURE & DESIGN SPECIFICATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.accentGlow
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "• Design System: Super Crystal Liquid Glass with translucent acrylic cards and glowing progress rings\n" +
                                    "• Themes: Midnight Neon, Aqua Liquid Glass & Dynamic Album Art\n" +
                                    "• Playback Core: AndroidX Media3 Audio & Video Engine\n" +
                                    "• Sound Processing: 10 Visualizer styles & 5/10-band EQ Studio with Bass Boost\n" +
                                    "• Architecture: Clean MVVM/MVI with Coroutines & StateFlow\n" +
                                    "• Offline Database: Android Room DB & Storage Access Framework\n" +
                                    "• Connectivity: Offline-first with M3U/M3U8 HLS streaming & GitHub Raw import",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            item {
                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.85f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "PRIVACY & OPEN SOURCE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.accentGlow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Matrix Player is strictly offline-first. Your local media, playback history, equalizer profiles, and custom playlists remain securely on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                }
            }
        }
    }
}
