package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoicePlaybackState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val activeSectionId: String? = null,
    val languageSupportedOnDevice: Boolean = true
)

class VoiceReadoutController(context: Context) {

    private val _state = MutableStateFlow(VoicePlaybackState())
    val state: StateFlow<VoicePlaybackState> = _state.asStateFlow()

    private var currentLanguageCode: String = "en"
    private var currentSpeechRate: Float = 1.0f

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureListener()
                val supported = applyLocale(currentLanguageCode)
                tts?.setSpeechRate(currentSpeechRate)
                _state.value = _state.value.copy(
                    isReady = true,
                    languageSupportedOnDevice = supported
                )
            } else {
                _state.value = _state.value.copy(isReady = false)
            }
        }
    }

    private fun configureListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _state.value = _state.value.copy(
                    isSpeaking = true,
                    activeSectionId = utteranceId
                )
            }

            override fun onDone(utteranceId: String?) {
                _state.value = _state.value.copy(
                    isSpeaking = false,
                    activeSectionId = null
                )
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _state.value = _state.value.copy(
                    isSpeaking = false,
                    activeSectionId = null
                )
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _state.value = _state.value.copy(
                    isSpeaking = false,
                    activeSectionId = null
                )
            }
        })
    }

    fun updateLanguage(languageCode: String): Boolean {
        currentLanguageCode = languageCode
        val supported = applyLocale(languageCode)
        _state.value = _state.value.copy(languageSupportedOnDevice = supported)
        return supported
    }

    fun checkLanguageAvailable(languageCode: String): Boolean {
        val engine = tts ?: return true
        val locale = Locale.forLanguageTag(languageCode)
        val result = engine.isLanguageAvailable(locale)
        return result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
    }

    fun updateSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.6f, 1.8f)
        tts?.setSpeechRate(currentSpeechRate)
    }

    fun speak(text: String, sectionId: String, languageCode: String = currentLanguageCode) {
        val engine = tts ?: return
        if (!_state.value.isReady || text.isBlank()) return
        applyLocale(languageCode)
        engine.setSpeechRate(currentSpeechRate)
        _state.value = _state.value.copy(isSpeaking = true, activeSectionId = sectionId)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, sectionId)
    }

    fun stop() {
        tts?.stop()
        _state.value = _state.value.copy(isSpeaking = false, activeSectionId = null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    private fun applyLocale(languageCode: String): Boolean {
        val engine = tts ?: return false
        val locale = Locale.forLanguageTag(languageCode)
        val availability = engine.setLanguage(locale)
        return availability != TextToSpeech.LANG_MISSING_DATA &&
            availability != TextToSpeech.LANG_NOT_SUPPORTED
    }
}
