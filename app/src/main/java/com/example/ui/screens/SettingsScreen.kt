package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CrystalGlassCard
import com.example.ui.components.MatrixTopAppBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.LocalMatrixColors
import com.example.ui.theme.MatrixThemeMode

@Composable
fun SettingsScreen(
    currentTheme: MatrixThemeMode,
    onSelectTheme: (MatrixThemeMode) -> Unit,
    onNavigate: (Screen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalMatrixColors.current
    var gaplessPlayback by remember { mutableStateOf(true) }
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var autoResume by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
    ) {
        MatrixTopAppBar(
            title = "Settings",
            onBackClick = onBack
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Theme Switcher Card (Midnight Neon, Aqua Liquid Glass, Dynamic Album Art)
            item {
                Text(
                    text = "THEME SYSTEM (3 REAL MODES)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(MatrixThemeMode.MIDNIGHT_NEON) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == MatrixThemeMode.MIDNIGHT_NEON,
                                onClick = { onSelectTheme(MatrixThemeMode.MIDNIGHT_NEON) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentGlow)
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("1. Midnight Neon", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("Cyberpunk electric cyan & neon purple glow", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(MatrixThemeMode.AQUA_LIQUID_GLASS) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == MatrixThemeMode.AQUA_LIQUID_GLASS,
                                onClick = { onSelectTheme(MatrixThemeMode.AQUA_LIQUID_GLASS) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentGlow)
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("2. Aqua Liquid Glass", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("Deep ocean cyan, translucent teal & aquatic flow", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectTheme(MatrixThemeMode.DYNAMIC_ALBUM_ART) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentTheme == MatrixThemeMode.DYNAMIC_ALBUM_ART,
                                onClick = { onSelectTheme(MatrixThemeMode.DYNAMIC_ALBUM_ART) },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.accentGlow)
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text("3. Dynamic Album Art", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("Dynamic sunset purples and vibrant orange hues", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                        }
                    }
                }
            }

            // Audio & Playback Options
            item {
                Text(
                    text = "AUDIO & PLAYBACK ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                CrystalGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
                    borderColor = colors.glassBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Gapless Audio Playback", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("AndroidX Media3 low-latency buffer", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Switch(
                                checked = gaplessPlayback,
                                onCheckedChange = { gaplessPlayback = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.backgroundDeep, checkedTrackColor = colors.accentGlow)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hardware Decoder Preference", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("GPU accelerated video decoding", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Switch(
                                checked = hardwareAcceleration,
                                onCheckedChange = { hardwareAcceleration = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.backgroundDeep, checkedTrackColor = colors.accentGlow)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Resume Playback Position", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Text("Restore last audio/video timeline", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            Switch(
                                checked = autoResume,
                                onCheckedChange = { autoResume = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = colors.backgroundDeep, checkedTrackColor = colors.accentGlow)
                            )
                        }
                    }
                }
            }

            // About Application Navigation Card
            item {
                CrystalGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.About) },
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = colors.backgroundSurface.copy(alpha = 0.8f),
                    borderColor = colors.glassBorder
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("About Matrix Player", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                            Text("Version 1.0.0 • Architecture & Open Source", style = MaterialTheme.typography.bodySmall, color = colors.accentGlow)
                        }
                        Icon(Icons.Default.Info, contentDescription = null, tint = colors.accentGlow)
                    }
                }
            }
        }
    }
}
