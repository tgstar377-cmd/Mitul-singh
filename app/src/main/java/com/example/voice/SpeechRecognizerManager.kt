package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechRecognizerManager(
    private val context: Context,
    private val onResult: (String) -> Unit
) : RecognitionListener {

    private val tag = "SpeechRecognizerMgr"
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _rmsAmplitude = MutableStateFlow(0f)
    val rmsAmplitude: StateFlow<Float> = _rmsAmplitude.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(this@SpeechRecognizerManager)
                    }
                    Log.d(tag, "SpeechRecognizer initialized successfully.")
                } else {
                    Log.w(tag, "SpeechRecognizer not available on this device.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to create SpeechRecognizer", e)
            }
        }
    }

    fun startListening(languagePreference: String = "hi-IN") {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initRecognizer()
                }

                _errorMessage.value = null
                _partialText.value = ""

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, languagePreference)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languagePreference)
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languagePreference)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                }

                speechRecognizer?.startListening(intent)
                _isListening.value = true
                Log.d(tag, "Started listening with SpeechRecognizer API in $languagePreference")
            } catch (e: Exception) {
                Log.e(tag, "Error starting SpeechRecognizer", e)
                _isListening.value = false
                _errorMessage.value = "Microphone error: ${e.localizedMessage}"
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                _isListening.value = false
            } catch (e: Exception) {
                Log.e(tag, "Error stopping SpeechRecognizer", e)
            }
        }
    }

    fun cancel() {
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                _isListening.value = false
                _partialText.value = ""
                _rmsAmplitude.value = 0f
            } catch (e: Exception) {
                Log.e(tag, "Error cancelling SpeechRecognizer", e)
            }
        }
    }

    fun destroy() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                _isListening.value = false
            } catch (e: Exception) {
                Log.e(tag, "Error destroying SpeechRecognizer", e)
            }
        }
    }

    // RecognitionListener Callbacks
    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        Log.d(tag, "Ready for speech")
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
        Log.d(tag, "User began speaking")
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize RMS dB typically ranging between -2 to 10
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _rmsAmplitude.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) = Unit

    override fun onEndOfSpeech() {
        _isListening.value = false
        Log.d(tag, "User ended speech")
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _rmsAmplitude.value = 0f
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client recognition error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try speaking again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognition busy, retrying..."
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
            else -> "Speech recognition error ($error)"
        }
        Log.w(tag, "SpeechRecognizer onError: $errorMsg ($error)")

        // For NO_MATCH or SPEECH_TIMEOUT, keep it clean
        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            _errorMessage.value = errorMsg
        }
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _rmsAmplitude.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val finalResult = matches?.firstOrNull().orEmpty().trim()
        Log.d(tag, "SpeechRecognizer onResults: $finalResult")
        _partialText.value = ""

        if (finalResult.isNotBlank()) {
            onResult(finalResult)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val partial = matches?.firstOrNull().orEmpty().trim()
        if (partial.isNotBlank()) {
            _partialText.value = partial
            Log.d(tag, "SpeechRecognizer partial: $partial")
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) = Unit
}
