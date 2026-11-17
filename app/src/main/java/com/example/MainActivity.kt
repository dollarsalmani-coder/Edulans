package com.example

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OfflinePin
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.ObjectAnalysisResult
import com.example.model.ThemeMode
import com.example.ui.components.LanguageSelectorSheet
import com.example.ui.components.ShareStudySheet
import com.example.ui.screens.AccessibilityScreen
import com.example.ui.screens.CameraPreviewScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.OfflineLibraryScreen
import com.example.ui.screens.StudyDetailScreen
import com.example.ui.theme.EduLensTheme
import com.example.ui.viewmodel.EduLensViewModel
import com.example.util.ImageCompressor
import com.example.util.StudyShareHelper
import com.example.util.VoiceInputMode
import kotlinx.coroutines.launch
import java.io.File

private const val ROUTE_EXPLORE = "explore"
private const val ROUTE_OFFLINE_LIBRARY = "offline_library"
private const val ROUTE_ACCESSIBILITY = "accessibility"
private const val ROUTE_STUDY_DETAIL = "study_detail"
private const val ROUTE_STUDY_DETAIL_PATTERN = "study_detail/{studyId}"
private const val ROUTE_CAMERA_PREVIEW = "camera_preview"

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = ROUTE_EXPLORE,
        labelRes = R.string.nav_explore,
        selectedIcon = Icons.Filled.Explore,
        unselectedIcon = Icons.Outlined.Explore,
        testTag = "nav_explore"
    ),
    TopLevelDestination(
        route = ROUTE_OFFLINE_LIBRARY,
        labelRes = R.string.nav_offline_library,
        selectedIcon = Icons.Filled.OfflinePin,
        unselectedIcon = Icons.Outlined.OfflinePin,
        testTag = "nav_offline_library"
    ),
    TopLevelDestination(
        route = ROUTE_ACCESSIBILITY,
        labelRes = R.string.nav_accessibility,
        selectedIcon = Icons.Filled.Language,
        unselectedIcon = Icons.Outlined.Language,
        testTag = "nav_accessibility"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: EduLensViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.preferences.themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDark
            }

            DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
                onDispose {}
            }

            EduLensTheme(darkTheme = isDarkTheme) {
                EduLensApp(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun EduLensApp(
    viewModel: EduLensViewModel = viewModel(),
    isDarkTheme: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showLanguageSheet by remember { mutableStateOf(false) }
    var shareTargetStudy by remember { mutableStateOf<ObjectAnalysisResult?>(null) }
    var isPreparingExport by remember { mutableStateOf(false) }
    var pendingSystemCameraFile by remember { mutableStateOf<File?>(null) }
    var pendingVoiceMode by remember { mutableStateOf(VoiceInputMode.VOICE_SEARCH) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onPhotoUriSelected(uri)
            if (navController.currentDestination?.route == ROUTE_CAMERA_PREVIEW) {
                navController.popBackStack()
            }
        }
    }

    val systemCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingSystemCameraFile
        if (success && file != null) {
            viewModel.onCapturedPhotoFile(file)
            if (navController.currentDestination?.route == ROUTE_CAMERA_PREVIEW) {
                navController.popBackStack()
            }
        }
        pendingSystemCameraFile = null
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenList?.firstOrNull()?.trim().orEmpty()
            if (spokenText.isNotBlank()) {
                viewModel.applySpokenTranscript(
                    transcript = spokenText,
                    mode = pendingVoiceMode,
                    onStudyReady = { study ->
                        navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }

    val launchSpeechIntentFallback: (VoiceInputMode) -> Unit = { mode ->
        pendingVoiceMode = mode
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, uiState.selectedLanguage.code)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (mode == VoiceInputMode.IMAGE_CONTEXT) {
                    "Speak context or a question about your photo…"
                } else {
                    "Speak an object name to study…"
                }
            )
        }
        runCatching {
            speechLauncher.launch(intent)
        }.onFailure {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Speech recognition service not available on this device.")
            }
        }
    }

    val startLiveSpeech: (VoiceInputMode) -> Unit = { mode ->
        viewModel.startVoiceInput(
            mode = mode,
            onStudyReady = { study ->
                navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                    launchSingleTop = true
                }
            }
        )
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startLiveSpeech(pendingVoiceMode)
        } else {
            launchSpeechIntentFallback(pendingVoiceMode)
        }
    }

    LaunchedEffect(uiState.statusBannerMessage) {
        val message = uiState.statusBannerMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.dismissBannerMessage()
        }
    }

    val launchCamera: () -> Unit = {
        navController.navigate(ROUTE_CAMERA_PREVIEW) {
            launchSingleTop = true
        }
    }

    val launchSystemCameraFallback: () -> Unit = {
        runCatching {
            val (uri, file) = ImageCompressor.createCameraCaptureUri(context)
            pendingSystemCameraFile = file
            systemCameraLauncher.launch(uri)
        }.onFailure {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("System camera unavailable. Try uploading a photo.")
            }
        }
    }

    val launchUploadPhoto: () -> Unit = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val launchVoiceSearch: (VoiceInputMode) -> Unit = { mode ->
        pendingVoiceMode = mode
        viewModel.clearSpeechError()
        val hasAudioPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (viewModel.isSpeechRecognizerAvailable()) {
            if (hasAudioPerm) {
                startLiveSpeech(mode)
            } else {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            launchSpeechIntentFallback(mode)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showTopLevelChrome = currentDestination?.route?.startsWith(ROUTE_STUDY_DETAIL) != true &&
        currentDestination?.route != ROUTE_CAMERA_PREVIEW

    val navigateTopLevel: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (showTopLevelChrome && !useNavigationRail) {
                    NavigationBar {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelChrome && useNavigationRail) {
                    NavigationRail {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes)
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = ROUTE_EXPLORE,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(ROUTE_EXPLORE) {
                        ExploreScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onCapturePhotoClick = launchCamera,
                            onUploadPhotoClick = launchUploadPhoto,
                            onVoiceSearchClick = launchVoiceSearch,
                            onStopVoiceInput = viewModel::stopVoiceInput,
                            onCancelVoiceInput = viewModel::cancelVoiceInput,
                            onClearPendingPhoto = viewModel::clearPendingPhoto,
                            onAnalyzeClick = { overrideQuery ->
                                viewModel.analyzeCurrentInput(overrideQuery) { study ->
                                    navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                                        launchSingleTop = true
                                    }
                                }
                            },
                            onOpenLanguagePicker = { showLanguageSheet = true },
                            onToggleLowBandwidth = {
                                viewModel.setLowBandwidthMode(!uiState.preferences.lowBandwidthMode)
                            },
                            onDisableOfflineMode = { viewModel.setOfflineAccessibilityMode(false) },
                            onOpenStudy = { study ->
                                viewModel.openStudy(study)
                                navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                                    launchSingleTop = true
                                }
                            },
                            onOpenOfflineLibrary = { navigateTopLevel(ROUTE_OFFLINE_LIBRARY) },
                            onSpeakStudySummary = { study ->
                                viewModel.speakSection(
                                    text = "${study.objectTitle}. ${study.summary}",
                                    sectionId = "card_${study.id}",
                                    languageCode = study.languageCode
                                )
                            },
                            onStopVoice = viewModel::stopVoice,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = { viewModel.toggleDayNightTheme(isDarkTheme) }
                        )
                    }

                    composable(ROUTE_OFFLINE_LIBRARY) {
                        OfflineLibraryScreen(
                            uiState = uiState,
                            onSearchQueryChange = viewModel::onLibrarySearchQueryChange,
                            onSelectCategory = viewModel::onLibraryCategorySelect,
                            onToggleOfflineMode = viewModel::setOfflineAccessibilityMode,
                            onOpenStudy = { study ->
                                viewModel.openStudy(study)
                                navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                                    launchSingleTop = true
                                }
                            },
                            onDeleteStudy = viewModel::deleteSavedStudy,
                            onSpeakStudy = viewModel::speakFullStudy,
                            onStopVoice = viewModel::stopVoice,
                            onShareStudy = { shareTargetStudy = it }
                        )
                    }

                    composable(ROUTE_ACCESSIBILITY) {
                        AccessibilityScreen(
                            uiState = uiState,
                            onOpenLanguagePicker = { showLanguageSheet = true },
                            onSelectLanguage = viewModel::selectLanguage,
                            onSetSpeechRate = viewModel::setVoiceSpeechRate,
                            onTestVoice = viewModel::testVoiceSample,
                            onStopVoice = viewModel::stopVoice,
                            onSetLowBandwidth = viewModel::setLowBandwidthMode,
                            onSetOfflineMode = viewModel::setOfflineAccessibilityMode,
                            onSetAutoSave = viewModel::setAutoSaveOffline,
                            isDarkTheme = isDarkTheme,
                            onSelectThemeMode = viewModel::setThemeMode
                        )
                    }

                    composable(
                        route = ROUTE_STUDY_DETAIL_PATTERN,
                        arguments = listOf(navArgument("studyId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val studyId = backStackEntry.arguments?.getString("studyId").orEmpty()
                        LaunchedEffect(studyId) {
                            if (studyId.isNotBlank() && uiState.activeDetailStudy?.id != studyId) {
                                viewModel.openStudyById(studyId)
                            }
                        }
                        val study = uiState.activeDetailStudy
                        if (study != null) {
                            StudyDetailScreen(
                                study = study,
                                selectedLanguage = uiState.selectedLanguage,
                                voiceState = uiState.voiceState,
                                onBack = { navController.popBackStack() },
                                onOpenLanguagePicker = { showLanguageSheet = true },
                                onToggleSaveOffline = viewModel::toggleSaveOffline,
                                onSpeakFullStudy = viewModel::speakFullStudy,
                                onSpeakSection = viewModel::speakSection,
                                onStopVoice = viewModel::stopVoice,
                                onShareStudy = { shareTargetStudy = it },
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleDayNightTheme(isDarkTheme) }
                            )
                        }
                    }

                    composable(ROUTE_CAMERA_PREVIEW) {
                        CameraPreviewScreen(
                            lowBandwidthMode = uiState.preferences.lowBandwidthMode,
                            languageNativeName = uiState.selectedLanguage.nativeName,
                            onBack = { navController.popBackStack() },
                            onUploadFromGallery = launchUploadPhoto,
                            onUseSystemCamera = launchSystemCameraFallback,
                            onPhotoCaptured = { bytes, rotationDegrees, autoAnalyze ->
                                viewModel.onCapturedPhotoBytes(
                                    bytes = bytes,
                                    rotationDegrees = rotationDegrees,
                                    autoAnalyze = autoAnalyze,
                                    onStudyReady = { study ->
                                        navController.navigate("$ROUTE_STUDY_DETAIL/${study.id}") {
                                            popUpTo(ROUTE_EXPLORE)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                                if (!autoAnalyze) {
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showLanguageSheet) {
        LanguageSelectorSheet(
            selectedLanguage = uiState.selectedLanguage,
            checkVoiceAvailable = viewModel::isVoiceLanguageAvailable,
            onSelectLanguage = viewModel::selectLanguage,
            onDismiss = { showLanguageSheet = false }
        )
    }

    shareTargetStudy?.let { targetStudy ->
        ShareStudySheet(
            study = targetStudy,
            language = uiState.selectedLanguage,
            isPreparingExport = isPreparingExport,
            onExportOption = { option ->
                coroutineScope.launch {
                    isPreparingExport = true
                    try {
                        val imageUris = StudyShareHelper.prepareShareImageUris(
                            context = context,
                            study = targetStudy,
                            language = uiState.selectedLanguage,
                            option = option
                        )
                        val chooserIntent = StudyShareHelper.buildShareChooserIntent(
                            context = context,
                            study = targetStudy,
                            language = uiState.selectedLanguage,
                            imageUris = imageUris,
                            option = option,
                            chooserTitle = context.getString(R.string.share_chooser_title)
                        )
                        shareTargetStudy = null
                        runCatching {
                            context.startActivity(chooserIntent)
                        }.onFailure {
                            snackbarHostState.showSnackbar("No compatible messaging or notes app found.")
                        }
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar(
                            e.localizedMessage ?: "Failed to prepare study export."
                        )
                    } finally {
                        isPreparingExport = false
                    }
                }
            },
            onCopyText = {
                val formatted = StudyShareHelper.formatEducationalText(
                    study = targetStudy,
                    language = uiState.selectedLanguage
                )
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard?.setPrimaryClip(
                    ClipData.newPlainText(targetStudy.objectTitle, formatted)
                )
                shareTargetStudy = null
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.share_copied_snackbar))
                }
            },
            onDismiss = {
                if (!isPreparingExport) {
                    shareTargetStudy = null
                }
            }
        )
    }
}
