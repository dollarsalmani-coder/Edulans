package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ObjectAnalysisRepository
import com.example.data.ai.SampleObjectAnalysisRepository
import com.example.data.local.EduLensDatabase
import com.example.data.local.StudyHistoryRepository
import com.example.data.prefs.AccessibilityPreferences
import com.example.data.prefs.AppPreferencesRepository
import com.example.model.ObjectAnalysisResult
import com.example.model.StudyLanguage
import com.example.model.SupportedLanguages
import com.example.model.ThemeMode
import com.example.util.ImageCompressor
import com.example.util.NetworkMonitor
import com.example.util.NetworkStatus
import com.example.util.SpeechRecognitionState
import com.example.util.SpeechToTextController
import com.example.util.VoiceInputMode
import com.example.util.VoicePlaybackState
import com.example.util.VoiceReadoutController
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data object Loading : AnalysisUiState
    data class Success(val result: ObjectAnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

data class EduLensUiState(
    val searchQuery: String = "",
    val pendingPhotoPath: String? = null,
    val pendingPayloadKb: Int = 0,
    val pendingSavingsPercent: Int = 0,
    val isCompressingPhoto: Boolean = false,
    val analysisState: AnalysisUiState = AnalysisUiState.Idle,
    val activeDetailStudy: ObjectAnalysisResult? = null,
    val savedStudies: List<ObjectAnalysisResult> = emptyList(),
    val librarySearchQuery: String = "",
    val libraryCategoryFilter: String = "All",
    val preferences: AccessibilityPreferences = AccessibilityPreferences(),
    val selectedLanguage: StudyLanguage = SupportedLanguages.all.first(),
    val networkStatus: NetworkStatus = NetworkStatus(),
    val voiceState: VoicePlaybackState = VoicePlaybackState(),
    val speechState: SpeechRecognitionState = SpeechRecognitionState(),
    val statusBannerMessage: String? = null
) {
    val isEffectivelyOffline: Boolean
        get() = preferences.offlineAccessibilityMode || !networkStatus.isConnected

    val filteredSavedStudies: List<ObjectAnalysisResult>
        get() = savedStudies.filter { item ->
            val matchesCategory = libraryCategoryFilter == "All" ||
                item.category.equals(libraryCategoryFilter, ignoreCase = true)
            val q = librarySearchQuery.trim()
            val matchesQuery = q.isBlank() ||
                item.objectTitle.contains(q, ignoreCase = true) ||
                item.scientificOrTechnicalName.contains(q, ignoreCase = true) ||
                item.summary.contains(q, ignoreCase = true) ||
                item.category.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }

    val offlineQuickMatches: List<ObjectAnalysisResult>
        get() {
            val q = searchQuery.trim()
            if (q.isBlank()) return savedStudies.take(3)
            return savedStudies.filter {
                it.objectTitle.contains(q, ignoreCase = true) ||
                    it.category.contains(q, ignoreCase = true) ||
                    it.summary.contains(q, ignoreCase = true)
            }
        }
}

class EduLensViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val sampleRepository = SampleObjectAnalysisRepository()
    private val analysisRepository: ObjectAnalysisRepository = sampleRepository

    private val database = EduLensDatabase.getInstance(application)
    private val historyRepository = StudyHistoryRepository(database.savedStudyDao())
    private val preferencesRepository = AppPreferencesRepository(application)
    private val networkMonitor = NetworkMonitor(application)
    private val voiceController = VoiceReadoutController(application)
    private val speechController = SpeechToTextController(application)

    private val _searchQuery = MutableStateFlow("")
    private val _pendingPhotoPath = MutableStateFlow<String?>(null)
    private val _pendingPayloadKb = MutableStateFlow(0)
    private val _pendingSavingsPercent = MutableStateFlow(0)
    private val _isCompressingPhoto = MutableStateFlow(false)
    private val _analysisState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    private val _activeDetailStudy = MutableStateFlow<ObjectAnalysisResult?>(null)
    private val _librarySearchQuery = MutableStateFlow("")
    private val _libraryCategoryFilter = MutableStateFlow("All")
    private val _statusBannerMessage = MutableStateFlow<String?>(null)

    private data class DraftInputState(
        val searchQuery: String,
        val pendingPhotoPath: String?,
        val pendingPayloadKb: Int,
        val pendingSavingsPercent: Int,
        val isCompressingPhoto: Boolean
    )

    private data class LibraryFilterState(
        val librarySearchQuery: String,
        val libraryCategoryFilter: String,
        val statusBannerMessage: String?
    )

    private data class EnvironmentState(
        val preferences: AccessibilityPreferences,
        val networkStatus: NetworkStatus,
        val voiceState: VoicePlaybackState,
        val speechState: SpeechRecognitionState
    )

    private val draftFlow = combine(
        _searchQuery,
        _pendingPhotoPath,
        _pendingPayloadKb,
        _pendingSavingsPercent,
        _isCompressingPhoto
    ) { query, photo, kb, savings, compressing ->
        DraftInputState(query, photo, kb, savings, compressing)
    }

    private val libraryFilterFlow = combine(
        _librarySearchQuery,
        _libraryCategoryFilter,
        _statusBannerMessage
    ) { query, category, banner ->
        LibraryFilterState(query, category, banner)
    }

    private val environmentFlow = combine(
        preferencesRepository.preferences,
        networkMonitor.status,
        voiceController.state,
        speechController.state
    ) { prefs, net, voice, speech ->
        EnvironmentState(prefs, net, voice, speech)
    }

    val uiState: StateFlow<EduLensUiState> = combine(
        draftFlow,
        _analysisState,
        _activeDetailStudy,
        historyRepository.savedStudies,
        libraryFilterFlow,
        environmentFlow
    ) { values ->
        val draft = values[0] as DraftInputState
        val analysis = values[1] as AnalysisUiState
        val detail = values[2] as ObjectAnalysisResult?
        @Suppress("UNCHECKED_CAST")
        val saved = values[3] as List<ObjectAnalysisResult>
        val libFilter = values[4] as LibraryFilterState
        val env = values[5] as EnvironmentState

        val prefs = env.preferences
        val selectedLang = SupportedLanguages.findByCode(prefs.selectedLanguageCode)
        voiceController.updateLanguage(selectedLang.code)
        voiceController.updateSpeechRate(prefs.voiceSpeechRate)

        val detailWithSavedFlag = detail?.let { current ->
            val isSaved = saved.any { it.id == current.id }
            current.copy(isSavedOffline = isSaved)
        }

        EduLensUiState(
            searchQuery = draft.searchQuery,
            pendingPhotoPath = draft.pendingPhotoPath,
            pendingPayloadKb = draft.pendingPayloadKb,
            pendingSavingsPercent = draft.pendingSavingsPercent,
            isCompressingPhoto = draft.isCompressingPhoto,
            analysisState = analysis,
            activeDetailStudy = detailWithSavedFlag,
            savedStudies = saved,
            librarySearchQuery = libFilter.librarySearchQuery,
            libraryCategoryFilter = libFilter.libraryCategoryFilter,
            preferences = prefs,
            selectedLanguage = selectedLang,
            networkStatus = env.networkStatus,
            voiceState = env.voiceState,
            speechState = env.speechState,
            statusBannerMessage = libFilter.statusBannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EduLensUiState()
    )

    init {
        viewModelScope.launch {
            historyRepository.ensureStarterPackSeeded(sampleRepository)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onPhotoUriSelected(uri: Uri) {
        viewModelScope.launch {
            _isCompressingPhoto.value = true
            try {
                val lowBandwidth = uiState.value.preferences.lowBandwidthMode
                val result = ImageCompressor.compressAndStoreImage(
                    context = getApplication(),
                    sourceUri = uri,
                    lowBandwidthMode = lowBandwidth
                )
                if (result != null) {
                    _pendingPhotoPath.value = result.localFilePath
                    _pendingPayloadKb.value = result.compressedKb
                    _pendingSavingsPercent.value = result.savingsPercent
                } else {
                    _statusBannerMessage.value = "Could not read selected image."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusBannerMessage.value = e.localizedMessage ?: "Failed to process image."
            } finally {
                _isCompressingPhoto.value = false
            }
        }
    }

    fun onCapturedPhotoFile(file: File) {
        if (!file.exists() || file.length() == 0L) return
        onPhotoUriSelected(Uri.fromFile(file))
    }

    fun onCapturedPhotoBytes(
        bytes: ByteArray,
        rotationDegrees: Int,
        autoAnalyze: Boolean,
        onStudyReady: (ObjectAnalysisResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isCompressingPhoto.value = true
            try {
                val lowBandwidth = uiState.value.preferences.lowBandwidthMode
                val result = ImageCompressor.compressAndStoreBytes(
                    context = getApplication(),
                    originalBytes = bytes,
                    rotationDegrees = rotationDegrees,
                    lowBandwidthMode = lowBandwidth
                )
                if (result != null) {
                    _pendingPhotoPath.value = result.localFilePath
                    _pendingPayloadKb.value = result.compressedKb
                    _pendingSavingsPercent.value = result.savingsPercent
                    if (autoAnalyze) {
                        analyzeCurrentInput(onStudyReady = onStudyReady)
                    } else {
                        _statusBannerMessage.value =
                            "Photo attached (${result.compressedKb} KB). Tap Analyze Object or add a note."
                    }
                } else {
                    _statusBannerMessage.value = "Could not process captured photo."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _statusBannerMessage.value = e.localizedMessage ?: "Failed to process captured photo."
            } finally {
                _isCompressingPhoto.value = false
            }
        }
    }

    fun clearPendingPhoto() {
        _pendingPhotoPath.value = null
        _pendingPayloadKb.value = 0
        _pendingSavingsPercent.value = 0
    }

    fun analyzeCurrentInput(
        overrideQuery: String? = null,
        onStudyReady: (ObjectAnalysisResult) -> Unit = {}
    ) {
        val snapshot = uiState.value
        val query = (overrideQuery ?: snapshot.searchQuery).trim()
        val photoPath = snapshot.pendingPhotoPath

        if (query.isBlank() && photoPath == null) {
            _statusBannerMessage.value = "Enter an object name or upload/capture a photo first."
            return
        }

        if (snapshot.isEffectivelyOffline) {
            val cachedMatch = snapshot.savedStudies.firstOrNull { saved ->
                query.isNotBlank() && (
                    saved.objectTitle.contains(query, ignoreCase = true) ||
                        saved.category.contains(query, ignoreCase = true) ||
                        saved.summary.contains(query, ignoreCase = true)
                    )
            }
            if (cachedMatch != null) {
                _activeDetailStudy.value = cachedMatch
                _analysisState.value = AnalysisUiState.Success(cachedMatch)
                _statusBannerMessage.value = "Opened from Offline Library with zero data used."
                onStudyReady(cachedMatch)
                return
            }
        }

        viewModelScope.launch {
            _analysisState.value = AnalysisUiState.Loading
            try {
                val result = analysisRepository.analyzeObject(
                    queryText = query,
                    compressedImagePath = photoPath,
                    payloadKb = snapshot.pendingPayloadKb,
                    targetLanguage = snapshot.selectedLanguage,
                    lowBandwidthMode = snapshot.preferences.lowBandwidthMode
                )

                val savedResult = if (snapshot.preferences.autoSaveOffline) {
                    historyRepository.saveForOffline(result)
                    result.copy(isSavedOffline = true)
                } else {
                    result
                }

                _activeDetailStudy.value = savedResult
                _analysisState.value = AnalysisUiState.Success(savedResult)
                if (overrideQuery != null) {
                    _searchQuery.value = overrideQuery
                }
                clearPendingPhoto()
                onStudyReady(savedResult)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _analysisState.value = AnalysisUiState.Error(
                    e.localizedMessage ?: "Unable to analyze object."
                )
            }
        }
    }

    fun openStudy(study: ObjectAnalysisResult) {
        _activeDetailStudy.value = study
        _analysisState.value = AnalysisUiState.Success(study)
    }

    fun openStudyById(id: String) {
        viewModelScope.launch {
            val found = historyRepository.getStudyById(id)
                ?: uiState.value.savedStudies.firstOrNull { it.id == id }
            if (found != null) {
                _activeDetailStudy.value = found
            }
        }
    }

    fun toggleSaveOffline(study: ObjectAnalysisResult) {
        viewModelScope.launch {
            val currentlySaved = uiState.value.savedStudies.any { it.id == study.id }
            if (currentlySaved) {
                historyRepository.removeFromOffline(study.id)
                _activeDetailStudy.value = study.copy(isSavedOffline = false)
                _statusBannerMessage.value = "Removed from Offline Library."
            } else {
                historyRepository.saveForOffline(study)
                _activeDetailStudy.value = study.copy(isSavedOffline = true)
                _statusBannerMessage.value = "Saved for offline learning."
            }
        }
    }

    fun deleteSavedStudy(id: String) {
        viewModelScope.launch {
            historyRepository.removeFromOffline(id)
            if (_activeDetailStudy.value?.id == id) {
                _activeDetailStudy.value = _activeDetailStudy.value?.copy(isSavedOffline = false)
            }
            _statusBannerMessage.value = "Deleted from Offline Library."
        }
    }

    fun onLibrarySearchQueryChange(query: String) {
        _librarySearchQuery.value = query
    }

    fun onLibraryCategorySelect(category: String) {
        _libraryCategoryFilter.value = category
    }

    fun selectLanguage(language: StudyLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguageCode(language.code)
            voiceController.stop()
            voiceController.updateLanguage(language.code)

            val active = _activeDetailStudy.value
            if (active != null && active.languageCode != language.code) {
                val translated = analysisRepository.translateAnalysis(active, language)
                _activeDetailStudy.value = translated
                if (active.isSavedOffline) {
                    historyRepository.saveForOffline(translated)
                }
            }
            _statusBannerMessage.value = "Language set to ${language.nativeName} (${language.englishName})."
        }
    }

    fun setLowBandwidthMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLowBandwidthMode(enabled)
            _statusBannerMessage.value = if (enabled) {
                "Low-Bandwidth Mode enabled: photos compress to lightweight sizes."
            } else {
                "Standard image quality restored."
            }
        }
    }

    fun setOfflineAccessibilityMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setOfflineAccessibilityMode(enabled)
            _statusBannerMessage.value = if (enabled) {
                "Offline Accessibility Mode active: studying cached guides with zero data."
            } else {
                "Online & sample search mode active."
            }
        }
    }

    fun setAutoSaveOffline(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoSaveOffline(enabled)
        }
    }

    fun setVoiceSpeechRate(rate: Float) {
        viewModelScope.launch {
            preferencesRepository.setVoiceSpeechRate(rate)
            voiceController.updateSpeechRate(rate)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
            _statusBannerMessage.value = when (mode) {
                ThemeMode.DARK -> "Night Study Dark Mode enabled for low-glare reading."
                ThemeMode.LIGHT -> "Daylight Light Mode enabled."
                ThemeMode.SYSTEM -> "Theme set to follow device system setting."
            }
        }
    }

    fun toggleDayNightTheme(currentlyDark: Boolean) {
        setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    fun speakFullStudy(study: ObjectAnalysisResult) {
        val script = buildString {
            append(study.objectTitle).append(". ")
            append(study.scientificOrTechnicalName).append(". ")
            append(study.summary).append(". ")
            append("Structural components: ")
            study.structures.forEach { part ->
                append(part.name).append(". ").append(part.functionDescription).append(" ")
            }
            append("Educational facts: ")
            study.facts.forEach { fact ->
                append(fact.title).append(". ").append(fact.detail).append(" ")
            }
            append("Practical solutions: ")
            study.solutions.forEach { sol ->
                append(sol.problemTitle).append(". ").append(sol.solutionSteps).append(" ")
            }
        }
        voiceController.speak(script, sectionId = "full_study_${study.id}", languageCode = study.languageCode)
    }

    fun speakSection(text: String, sectionId: String, languageCode: String) {
        voiceController.speak(text, sectionId = sectionId, languageCode = languageCode)
    }

    fun stopVoice() {
        voiceController.stop()
    }

    fun testVoiceSample() {
        val lang = uiState.value.selectedLanguage
        val sampleLine = when (lang.code) {
            "es" -> "Hola. Esta es la guía de voz educativa de EduLens en español."
            "fr" -> "Bonjour. Ceci est la lecture vocale éducative EduLens en français."
            "ar" -> "مرحبا. هذا هو الدليل الصوتي التعليمي من EduLens باللغة العربية."
            "hi" -> "नमस्ते। यह हिंदी में EduLens का शैक्षिक वॉयस गाइड है।"
            "zh" -> "你好。这是 EduLens 的中文语音学习助手。"
            "bn" -> "নমস্কার। এটি বাংলায় EduLens-এর শিক্ষামূলক ভয়েস গাইড।"
            "pt" -> "Olá. Este é o guia de voz educacional EduLens em português."
            "sw" -> "Habari. Huu ni mwongozo wa sauti wa EduLens kwa Kiswahili."
            "ja" -> "こんにちは。これは日本語のEduLens音声学習ガイドです。"
            "de" -> "Hallo. Dies ist der EduLens-Sprachleitfaden auf Deutsch."
            "ko" -> "안녕하세요. 한국어 EduLens 음성 학습 가이드입니다."
            "id" -> "Halo. Ini adalah panduan suara edukasi EduLens dalam bahasa Indonesia."
            "ru" -> "Здравствуйте. Это голосовой учебный гид EduLens на русском языке."
            "tr" -> "Merhaba. Bu, Türkçe EduLens sesli eğitim rehberidir."
            else -> "Hello. This is EduLens voice accessibility readout, ready for offline and global learning."
        }
        voiceController.speak(sampleLine, sectionId = "voice_test", languageCode = lang.code)
    }

    fun isVoiceLanguageAvailable(code: String): Boolean =
        voiceController.checkLanguageAvailable(code)

    fun isSpeechRecognizerAvailable(): Boolean = speechController.isAvailable()

    fun startVoiceInput(
        mode: VoiceInputMode,
        onStudyReady: (ObjectAnalysisResult) -> Unit = {}
    ) {
        voiceController.stop()
        val snapshot = uiState.value
        speechController.startListening(
            languageCode = snapshot.selectedLanguage.code,
            preferOffline = snapshot.isEffectivelyOffline,
            mode = mode,
            onPartial = { partial ->
                _searchQuery.value = partial
            },
            onFinal = { finalTranscript, completedMode ->
                applySpokenTranscript(
                    transcript = finalTranscript,
                    mode = completedMode,
                    onStudyReady = onStudyReady
                )
            }
        )
    }

    fun stopVoiceInput() {
        speechController.stopListening()
    }

    fun cancelVoiceInput() {
        speechController.cancel()
    }

    fun clearSpeechError() {
        speechController.clearError()
    }

    fun applySpokenTranscript(
        transcript: String,
        mode: VoiceInputMode,
        onStudyReady: (ObjectAnalysisResult) -> Unit = {}
    ) {
        val cleaned = transcript.trim()
        if (cleaned.isBlank()) return
        _searchQuery.value = cleaned

        val hasPhoto = _pendingPhotoPath.value != null
        if (mode == VoiceInputMode.IMAGE_CONTEXT || hasPhoto) {
            _statusBannerMessage.value =
                "Voice context added: \"$cleaned\". Tap Analyze Object to inspect."
        } else {
            analyzeCurrentInput(overrideQuery = cleaned, onStudyReady = onStudyReady)
        }
    }

    fun dismissBannerMessage() {
        _statusBannerMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceController.shutdown()
        speechController.destroy()
        networkMonitor.release()
    }
}
