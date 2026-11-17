package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class VoiceInputMode {
    VOICE_SEARCH,
    IMAGE_CONTEXT
}

data class SpeechRecognitionState(
    val isAvailable: Boolean = true,
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val rmsDb: Float = 0f,
    val activeMode: VoiceInputMode = VoiceInputMode.VOICE_SEARCH,
    val errorMessage: String? = null
)

class SpeechToTextController(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(
        SpeechRecognitionState(isAvailable = SpeechRecognizer.isRecognitionAvailable(appContext))
    )
    val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null
    private var onFinalTranscript: ((String, VoiceInputMode) -> Unit)? = null
    private var onPartialTranscript: ((String) -> Unit)? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening(
        languageCode: String,
        preferOffline: Boolean,
        mode: VoiceInputMode,
        onPartial: (String) -> Unit,
        onFinal: (String, VoiceInputMode) -> Unit
    ) {
        mainHandler.post {
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                _state.value = _state.value.copy(
                    isAvailable = false,
                    isListening = false,
                    errorMessage = "Speech recognition service unavailable."
                )
                return@post
            }

            onPartialTranscript = onPartial
            onFinalTranscript = onFinal

            ensureRecognizer()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                if (preferOffline) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            }

            _state.value = _state.value.copy(
                isAvailable = true,
                isListening = true,
                partialTranscript = "",
                rmsDb = 0f,
                activeMode = mode,
                errorMessage = null
            )

            runCatching {
                recognizer?.startListening(intent)
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    isListening = false,
                    errorMessage = err.localizedMessage ?: "Failed to start voice listening."
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            runCatching { recognizer?.stopListening() }
            _state.value = _state.value.copy(isListening = false)
        }
    }

    fun cancel() {
        mainHandler.post {
            runCatching { recognizer?.cancel() }
            _state.value = _state.value.copy(
                isListening = false,
                partialTranscript = "",
                rmsDb = 0f
            )
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    fun destroy() {
        mainHandler.post {
            runCatching {
                recognizer?.cancel()
                recognizer?.destroy()
            }
            recognizer = null
        }
    }

    private fun ensureRecognizer() {
        if (recognizer != null) return
        recognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _state.value = _state.value.copy(isListening = true, errorMessage = null)
                }

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) {
                    _state.value = _state.value.copy(rmsDb = rmsdB.coerceIn(0f, 12f))
                }

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    _state.value = _state.value.copy(isListening = false)
                }

                override fun onError(error: Int) {
                    val message = mapSpeechError(error)
                    _state.value = _state.value.copy(
                        isListening = false,
                        errorMessage = message
                    )
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val transcript = matches?.firstOrNull()?.trim().orEmpty()
                    val mode = _state.value.activeMode
                    _state.value = _state.value.copy(
                        isListening = false,
                        partialTranscript = transcript
                    )
                    if (transcript.isNotBlank()) {
                        onFinalTranscript?.invoke(transcript, mode)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        ?.trim()
                        .orEmpty()
                    if (partial.isNotBlank()) {
                        _state.value = _state.value.copy(partialTranscript = partial)
                        onPartialTranscript?.invoke(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    private fun mapSpeechError(errorCode: Int): String = when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Microphone audio recording error."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for live voice input."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Network speech service unavailable. Try Offline mode or type your query."
        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that clearly. Please try speaking again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Please wait a moment."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap the mic and speak clearly."
        else -> "Voice input stopped (code $errorCode)."
    }
}
