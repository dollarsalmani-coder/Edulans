package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.StudyLanguage
import com.example.model.SupportedLanguages
import com.example.model.ThemeMode
import com.example.ui.components.SampleFirebaseNoticeBanner
import com.example.ui.viewmodel.EduLensUiState

private val quickLanguageCodes = listOf("en", "es", "fr", "ar", "hi", "zh", "sw", "pt")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(
    uiState: EduLensUiState,
    onOpenLanguagePicker: () -> Unit,
    onSelectLanguage: (StudyLanguage) -> Unit,
    onSetSpeechRate: (Float) -> Unit,
    onTestVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSetLowBandwidth: (Boolean) -> Unit,
    onSetOfflineMode: (Boolean) -> Unit,
    onSetAutoSave: (Boolean) -> Unit,
    isDarkTheme: Boolean,
    onSelectThemeMode: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTestingVoice = uiState.voiceState.isSpeaking && uiState.voiceState.activeSectionId == "voice_test"
    var languageDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Global, Voice & Low-Bandwidth",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Designed for inclusive multilingual learning and reliable access on slow or offline connections.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reading Theme & Night Study",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Switch between warm daylight parchment and low-glare obsidian dark mode.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val themeModes = ThemeMode.entries
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            themeModes.forEachIndexed { index, mode ->
                                val selected = uiState.preferences.themeMode == mode
                                SegmentedButton(
                                    selected = selected,
                                    onClick = { onSelectThemeMode(mode) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = themeModes.size
                                    ),
                                    icon = {
                                        SegmentedButtonDefaults.Icon(active = selected) {
                                            Icon(
                                                imageVector = when (mode) {
                                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                                },
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag("theme_mode_${mode.name.lowercase()}")
                                ) {
                                    Text(mode.label, maxLines = 1)
                                }
                            }
                        }

                        AccessibilityToggleRow(
                            icon = Icons.Default.DarkMode,
                            title = "Night Study Dark Mode",
                            subtitle = if (isDarkTheme) {
                                "Active: reduced glare with soft mint & mist contrast."
                            } else {
                                "Switch on for comfortable reading in low-light rooms."
                            },
                            checked = isDarkTheme,
                            onCheckedChange = { enableDark ->
                                onSelectThemeMode(if (enableDark) ThemeMode.DARK else ThemeMode.LIGHT)
                            },
                            testTag = "night_study_dark_switch"
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Preferred Response & Voice Language",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Passed to analysis prompts and on-device voice readout.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenLanguagePicker,
                                modifier = Modifier.testTag("browse_all_languages_button")
                            ) {
                                Text("Search")
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = languageDropdownExpanded,
                            onExpandedChange = { languageDropdownExpanded = !languageDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${uiState.selectedLanguage.nativeName} (${uiState.selectedLanguage.englishName})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Preferred Language") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = languageDropdownExpanded)
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth()
                                    .testTag("preferred_language_dropdown")
                            )

                            ExposedDropdownMenu(
                                expanded = languageDropdownExpanded,
                                onDismissRequest = { languageDropdownExpanded = false }
                            ) {
                                SupportedLanguages.all.forEach { language ->
                                    val isSelected = language.code == uiState.selectedLanguage.code
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = "${language.nativeName} · ${language.englishName}",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                                Text(
                                                    text = "${language.regionLabel} · code: ${language.code}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectLanguage(language)
                                            languageDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("dropdown_lang_${language.code}")
                                    )
                                }
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Prompt Language Parameter: ${uiState.selectedLanguage.englishName} (${uiState.selectedLanguage.nativeName}, ${uiState.selectedLanguage.code})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            quickLanguageCodes.forEach { code ->
                                val lang = SupportedLanguages.findByCode(code)
                                FilterChip(
                                    selected = uiState.selectedLanguage.code == lang.code,
                                    onClick = { onSelectLanguage(lang) },
                                    label = { Text(lang.nativeName) },
                                    modifier = Modifier.testTag("quick_lang_${lang.code}")
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Voice Accessibility Readout",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (uiState.voiceState.isReady) {
                                        "On-device speech synthesizer ready for offline listening."
                                    } else {
                                        "Initializing device text-to-speech engine…"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Reading Speed",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                0.75f to "0.75x Slow",
                                1.0f to "1.0x Normal",
                                1.25f to "1.25x Fast"
                            ).forEach { (rate, label) ->
                                FilterChip(
                                    selected = uiState.preferences.voiceSpeechRate == rate,
                                    onClick = { onSetSpeechRate(rate) },
                                    label = { Text(label) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("voice_speed_${rate}")
                                )
                            }
                        }

                        Button(
                            onClick = { if (isTestingVoice) onStopVoice() else onTestVoice() },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("test_voice_button")
                        ) {
                            Icon(
                                imageVector = if (isTestingVoice) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isTestingVoice) {
                                    "Stop Voice Sample"
                                } else {
                                    "Test Voice in ${uiState.selectedLanguage.nativeName}"
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Low-Bandwidth & Offline Controls",
                            style = MaterialTheme.typography.titleMedium
                        )

                        AccessibilityToggleRow(
                            icon = Icons.Default.DataSaverOn,
                            title = "Low-Bandwidth Compression",
                            subtitle = "Shrinks uploaded photos to 480px lightweight JPEG (~20–45 KB) for 2G/3G areas.",
                            checked = uiState.preferences.lowBandwidthMode,
                            onCheckedChange = onSetLowBandwidth,
                            testTag = "low_bandwidth_switch"
                        )

                        AccessibilityToggleRow(
                            icon = Icons.Default.CloudOff,
                            title = "Offline Accessibility Mode",
                            subtitle = "Prioritize saved offline library guides with zero mobile data usage.",
                            checked = uiState.preferences.offlineAccessibilityMode,
                            onCheckedChange = onSetOfflineMode,
                            testTag = "offline_accessibility_switch"
                        )

                        AccessibilityToggleRow(
                            icon = Icons.Default.DownloadDone,
                            title = "Auto-Save Every Discovery Offline",
                            subtitle = "Automatically cache analyzed objects and compressed photos to your device.",
                            checked = uiState.preferences.autoSaveOffline,
                            onCheckedChange = onSetAutoSave,
                            testTag = "auto_save_switch"
                        )
                    }
                }
            }

            item {
                SampleFirebaseNoticeBanner()
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun AccessibilityToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}
