package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.media.subtitle.MatrixSubtitleManager
import com.example.media.subtitle.OnlineSubtitleResult
import com.example.ui.theme.RefBlueCyanGlow
import com.example.ui.theme.RefPurpleLight

val AvailableTargetLanguages = listOf("English(cc)", "Spanish(Español)", "Japanese(日本語)", "French(Français)", "German(Deutsch)")
val AvailableMotherLanguages = listOf("Bengali(বাংলা)", "Hindi(हिन्दी)", "Arabic(العربية)", "Chinese(中文)", "Korean(한국어)", "Spanish(Español)")
val AvailableFontSizes = listOf("18", "20", "22", "25", "28", "32", "36")
val AvailableEncodings = listOf("Universal (UTF-8)", "UTF-16 Little Endian", "ASCII", "Windows-1252", "ISO-8859-1")

val UpperColorPalette = listOf(Color.White, Color(0xFFFEF08A), Color(0xFF67E8F9), Color(0xFF86EFAC))
val LowerColorPalette = listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFA855F7), Color(0xFFEC4899))

/**
 * Pixel-perfect clone of UI Reference 1cb5f32d (LLNY Setting Menu) & Reference b35ecc5e (Dual Subtitle Studio):
 * - Top modal header: "LLNY Setting Menu" with [x] dismiss button.
 * - Tabs: Subtitles, Tools, Shortcuts, Appearance, Rate Us, Account.
 * - Upper Subtitles (Target Language) row: Language dropdown, Font Size dropdown, Color swatch, Download icon.
 * - Lower Subtitles (Mother Language) row: Language dropdown, Font Size dropdown, Color swatch, Download icon.
 * - [Advanced Setting] button: ±50ms precision subtitle sync stepper, text shadow, encoding, and background opacity.
 * - Live Dual Subtitle preview matching Reference 1cb5f32d:
 *   "The losers find problem in every answer but the winners find an answer in every problem."
 * - Online Subtitle Search & Download sheet.
 */
