package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DataSaverOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ObjectAnalysisResult
import com.example.ui.components.ConnectivityStatusPill
import com.example.ui.components.LanguageQuickButton
import com.example.ui.components.OfflineReadyBadge
import com.example.ui.components.SampleFirebaseNoticeBanner
import com.example.ui.components.StudyImageView
import com.example.ui.components.ThemeQuickToggleButton
import com.example.ui.components.VoiceSectionButton
import com.example.ui.theme.studySemanticColors
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.EduLensUiState
import com.example.util.SpeechRecognitionState
import com.example.util.VoiceInputMode

private val quickDiscoveryTopics = listOf(
    "Dicot Leaf",
    "Mechanical Watch",
    "Solar Panel",
    "Water Filter",
    "Electric Motor"
)

@Composable
fun ExploreScreen(
    uiState: EduLensUiState,
    onSearchQueryChange: (String) -> Unit,
    onCapturePhotoClick: () -> Unit,
    onUploadPhotoClick: () -> Unit,
    onVoiceSearchClick: (VoiceInputMode) -> Unit,
    onStopVoiceInput: () -> Unit,
    onCancelVoiceInput: () -> Unit,
    onClearPendingPhoto: () -> Unit,
    onAnalyzeClick: (String?) -> Unit,
    onOpenLanguagePicker: () -> Unit,
    onToggleLowBandwidth: () -> Unit,
    onDisableOfflineMode: () -> Unit,
    onOpenStudy: (ObjectAnalysisResult) -> Unit,
    onOpenOfflineLibrary: () -> Unit,
    onSpeakStudySummary: (ObjectAnalysisResult) -> Unit,
    onStopVoice: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tagline_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ConnectivityStatusPill(
                            isOfflineMode = uiState.preferences.offlineAccessibilityMode,
                            isNetworkConnected = uiState.networkStatus.isConnected,
                            isMetered = uiState.networkStatus.isMetered,
                            lowBandwidthMode = uiState.preferences.lowBandwidthMode,
                            onClick = onToggleLowBandwidth
                        )
                        ThemeQuickToggleButton(
                            isDarkTheme = isDarkTheme,
                            onToggle = onToggleTheme
                        )
                        LanguageQuickButton(
                            selectedLanguage = uiState.selectedLanguage,
                            onClick = onOpenLanguagePicker
                        )
                    }
                }
            }

            item {
                HeroDiscoveryBanner(
                    savedCount = uiState.savedStudies.size,
                    languageName = uiState.selectedLanguage.nativeName
                )
            }

            item {
                VisualSearchBarCard(
                    searchQuery = uiState.searchQuery,
                    pendingPhotoPath = uiState.pendingPhotoPath,
                    pendingPayloadKb = uiState.pendingPayloadKb,
                    pendingSavingsPercent = uiState.pendingSavingsPercent,
                    isCompressingPhoto = uiState.isCompressingPhoto,
                    isAnalyzing = uiState.analysisState is AnalysisUiState.Loading,
                    lowBandwidthMode = uiState.preferences.lowBandwidthMode,
                    speechState = uiState.speechState,
                    languageNativeName = uiState.selectedLanguage.nativeName,
                    isOfflineMode = uiState.isEffectivelyOffline,
                    onSearchQueryChange = onSearchQueryChange,
                    onCapturePhotoClick = onCapturePhotoClick,
                    onUploadPhotoClick = onUploadPhotoClick,
                    onVoiceSearchClick = onVoiceSearchClick,
                    onStopVoiceInput = onStopVoiceInput,
                    onCancelVoiceInput = onCancelVoiceInput,
                    onClearPendingPhoto = onClearPendingPhoto,
                    onAnalyzeClick = { onAnalyzeClick(null) }
                )
            }

            if (uiState.isEffectivelyOffline) {
                item {
                    OfflineActiveBanner(
                        offlineModeEnabled = uiState.preferences.offlineAccessibilityMode,
                        cachedMatches = uiState.offlineQuickMatches,
                        onOpenStudy = onOpenStudy,
                        onOpenLibrary = onOpenOfflineLibrary,
                        onDisableOfflineMode = onDisableOfflineMode
                    )
                }
            }

            item {
                SampleFirebaseNoticeBanner()
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Instant Visual Topics",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickDiscoveryTopics.forEach { topic ->
                            AssistChip(
                                onClick = { onAnalyzeClick(topic) },
                                label = { Text(topic) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("quick_topic_${topic.lowercase().replace(' ', '_')}")
                            )
                        }
                    }
                }
            }

            if (uiState.analysisState is AnalysisUiState.Error) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.analysisState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved for Offline Study (${uiState.savedStudies.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(
                        onClick = onOpenOfflineLibrary,
                        modifier = Modifier.testTag("view_all_offline_button")
                    ) {
                        Text("Open Library")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.savedStudies.take(5), key = { it.id }) { study ->
                        val isSpeakingThis = uiState.voiceState.isSpeaking &&
                            uiState.voiceState.activeSectionId == "card_${study.id}"
                        OfflinePreviewCard(
                            study = study,
                            isSpeakingThis = isSpeakingThis,
                            onClick = { onOpenStudy(study) },
                            onToggleVoice = {
                                if (isSpeakingThis) onStopVoice() else onSpeakStudySummary(study)
                            }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HeroDiscoveryBanner(
    savedCount: Int,
    languageName: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(156.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_banner),
                contentDescription = "Visual encyclopedia objects illustration",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xE60B3F3C),
                                Color(0xB80F5C58),
                                Color(0x400F5C58)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "$languageName · $savedCount Offline Guides Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Snap or Search Any Object",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Explore internal structure, science facts, practical solutions & voice readout anywhere.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun VisualSearchBarCard(
    searchQuery: String,
    pendingPhotoPath: String?,
    pendingPayloadKb: Int,
    pendingSavingsPercent: Int,
    isCompressingPhoto: Boolean,
    isAnalyzing: Boolean,
    lowBandwidthMode: Boolean,
    speechState: SpeechRecognitionState,
    languageNativeName: String,
    isOfflineMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onCapturePhotoClick: () -> Unit,
    onUploadPhotoClick: () -> Unit,
    onVoiceSearchClick: (VoiceInputMode) -> Unit,
    onStopVoiceInput: () -> Unit,
    onCancelVoiceInput: () -> Unit,
    onClearPendingPhoto: () -> Unit,
    onAnalyzeClick: () -> Unit
) {
    val activeMicMode = if (pendingPhotoPath != null) {
        VoiceInputMode.IMAGE_CONTEXT
    } else {
        VoiceInputMode.VOICE_SEARCH
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        if (pendingPhotoPath != null) {
                            "Speak or type a question about this photo…"
                        } else {
                            stringResource(R.string.search_bar_hint)
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchQueryChange("") },
                                modifier = Modifier.testTag("clear_search_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                        IconButton(
                            onClick = {
                                if (speechState.isListening) {
                                    onStopVoiceInput()
                                } else {
                                    onVoiceSearchClick(activeMicMode)
                                }
                            },
                            modifier = Modifier.testTag("voice_search_button")
                        ) {
                            Icon(
                                imageVector = if (speechState.isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = if (pendingPhotoPath != null) {
                                    "Speak photo context"
                                } else {
                                    stringResource(R.string.action_voice_search)
                                },
                                tint = if (speechState.isListening) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.secondary
                                }
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onAnalyzeClick() }),
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("visual_search_input")
            )

            AnimatedVisibility(visible = speechState.isListening) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_stt_listening_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size((34f + (speechState.rmsDb * 0.8f)).dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Listening",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (speechState.activeMode == VoiceInputMode.IMAGE_CONTEXT) {
                                        "Listening for Photo Context…"
                                    } else {
                                        "Listening for Voice Search…"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = buildString {
                                        append(languageNativeName)
                                        if (isOfflineMode) append(" · Offline STT Requested")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                            TextButton(
                                onClick = onCancelVoiceInput,
                                modifier = Modifier.testTag("cancel_voice_input_button")
                            ) {
                                Text("Cancel")
                            }
                            FilledTonalButton(
                                onClick = onStopVoiceInput,
                                modifier = Modifier.testTag("stop_voice_input_button")
                            ) {
                                Text("Done")
                            }
                        }
                        if (speechState.partialTranscript.isNotBlank()) {
                            Text(
                                text = "\"${speechState.partialTranscript}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            if (!speechState.errorMessage.isNullOrBlank()) {
                Text(
                    text = speechState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.testTag("stt_error_text")
                )
            }

            AnimatedVisibility(visible = isCompressingPhoto || pendingPhotoPath != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCompressingPhoto) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(
                                text = if (lowBandwidthMode) {
                                    "Compressing photo for low-bandwidth learning…"
                                } else {
                                    "Preparing uploaded photo…"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else if (pendingPhotoPath != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StudyImageView(
                                    imageSource = pendingPhotoPath,
                                    contentDescription = "Uploaded object preview",
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Photo Attached to Search",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = buildString {
                                            append("$pendingPayloadKb KB ready")
                                            if (pendingSavingsPercent > 0) {
                                                append(" · $pendingSavingsPercent% data saved")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = onClearPendingPhoto,
                                    modifier = Modifier.testTag("clear_photo_button")
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove attached photo")
                                }
                            }
                            AssistChip(
                                onClick = { onVoiceSearchClick(VoiceInputMode.IMAGE_CONTEXT) },
                                label = { Text("Add Spoken Context to Photo") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("add_voice_context_button")
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onCapturePhotoClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("camera_capture_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_capture_photo), maxLines = 1)
                }

                OutlinedButton(
                    onClick = onUploadPhotoClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("upload_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_upload_photo), maxLines = 1)
                }
            }

            if (lowBandwidthMode) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DataSaverOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.low_bandwidth_active),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Button(
                onClick = onAnalyzeClick,
                enabled = !isAnalyzing && !isCompressingPhoto,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("analyze_object_button")
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Building Educational Breakdown…")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.action_analyze_object))
                }
            }
        }
    }
}

@Composable
private fun OfflineActiveBanner(
    offlineModeEnabled: Boolean,
    cachedMatches: List<ObjectAnalysisResult>,
    onOpenStudy: (ObjectAnalysisResult) -> Unit,
    onOpenLibrary: () -> Unit,
    onDisableOfflineMode: () -> Unit
) {
    val semantic = studySemanticColors()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = semantic.offlineContainer
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = semantic.offlineGreen,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.offline_mode_banner_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = semantic.offlineGreen
                    )
                    Text(
                        text = stringResource(R.string.offline_mode_banner_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.offlineGreen.copy(alpha = 0.9f)
                    )
                }
                if (offlineModeEnabled) {
                    TextButton(onClick = onDisableOfflineMode) {
                        Text("Go Online", color = semantic.offlineGreen)
                    }
                }
            }
            if (cachedMatches.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cachedMatches.forEach { study ->
                        AssistChip(
                            onClick = { onOpenStudy(study) },
                            label = { Text(study.objectTitle) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.OfflinePin,
                                    contentDescription = null,
                                    tint = semantic.offlineGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        )
                    }
                }
            } else {
                TextButton(onClick = onOpenLibrary) {
                    Text("Browse All Cached Guides", color = semantic.offlineGreen)
                }
            }
        }
    }
}

@Composable
private fun OfflinePreviewCard(
    study: ObjectAnalysisResult,
    isSpeakingThis: Boolean,
    onClick: () -> Unit,
    onToggleVoice: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(236.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("recent_offline_card_${study.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp)
            ) {
                StudyImageView(
                    imageSource = study.imageSource,
                    contentDescription = study.objectTitle,
                    modifier = Modifier.fillMaxSize()
                )
                OfflineReadyBadge(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
                VoiceSectionButton(
                    isSpeakingThis = isSpeakingThis,
                    onSpeak = onToggleVoice,
                    onStop = onToggleVoice,
                    contentDescription = "Listen to ${study.objectTitle}",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                )
            }
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = study.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = study.objectTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${study.structures.size} structures · ${study.facts.size} facts · ${study.estimatedPayloadKb} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
