package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.StudyLanguage
import com.example.model.SupportedLanguages
import com.example.ui.theme.studySemanticColors
import java.io.File

@Composable
fun StudyImageView(
    imageSource: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        imageSource == null -> {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        imageSource.startsWith("drawable://") -> {
            val resId = when (imageSource.removePrefix("drawable://")) {
                "img_sample_leaf" -> R.drawable.img_sample_leaf
                "img_sample_watch" -> R.drawable.img_sample_watch
                "img_sample_solar" -> R.drawable.img_sample_solar
                else -> R.drawable.img_hero_banner
            }
            Image(
                painter = painterResource(id = resId),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        else -> {
            val file = File(imageSource)
            if (file.exists()) {
                AsyncImage(
                    model = file,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = modifier
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun SampleFirebaseNoticeBanner(
    modifier: Modifier = Modifier
) {
    val semantic = studySemanticColors()
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sample_ai_notice_banner"),
        shape = RoundedCornerShape(16.dp),
        color = semantic.noticeContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = semantic.noticeAmber,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.sample_ai_notice_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = semantic.noticeAmber,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.sample_ai_notice_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.noticeAmber.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun ConnectivityStatusPill(
    isOfflineMode: Boolean,
    isNetworkConnected: Boolean,
    isMetered: Boolean,
    lowBandwidthMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = studySemanticColors()
    val label = when {
        isOfflineMode -> "Offline Mode"
        !isNetworkConnected -> "No Internet"
        lowBandwidthMode -> "Low-Bandwidth"
        isMetered -> "Metered"
        else -> "Ready"
    }
    val containerColor = when {
        isOfflineMode || !isNetworkConnected -> semantic.offlineContainer
        lowBandwidthMode -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        isOfflineMode || !isNetworkConnected -> semantic.offlineGreen
        lowBandwidthMode -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val icon = when {
        isOfflineMode || !isNetworkConnected -> Icons.Default.CloudOff
        lowBandwidthMode -> Icons.Default.DataSaverOn
        else -> Icons.Default.Wifi
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .testTag("connectivity_status_pill"),
        color = containerColor,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ThemeQuickToggleButton(
    isDarkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .testTag("theme_toggle_button")
    ) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Night Study Dark Mode",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun LanguageQuickButton(
    selectedLanguage: StudyLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .testTag("language_picker_button"),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Select language",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = selectedLanguage.nativeName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VoiceSectionButton(
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = { if (isSpeakingThis) onStop() else onSpeak() },
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(
                if (isSpeakingThis) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                }
            )
    ) {
        Icon(
            imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
            contentDescription = contentDescription,
            tint = if (isSpeakingThis) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            },
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun OfflineReadyBadge(modifier: Modifier = Modifier) {
    val semantic = studySemanticColors()
    Surface(
        modifier = modifier,
        color = semantic.offlineContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.OfflinePin,
                contentDescription = null,
                tint = semantic.offlineGreen,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = stringResource(R.string.saved_offline_badge),
                style = MaterialTheme.typography.labelSmall,
                color = semantic.offlineGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectorSheet(
    selectedLanguage: StudyLanguage,
    checkVoiceAvailable: (String) -> Boolean,
    onSelectLanguage: (StudyLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            SupportedLanguages.all
        } else {
            SupportedLanguages.all.filter {
                it.englishName.contains(q, ignoreCase = true) ||
                    it.nativeName.contains(q, ignoreCase = true) ||
                    it.code.contains(q, ignoreCase = true) ||
                    it.regionLabel.contains(q, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Global Learning Language",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Updates study guide explanations & voice readout locale",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_language_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close language selector")
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search language or script…") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_search_input")
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredLanguages, key = { it.code }) { language ->
                    val isSelected = language.code == selectedLanguage.code
                    val voiceAvailable = remember(language.code) {
                        checkVoiceAvailable(language.code)
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectLanguage(language)
                                onDismiss()
                            }
                            .testTag("language_option_${language.code}"),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${language.nativeName} · ${language.englishName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = buildString {
                                        append(language.regionLabel)
                                        if (language.isRtl) append(" · RTL")
                                        append(if (voiceAvailable) " · Voice Ready" else " · Device TTS Pack Optional")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            AnimatedVisibility(visible = isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}