@Composable
fun SubtitleManagerScreen(
    subtitleManager: MatrixSubtitleManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by subtitleManager.settings.collectAsStateWithLifecycle()
    val onlineResults by subtitleManager.onlineResults.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Subtitles") }
    var isAdvancedOpen by remember { mutableStateOf(false) }
    var showOnlineSearchSheet by remember { mutableStateOf(false) }
    var targetLanguageForDownload by remember { mutableStateOf(true) } // true = Upper, false = Lower

    // Dropdown expansion state
    var showUpperLangDropdown by remember { mutableStateOf(false) }
    var showLowerLangDropdown by remember { mutableStateOf(false) }
    var showUpperSizeDropdown by remember { mutableStateOf(false) }
    var showLowerSizeDropdown by remember { mutableStateOf(false) }
    var showUpperColorPicker by remember { mutableStateOf(false) }
    var showLowerColorPicker by remember { mutableStateOf(false) }
    var showEncodingDropdown by remember { mutableStateOf(false) }

    // Online search query
    var searchQuery by remember { mutableStateOf("The Studio") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117)) // Deep movie theater backdrop
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("subtitle_manager_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onBack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x331E2438))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { showOnlineSearchSheet = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = RefBlueCyanGlow, modifier = Modifier.size(16.dp))
                        Text("Search Subtitles", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // LLNY SETTING MENU MODAL CARD (Exact visual clone of UI Reference 1cb5f32d)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xB3181B26)) // 70% Super Crystal Liquid Glass
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x60FFFFFF),
                                Color(0x3300F0FF),
                                Color(0x44B026FF),
                                Color(0x50FFFFFF)
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row: "LLNY Setting Menu" with [X] Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LLNY Setting Menu",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0x33282D42))
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Navigation Row: [Subtitles (Active)] [Tools] [Shortcuts] [Appearance] [Rate Us] [Account]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Subtitles", "Tools", "Shortcuts", "Appearance", "Rate Us", "Account").forEach { tabName ->
                            val isSelected = tabName == activeTab
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color(0xFF383B47) else Color.Transparent
                                    )
                                    .clickable { activeTab = tabName }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = tabName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF71717A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // SUBTITLES TABLE HEADER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "",
                            modifier = Modifier.weight(1.8f)
                        )
                        Text(
                            text = "Language",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.weight(1.6f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Font Size",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Color",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.weight(1.0f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Download",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFA1A1AA),
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ROW 1: Upper Subtitles (Target Language)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upper Subtitles (Target Language)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.weight(1.8f)
                        )

                        // Language Dropdown Selector
                        Box(
                            modifier = Modifier
                                .weight(1.6f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262933))
                                    .clickable { showUpperLangDropdown = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(settings.upperLanguage, color = Color.White, fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showUpperLangDropdown,
                                onDismissRequest = { showUpperLangDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                AvailableTargetLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = Color.White) },
                                        onClick = {
                                            subtitleManager.setUpperLanguage(lang)
                                            showUpperLangDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Font Size Selector
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262933))
                                    .clickable { showUpperSizeDropdown = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("${settings.upperFontSizeSp}", color = Color.White, fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showUpperSizeDropdown,
                                onDismissRequest = { showUpperSizeDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                AvailableFontSizes.forEach { sz ->
                                    DropdownMenuItem(
                                        text = { Text(sz, color = Color.White) },
                                        onClick = {
                                            subtitleManager.setUpperFontSize(sz.toIntOrNull() ?: 25)
                                            showUpperSizeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Color Swatch Box (White)
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(settings.upperColor)
                                    .border(1.dp, Color(0xFF6B7280), RoundedCornerShape(6.dp))
                                    .clickable { showUpperColorPicker = true }
                            )

                            DropdownMenu(
                                expanded = showUpperColorPicker,
                                onDismissRequest = { showUpperColorPicker = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                UpperColorPalette.forEach { color ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                                                Text(if (color == Color.White) "Pure White" else "Neon Color", color = Color.White)
                                            }
                                        },
                                        onClick = {
                                            subtitleManager.setUpperColor(color)
                                            showUpperColorPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Download Cloud Icon
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33374151))
                                    .clickable {
                                        targetLanguageForDownload = true
                                        showOnlineSearchSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download Upper", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // ROW 2: Lower Subtitles (Mother Language)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lower Subtitles (Mother Language)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.weight(1.8f)
                        )

                        // Language Dropdown Selector
                        Box(
                            modifier = Modifier
                                .weight(1.6f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262933))
                                    .clickable { showLowerLangDropdown = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(settings.lowerLanguage, color = Color.White, fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showLowerLangDropdown,
                                onDismissRequest = { showLowerLangDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                AvailableMotherLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = Color.White) },
                                        onClick = {
                                            subtitleManager.setLowerLanguage(lang)
                                            showLowerLangDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Font Size Selector
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF262933))
                                    .clickable { showLowerSizeDropdown = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("${settings.lowerFontSizeSp}", color = Color.White, fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showLowerSizeDropdown,
                                onDismissRequest = { showLowerSizeDropdown = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                AvailableFontSizes.forEach { sz ->
                                    DropdownMenuItem(
                                        text = { Text(sz, color = Color.White) },
                                        onClick = {
                                            subtitleManager.setLowerFontSize(sz.toIntOrNull() ?: 22)
                                            showLowerSizeDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Color Swatch Box (Red / Coral from Reference 1cb5f32d)
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(settings.lowerColor)
                                    .border(1.dp, Color(0xFF6B7280), RoundedCornerShape(6.dp))
                                    .clickable { showLowerColorPicker = true }
                            )

                            DropdownMenu(
                                expanded = showLowerColorPicker,
                                onDismissRequest = { showLowerColorPicker = false },
                                modifier = Modifier.background(Color(0xFF1E2230))
                            ) {
                                LowerColorPalette.forEach { color ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
                                                Text(if (color == Color(0xFFEF4444)) "Crimson Red" else "Accent Color", color = Color.White)
                                            }
                                        },
                                        onClick = {
                                            subtitleManager.setLowerColor(color)
                                            showLowerColorPicker = false
                                        }
                                    )
                                }
                            }
                        }

                        // Download Cloud Icon
                        Box(
                            modifier = Modifier
                                .weight(1.1f)
                                .padding(horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x33374151))
                                    .clickable {
                                        targetLanguageForDownload = false
                                        showOnlineSearchSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Download Lower", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // ADVANCED SETTING BUTTON (Exact clone of Reference 1cb5f32d)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2B2E38))
                                .border(1.dp, Color(0xFF3F424E), RoundedCornerShape(12.dp))
                                .clickable { isAdvancedOpen = !isAdvancedOpen }
                                .padding(horizontal = 24.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = if (isAdvancedOpen) "Hide Advanced Settings" else "Advanced Setting",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // EXPANDABLE ADVANCED SETTINGS PANEL: Sync ±50ms, Shadow, Encoding
                    AnimatedVisibility(
                        visible = isAdvancedOpen,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x6611131C))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text("Synchronization & Encoding (±50ms Precision)", color = RefBlueCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Upper Subtitle Sync Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Upper Subtitle Sync Offset:", color = Color.White, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0x33FFFFFF))
                                            .clickable { subtitleManager.adjustUpperDelay(-50L) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("-50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${settings.upperDelayMs} ms", color = RefBlueCyanGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0x33FFFFFF))
                                            .clickable { subtitleManager.adjustUpperDelay(50L) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("+50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Lower Subtitle Sync Stepper
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lower Subtitle Sync Offset:", color = Color.White, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0x33FFFFFF))
                                            .clickable { subtitleManager.adjustLowerDelay(-50L) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("-50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${settings.lowerDelayMs} ms", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(Color(0x33FFFFFF))
                                            .clickable { subtitleManager.adjustLowerDelay(50L) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("+50ms", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Character Encoding (Universal UTF-8)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Character Encoding:", color = Color.White, fontSize = 12.sp)
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF262933))
                                            .clickable { showEncodingDropdown = true }
                                            .padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(settings.encoding, color = RefBlueCyanGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                                    }

                                    DropdownMenu(
                                        expanded = showEncodingDropdown,
                                        onDismissRequest = { showEncodingDropdown = false },
                                        modifier = Modifier.background(Color(0xFF1E2230))
                                    ) {
                                        AvailableEncodings.forEach { enc ->
                                            DropdownMenuItem(
                                                text = { Text(enc, color = Color.White) },
                                                onClick = {
                                                    subtitleManager.setEncoding(enc)
                                                    showEncodingDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // LIVE VIDEO SUBTITLE RENDERING PREVIEW (Exact match of Reference 1cb5f32d)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xE60A0C14))
                    .border(1.5.dp, Color(0x33FFFFFF), RoundedCornerShape(22.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "LIVE DUAL SUBTITLE PREVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF64748B)
                    )

                    // Upper Target Language Subtitle (English CC)
                    if (settings.upperEnabled) {
                        Text(
                            text = "The losers find problem in every answer but the winners find an answer in every problem.",
                            fontSize = settings.upperFontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = settings.upperColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = settings.backgroundOpacity))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Lower Mother Language Subtitle (Bengali Translation)
                    if (settings.lowerEnabled) {
                        Text(
                            text = "পরাজিতরা প্রতিটি উত্তরে সমস্যা খুঁজে পায় কিন্তু বিজয়ীরা প্রতিটি সমস্যায় উত্তর খুঁজে পায়।",
                            fontSize = settings.lowerFontSizeSp.sp,
                            fontWeight = FontWeight.Bold,
                            color = settings.lowerColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = settings.backgroundOpacity))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ONLINE SUBTITLE SEARCH & DOWNLOAD DIALOG
        if (showOnlineSearchSheet) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable { showOnlineSearchSheet = false },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(440.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF161924))
                        .border(1.5.dp, Color(0x5500F0FF), RoundedCornerShape(26.dp))
                        .clickable(enabled = false) { }
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Online Subtitle Search (OpenSubtitles)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showOnlineSearchSheet = false }
                            )
                        }

                        Text(
                            text = "Target Track: ${if (targetLanguageForDownload) "Upper (Target)" else "Lower (Mother)"}",
                            color = RefBlueCyanGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Search Input
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by Movie or Video title...", color = Color(0xFFA1A1AA)) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = RefBlueCyanGlow) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RefBlueCyanGlow,
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Online Results List
                        Text("Available Matches (${onlineResults.size}):", color = Color(0xFFA1A1AA), fontSize = 11.sp, fontWeight = FontWeight.Bold)

                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(onlineResults) { res ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x33282D42))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(res.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${res.language} • ${res.format} • ${res.downloads} downloads", color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Brush.horizontalGradient(listOf(RefBlueCyanGlow, RefPurpleLight)))
                                            .clickable {
                                                subtitleManager.applyOnlineSubtitle(res, targetLanguageForDownload)
                                                showOnlineSearchSheet = false
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Import", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = "Close",
                                color = Color(0xFFA1A1AA),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { showOnlineSearchSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
