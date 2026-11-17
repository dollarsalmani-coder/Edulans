package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.EducationalFact
import com.example.model.ObjectAnalysisResult
import com.example.model.PracticalSolution
import com.example.model.QuickQuizQuestion
import com.example.model.StructurePart
import com.example.model.StudyLanguage
import com.example.ui.components.LanguageQuickButton
import com.example.ui.components.OfflineReadyBadge
import com.example.ui.components.SampleFirebaseNoticeBanner
import com.example.ui.components.StudyImageView
import com.example.ui.components.VoiceSectionButton
import com.example.ui.theme.studySemanticColors
import com.example.util.VoicePlaybackState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDetailScreen(
    study: ObjectAnalysisResult,
    selectedLanguage: StudyLanguage,
    voiceState: VoicePlaybackState,
    onBack: () -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onToggleSaveOffline: (ObjectAnalysisResult) -> Unit,
    onSpeakFullStudy: (ObjectAnalysisResult) -> Unit,
    onSpeakSection: (String, String, String) -> Unit,
    onStopVoice: () -> Unit,
    onShareStudy: (ObjectAnalysisResult) -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    BackHandler(onBack = onBack)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Structure", "Facts & Quiz", "Solutions")
    val isSpeakingFull = voiceState.isSpeaking && voiceState.activeSectionId == "full_study_${study.id}"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = study.objectTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    LanguageQuickButton(
                        selectedLanguage = selectedLanguage,
                        onClick = onOpenLanguagePicker,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = { onShareStudy(study) },
                        modifier = Modifier.testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share study guide and solution images"
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { onToggleSaveOffline(study) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("detail_save_offline_button")
                    ) {
                        Icon(
                            imageVector = if (study.isSavedOffline) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (study.isSavedOffline) "Saved offline" else "Save for offline"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (study.isSamplePlaceholder) {
                    item {
                        SampleFirebaseNoticeBanner()
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                StudyImageView(
                                    imageSource = study.imageSource,
                                    contentDescription = study.objectTitle,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = study.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    if (study.isSavedOffline) {
                                        OfflineReadyBadge()
                                    }
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "${study.estimatedPayloadKb} KB · ${selectedLanguage.nativeName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = study.objectTitle,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = study.scientificOrTechnicalName,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = study.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (isSpeakingFull) onStopVoice() else onSpeakFullStudy(study)
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSpeakingFull) {
                                                MaterialTheme.colorScheme.secondary
                                            } else {
                                                MaterialTheme.colorScheme.primary
                                            }
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("detail_listen_all_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeakingFull) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isSpeakingFull) {
                                                "Stop Voice"
                                            } else {
                                                "Listen (${selectedLanguage.nativeName})"
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { onShareStudy(study) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .testTag("detail_export_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share", maxLines = 1)
                                    }

                                    OutlinedIconButton(
                                        onClick = onToggleTheme,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("detail_theme_toggle_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Night Study Dark Mode",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag("detail_tab_${index}")
                            )
                        }
                    }
                }

                when (selectedTabIndex) {
                    0 -> {
                        items(study.structures, key = { "${study.id}_part_${it.layerOrder}_${it.name}" }) { part ->
                            val sectionId = "part_${study.id}_${part.layerOrder}"
                            val isSpeakingThis = voiceState.isSpeaking && voiceState.activeSectionId == sectionId
                            StructurePartCard(
                                part = part,
                                isSpeakingThis = isSpeakingThis,
                                onSpeak = {
                                    onSpeakSection(
                                        "Layer ${part.layerOrder}: ${part.name}. ${part.materialOrRole}. ${part.functionDescription}",
                                        sectionId,
                                        study.languageCode
                                    )
                                },
                                onStop = onStopVoice
                            )
                        }
                    }

                    1 -> {
                        items(study.facts, key = { "${study.id}_fact_${it.title}" }) { fact ->
                            val sectionId = "fact_${study.id}_${fact.title.hashCode()}"
                            val isSpeakingThis = voiceState.isSpeaking && voiceState.activeSectionId == sectionId
                            EducationalFactCard(
                                fact = fact,
                                isSpeakingThis = isSpeakingThis,
                                onSpeak = {
                                    onSpeakSection(
                                        "${fact.discipline}. ${fact.title}. ${fact.detail}",
                                        sectionId,
                                        study.languageCode
                                    )
                                },
                                onStop = onStopVoice
                            )
                        }

                        study.quiz?.let { quiz ->
                            item(key = "${study.id}_quiz") {
                                QuickQuizCard(quiz = quiz)
                            }
                        }
                    }

                    2 -> {
                        items(study.solutions, key = { "${study.id}_sol_${it.problemTitle}" }) { solution ->
                            val sectionId = "sol_${study.id}_${solution.problemTitle.hashCode()}"
                            val isSpeakingThis = voiceState.isSpeaking && voiceState.activeSectionId == sectionId
                            PracticalSolutionCard(
                                solution = solution,
                                isSpeakingThis = isSpeakingThis,
                                onSpeak = {
                                    onSpeakSection(
                                        "${solution.problemTitle}. ${solution.solutionSteps}. Tip: ${solution.careOrSustainabilityTip}",
                                        sectionId,
                                        study.languageCode
                                    )
                                },
                                onStop = onStopVoice
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun StructurePartCard(
    part: StructurePart,
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${part.layerOrder}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = part.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = part.materialOrRole,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = part.functionDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VoiceSectionButton(
                isSpeakingThis = isSpeakingThis,
                onSpeak = onSpeak,
                onStop = onStop,
                contentDescription = "Listen to ${part.name}"
            )
        }
    }
}

@Composable
private fun EducationalFactCard(
    fact: EducationalFact,
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = fact.discipline,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = fact.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = fact.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            VoiceSectionButton(
                isSpeakingThis = isSpeakingThis,
                onSpeak = onSpeak,
                onStop = onStop,
                contentDescription = "Listen to ${fact.title}"
            )
        }
    }
}

@Composable
private fun PracticalSolutionCard(
    solution: PracticalSolution,
    isSpeakingThis: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = solution.problemTitle,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                VoiceSectionButton(
                    isSpeakingThis = isSpeakingThis,
                    onSpeak = onSpeak,
                    onStop = onStop,
                    contentDescription = "Listen to solution for ${solution.problemTitle}"
                )
            }
            Text(
                text = solution.solutionSteps,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            val semantic = studySemanticColors()
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = semantic.offlineContainer
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = semantic.offlineGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = solution.careOrSustainabilityTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.offlineGreen
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickQuizCard(quiz: QuickQuizQuestion) {
    var selectedOption by remember(quiz.question) { mutableStateOf<Int?>(null) }
    val semantic = studySemanticColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Quiz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Quick Knowledge Check",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = quiz.question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            quiz.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == index
                val isCorrect = index == quiz.correctIndex
                val containerColor = when {
                    selectedOption == null -> MaterialTheme.colorScheme.surface
                    isCorrect -> semantic.offlineContainer
                    isSelected -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedOption = index }
                        .testTag("quiz_option_$index"),
                    color = containerColor,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedOption != null && isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct answer",
                                tint = semantic.offlineGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            if (selectedOption != null) {
                Text(
                    text = quiz.explanation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
