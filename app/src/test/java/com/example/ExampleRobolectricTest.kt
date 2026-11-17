package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.SampleObjectAnalysisRepository
import com.example.data.local.EduLensDatabase
import com.example.data.local.StudyHistoryRepository
import com.example.model.SupportedLanguages
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EduLens", appName)
    }

    @Test
    fun `sample analysis returns clearly labeled multilingual guide with structures facts and solutions`() = runTest {
        val repository = SampleObjectAnalysisRepository()
        val spanish = SupportedLanguages.findByCode("es")

        val result = repository.analyzeObject(
            queryText = "Solar panel",
            compressedImagePath = null,
            payloadKb = 20,
            targetLanguage = spanish,
            lowBandwidthMode = true
        )

        assertTrue(result.isSamplePlaceholder)
        assertEquals("es", result.languageCode)
        assertTrue(result.structures.isNotEmpty())
        assertTrue(result.facts.isNotEmpty())
        assertTrue(result.solutions.isNotEmpty())
        assertTrue(result.summary.contains("Español"))
    }

    @Test
    fun `offline room repository seeds starter pack and persists new studies`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, EduLensDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val sampleRepo = SampleObjectAnalysisRepository()
            val historyRepo = StudyHistoryRepository(db.savedStudyDao())

            historyRepo.ensureStarterPackSeeded(sampleRepo)
            val initial = historyRepo.savedStudies.first()
            assertEquals(3, initial.size)

            val custom = sampleRepo.analyzeObject(
                queryText = "Water filter",
                compressedImagePath = null,
                payloadKb = 18,
                targetLanguage = SupportedLanguages.findByCode("en"),
                lowBandwidthMode = true
            )
            historyRepo.saveForOffline(custom)

            val updated = historyRepo.savedStudies.first()
            assertEquals(4, updated.size)
            assertTrue(updated.any { it.objectTitle.contains("Water Filter") && it.isSavedOffline })

            val loadedById = historyRepo.getStudyById(custom.id)
            assertEquals(custom.objectTitle, loadedById?.objectTitle)
            assertEquals(custom.structures.size, loadedById?.structures?.size)
            assertEquals(custom.facts.size, loadedById?.facts?.size)
            assertEquals(custom.solutions.size, loadedById?.solutions?.size)
        } finally {
            db.close()
        }
    }

    @Test
    fun `analysis prompt includes selected language parameter`() {
        val arabic = SupportedLanguages.findByCode("ar")
        val prompt = com.example.data.ai.ObjectAnalysisRepository.buildAnalysisPrompt(
            queryText = "Mechanical Watch",
            targetLanguage = arabic,
            lowBandwidthMode = false
        )

        assertTrue(prompt.contains("Arabic"))
        assertTrue(prompt.contains("العربية"))
        assertTrue(prompt.contains("locale=ar"))
        assertTrue(prompt.contains("Mechanical Watch"))
    }

    @Test
    fun `spoken transcript populates search query for voice image context`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val store = androidx.lifecycle.ViewModelStore()
        val vm = androidx.lifecycle.ViewModelProvider(
            store,
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        )[com.example.ui.viewmodel.EduLensViewModel::class.java]

        val collectorJob = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main.immediate
        ).launch {
            vm.uiState.collect {}
        }

        try {
            vm.applySpokenTranscript(
                transcript = "Explain the photovoltaic silicon layers",
                mode = com.example.util.VoiceInputMode.IMAGE_CONTEXT
            )

            val deadline = System.currentTimeMillis() + 5_000L
            while (
                vm.uiState.value.searchQuery != "Explain the photovoltaic silicon layers" &&
                System.currentTimeMillis() < deadline
            ) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
                    .idleFor(50, java.util.concurrent.TimeUnit.MILLISECONDS)
                Thread.sleep(20)
            }
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

            assertEquals("Explain the photovoltaic silicon layers", vm.uiState.value.searchQuery)
            assertTrue(
                vm.uiState.value.statusBannerMessage?.contains("Voice context added") == true
            )
        } finally {
            collectorJob.cancel()
            store.clear()
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `voice search transcript triggers educational analysis in selected language`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val store = androidx.lifecycle.ViewModelStore()
        val vm = androidx.lifecycle.ViewModelProvider(
            store,
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        )[com.example.ui.viewmodel.EduLensViewModel::class.java]

        val collectorJob = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main.immediate
        ).launch {
            vm.uiState.collect {}
        }

        try {
            var readyTitle: String? = null
            vm.applySpokenTranscript(
                transcript = "Solar Panel",
                mode = com.example.util.VoiceInputMode.VOICE_SEARCH,
                onStudyReady = { result -> readyTitle = result.objectTitle }
            )

            val deadline = System.currentTimeMillis() + 5_000L
            while (
                (vm.uiState.value.activeDetailStudy == null || readyTitle == null) &&
                System.currentTimeMillis() < deadline
            ) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
                    .idleFor(50, java.util.concurrent.TimeUnit.MILLISECONDS)
                Thread.sleep(20)
            }
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

            assertEquals("Solar Panel", vm.uiState.value.searchQuery)
            assertTrue(vm.uiState.value.activeDetailStudy?.structures?.isNotEmpty() == true)
            assertTrue(readyTitle != null)
        } finally {
            collectorJob.cancel()
            store.clear()
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `share helper formats educational content and exports solution and object images via FileProvider`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sampleRepo = SampleObjectAnalysisRepository()
        val english = SupportedLanguages.findByCode("en")
        val study = sampleRepo.analyzeObject(
            queryText = "Solar Panel",
            compressedImagePath = null,
            payloadKb = 22,
            targetLanguage = english,
            lowBandwidthMode = false
        ).copy(imageSource = "drawable://img_sample_solar")

        val formatted = com.example.util.StudyShareHelper.formatEducationalText(study, english)
        assertTrue(formatted.contains(study.objectTitle))
        assertTrue(formatted.contains("Structural Breakdown"))
        assertTrue(formatted.contains("Educational Facts"))
        assertTrue(formatted.contains("Practical Solutions"))
        assertTrue(formatted.contains(study.facts.first().title))
        assertTrue(formatted.contains(study.solutions.first().problemTitle))

        val fullUris = com.example.util.StudyShareHelper.prepareShareImageUris(
            context = context,
            study = study,
            language = english,
            option = com.example.util.ShareExportOption.FULL_PACKAGE
        )
        assertEquals(2, fullUris.size)
        assertTrue(fullUris.all { it.scheme == "content" && it.authority == "${context.packageName}.fileprovider" })

        val fullIntent = com.example.util.StudyShareHelper.buildRawShareIntent(
            context = context,
            study = study,
            language = english,
            imageUris = fullUris,
            option = com.example.util.ShareExportOption.FULL_PACKAGE
        )
        assertEquals(android.content.Intent.ACTION_SEND_MULTIPLE, fullIntent.action)
        assertEquals("image/*", fullIntent.type)
        assertTrue((fullIntent.flags and android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0)
        assertEquals(formatted, fullIntent.getStringExtra(android.content.Intent.EXTRA_TEXT))

        val solutionOnlyUris = com.example.util.StudyShareHelper.prepareShareImageUris(
            context = context,
            study = study,
            language = english,
            option = com.example.util.ShareExportOption.SOLUTION_IMAGE
        )
        assertEquals(1, solutionOnlyUris.size)
        val singleImageIntent = com.example.util.StudyShareHelper.buildRawShareIntent(
            context = context,
            study = study,
            language = english,
            imageUris = solutionOnlyUris,
            option = com.example.util.ShareExportOption.SOLUTION_IMAGE
        )
        assertEquals(android.content.Intent.ACTION_SEND, singleImageIntent.action)
        assertEquals("image/*", singleImageIntent.type)

        val textIntent = com.example.util.StudyShareHelper.buildRawShareIntent(
            context = context,
            study = study,
            language = english,
            imageUris = emptyList(),
            option = com.example.util.ShareExportOption.TEXT_NOTES
        )
        assertEquals(android.content.Intent.ACTION_SEND, textIntent.action)
        assertEquals("text/plain", textIntent.type)
    }

    @Test
    fun `theme switcher updates and persists light and dark modes`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val store = androidx.lifecycle.ViewModelStore()
        val vm = androidx.lifecycle.ViewModelProvider(
            store,
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        )[com.example.ui.viewmodel.EduLensViewModel::class.java]

        val collectorJob = kotlinx.coroutines.CoroutineScope(
            kotlinx.coroutines.Dispatchers.Main.immediate
        ).launch {
            vm.uiState.collect {}
        }

        try {
            vm.setThemeMode(com.example.model.ThemeMode.DARK)

            val deadline1 = System.currentTimeMillis() + 5_000L
            while (
                vm.uiState.value.preferences.themeMode != com.example.model.ThemeMode.DARK &&
                System.currentTimeMillis() < deadline1
            ) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
                    .idleFor(50, java.util.concurrent.TimeUnit.MILLISECONDS)
                Thread.sleep(20)
            }
            assertEquals(com.example.model.ThemeMode.DARK, vm.uiState.value.preferences.themeMode)

            vm.toggleDayNightTheme(currentlyDark = true)

            val deadline2 = System.currentTimeMillis() + 5_000L
            while (
                vm.uiState.value.preferences.themeMode != com.example.model.ThemeMode.LIGHT &&
                System.currentTimeMillis() < deadline2
            ) {
                org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper())
                    .idleFor(50, java.util.concurrent.TimeUnit.MILLISECONDS)
                Thread.sleep(20)
            }
            assertEquals(com.example.model.ThemeMode.LIGHT, vm.uiState.value.preferences.themeMode)
        } finally {
            collectorJob.cancel()
            store.clear()
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `camera focus overlay displays reticle on tap and hides when idle`() {
        var focusState by mutableStateOf(com.example.ui.components.TapFocusState())

        composeTestRule.setContent {
            com.example.ui.components.CameraFocusOverlay(focusState = focusState)
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("camera_focus_reticle").assertDoesNotExist()

        focusState = com.example.ui.components.TapFocusState(
            tapOffset = androidx.compose.ui.geometry.Offset(180f, 240f),
            status = com.example.ui.components.FocusStatus.FOCUSING,
            requestId = 1L
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("camera_focus_reticle").assertIsDisplayed()

        focusState = focusState.copy(status = com.example.ui.components.FocusStatus.LOCKED)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("camera_focus_reticle").assertIsDisplayed()

        focusState = com.example.ui.components.TapFocusState()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("camera_focus_reticle").assertDoesNotExist()
    }

    @Test
    fun `camera zoom clamping and zoom controls update target zoom ratio`() {
        assertEquals(1.0f, com.example.ui.components.clampZoomRatio(0.4f, 1.0f, 5.0f), 0.001f)
        assertEquals(5.0f, com.example.ui.components.clampZoomRatio(9.2f, 1.0f, 5.0f), 0.001f)
        assertEquals(2.4f, com.example.ui.components.clampZoomRatio(2.4f, 1.0f, 5.0f), 0.001f)
        assertEquals("2.4x", com.example.ui.components.formatZoomLabel(2.4f))

        var selectedZoom by mutableStateOf(1.0f)
        composeTestRule.setContent {
            com.example.ui.components.CameraZoomControls(
                currentZoomRatio = selectedZoom,
                minZoomRatio = 1.0f,
                maxZoomRatio = 4.0f,
                onSelectZoomRatio = { selectedZoom = it }
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("camera_zoom_controls").assertIsDisplayed()
        composeTestRule.onNodeWithTag("camera_zoom_preset_2.0x").assertIsDisplayed()

        // Simulate tapping 2.0x preset
        composeTestRule.onNodeWithTag("camera_zoom_preset_2.0x")
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(2.0f, selectedZoom, 0.001f)
    }
}
