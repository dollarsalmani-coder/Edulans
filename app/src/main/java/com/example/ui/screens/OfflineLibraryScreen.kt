package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ObjectAnalysisResult
import com.example.model.SupportedLanguages
import com.example.ui.components.OfflineReadyBadge
import com.example.ui.components.SampleFirebaseNoticeBanner
import com.example.ui.components.StudyImageView
import com.example.ui.components.VoiceSectionButton
import com.example.ui.theme.studySemanticColors
import com.example.ui.viewmodel.EduLensUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val libraryCategories = listOf("All", "Biology", "Engineering", "Energy", "Everyday")

@Composable
fun OfflineLibraryScreen(
    uiState: EduLensUiState,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleOfflineMode: (Boolean) -> Unit,
    onOpenStudy: (ObjectAnalysisResult) -> Unit,
    onDeleteStudy: (String) -> Unit,
    onSpeakStudy: (ObjectAnalysisResult) -> Unit,
    onStopVoice: () -> Unit,
    onShareStudy: (ObjectAnalysisResult) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCachedKb = uiState.savedStudies.sumOf { it.estimatedPayloadKb }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Offline Study Library",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Saved structural guides, facts, solutions & voice playback available anytime without internet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                val semantic = studySemanticColors()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = if (uiState.preferences.offlineAccessibilityMode) {
                        semantic.offlineContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (uiState.preferences.offlineAccessibilityMode) {
                                semantic.offlineGreen
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(26.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Offline Accessibility Mode",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${uiState.savedStudies.size} cached guides · $totalCachedKb KB total storage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.preferences.offlineAccessibilityMode,
                            onCheckedChange = onToggleOfflineMode,
                            modifier = Modifier.testTag("offline_mode_switch")
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.librarySearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search saved objects, topics, or facts…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("offline_search_input")
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    libraryCategories.forEach { category ->
                        FilterChip(
                            selected = uiState.libraryCategoryFilter == category,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category) },
                            modifier = Modifier.testTag("library_category_$category")
                        )
                    }
                }
            }

            item {
                SampleFirebaseNoticeBanner()
            }

            if (uiState.filteredSavedStudies.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No matching offline guides",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Search or capture any object in Explore, and it will automatically save here for offline study.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.filteredSavedStudies, key = { it.id }) { study ->
                    val isSpeakingThis = uiState.voiceState.isSpeaking &&
                        uiState.voiceState.activeSectionId == "full_study_${study.id}"
                    val lang = SupportedLanguages.findByCode(study.languageCode)
                    val savedDateLabel = remember(study.timestamp) {
                        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(study.timestamp))
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenStudy(study) }
                            .testTag("saved_study_card_${study.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StudyImageView(
                                imageSource = study.imageSource,
                                contentDescription = study.objectTitle,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OfflineReadyBadge()
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = study.objectTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = study.scientificOrTechnicalName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${study.structures.size} structures · ${study.facts.size} facts · ${study.solutions.size} solutions",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Saved $savedDateLabel · ${study.estimatedPayloadKb} KB · Tap to open offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                VoiceSectionButton(
                                    isSpeakingThis = isSpeakingThis,
                                    onSpeak = { onSpeakStudy(study) },
                                    onStop = onStopVoice,
                                    contentDescription = "Listen to ${study.objectTitle}"
                                )
                                IconButton(
                                    onClick = { onShareStudy(study) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("share_study_${study.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share ${study.objectTitle}",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteStudy(study.id) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("delete_study_${study.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete ${study.objectTitle}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
