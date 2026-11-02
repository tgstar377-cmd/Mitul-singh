package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class VoiceProfile(
    val id: String,
    val name: String,
    val subtitle: String,
    val pitch: Float,
    val speechRate: Float,
    val sampleHindi: String,
    val badge: String,
    val isFemale: Boolean
)

class VoiceManager(context: Context) : TextToSpeech.OnInitListener {

    private val tag = "VoiceManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Default to sweet girl voice as requested by Mitul
    private val _isFemaleVoice = MutableStateFlow(true)
    val isFemaleVoice: StateFlow<Boolean> = _isFemaleVoice.asStateFlow()

    // Keep isMaleVoice for backward compatibility with existing collectors
    val isMaleVoice: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    val availableProfiles = listOf(
        VoiceProfile(
            id = "sweet_girl",
            name = "1. Sweet Girl (लड़की की प्यारी आवाज़)",
            subtitle = "Melodic, sweet, pleasant & caring Indian girl voice",
            pitch = 1.22f,
            speechRate = 0.95f,
            sampleHindi = "नमस्ते मितुल जी! कहिए, मैं आपके लिए क्या काम करूँ? YouTube खोलूँ या कुछ और?",
            badge = "🌸 लड़की की आवाज़ (Active)",
            isFemale = true
        ),
        VoiceProfile(
            id = "gentle_girl",
            name = "2. Gentle Girl (शांत व मधुर लड़की)",
            subtitle = "Soft, respectful & gentle companion female tone",
            pitch = 1.15f,
            speechRate = 0.92f,
            sampleHindi = "हाँ मितुल जी, आपका काम तुरंत पूरा कर दिया!",
            badge = "✨ Soft & Caring",
            isFemale = true
        ),
        VoiceProfile(
            id = "energetic_girl",
            name = "3. Energetic Girl (तेज़ व खुशमिजाज लड़की)",
            subtitle = "Lively, cheerful & fast-acting assistant voice",
            pitch = 1.28f,
            speechRate = 1.05f,
            sampleHindi = "हाँ मितुल जी, YouTube open कर दिया है, मजे कीजिए!",
            badge = "⚡ Fast & Energetic",
            isFemale = true
        ),
        VoiceProfile(
            id = "smart_boy",
            name = "4. Smart Boy (लड़के की आवाज़)",
            subtitle = "Friendly young male brotherly tone",
            pitch = 0.88f,
            speechRate = 0.98f,
            sampleHindi = "हाँ मितुल भाई! TG Assistant ready hai.",
            badge = "👦 Boy Voice",
            isFemale = false
        )
    )

    private val _selectedProfileId = MutableStateFlow("sweet_girl")
    val selectedProfileId: StateFlow<String> = _selectedProfileId.asStateFlow()

    private val hindiLocale = Locale("hi", "IN")
    private val englishLocale = Locale("en", "IN")
    private val englishUsLocale = Locale.US

    // Sweet female pitch calibration (1.20f - 1.25f produces a pleasant, feminine, sweet voice)
    private var currentPitch = 1.22f
    private var currentSpeechRate = 0.95f

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setSpeechRate(currentSpeechRate)
            tts?.setPitch(currentPitch)

            applyVoice(hindiLocale, isFemale = true)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
            Log.d(tag, "TTS initialized with Sweet Girl Voice profile.")
        } else {
            Log.e(tag, "TTS initialization failed with status $status")
        }
    }

    fun selectVoiceProfile(profileId: String) {
        val profile = availableProfiles.firstOrNull { it.id == profileId } ?: return
        _selectedProfileId.value = profile.id
        _isFemaleVoice.value = profile.isFemale
        currentPitch = profile.pitch
        currentSpeechRate = profile.speechRate
        tts?.setPitch(currentPitch)
        tts?.setSpeechRate(currentSpeechRate)
    }

    fun previewVoice(profile: VoiceProfile) {
        if (!isInitialized || tts == null) return

        tts?.setPitch(profile.pitch)
        tts?.setSpeechRate(profile.speechRate)

        val targetLocale = hindiLocale
        val check = tts?.isLanguageAvailable(targetLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        if (check >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = targetLocale
            applyVoice(targetLocale, isFemale = profile.isFemale)
        } else {
            tts?.language = englishUsLocale
            applyVoice(englishUsLocale, isFemale = profile.isFemale)
        }

        val utteranceId = "PREVIEW_${profile.id}_${System.currentTimeMillis()}"
        tts?.speak(profile.sampleHindi, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun setMaleVoiceEnabled(enabled: Boolean) {
        _isFemaleVoice.value = !enabled
        currentPitch = if (enabled) 0.88f else 1.22f
        tts?.setPitch(currentPitch)
    }

    fun setPitch(pitch: Float) {
        currentPitch = pitch
        tts?.setPitch(pitch)
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate
        tts?.setSpeechRate(rate)
    }

    private fun applyVoice(targetLocale: Locale, isFemale: Boolean) {
        if (!isInitialized || tts == null) return

        try {
            val availableVoices = tts?.voices
            if (!availableVoices.isNullOrEmpty()) {
                val matchedVoice = if (isFemale) {
                    availableVoices.firstOrNull { voice ->
                        val matchesLang = voice.locale.language.equals(targetLocale.language, ignoreCase = true)
                        val isFem = voice.name.contains("female", ignoreCase = true) ||
                                voice.name.contains("#female", ignoreCase = true) ||
                                voice.name.contains("hif", ignoreCase = true) ||
                                voice.name.contains("enc", ignoreCase = true)
                        matchesLang && isFem
                    } ?: availableVoices.firstOrNull { voice ->
                        voice.locale.language.equals(targetLocale.language, ignoreCase = true) &&
                                !voice.name.contains("male", ignoreCase = true)
                    }
                } else {
                    availableVoices.firstOrNull { voice ->
                        val matchesLang = voice.locale.language.equals(targetLocale.language, ignoreCase = true)
                        val isMale = voice.name.contains("male", ignoreCase = true) ||
                                voice.name.contains("#male", ignoreCase = true)
                        matchesLang && isMale
                    }
                }

                if (matchedVoice != null) {
                    tts?.voice = matchedVoice
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "Could not set custom voice: ${e.message}")
        }
    }

    fun speak(text: String, language: String = "en", speechRate: Float? = null) {
        if (!isInitialized || tts == null) return

        val rate = speechRate ?: currentSpeechRate
        tts?.setSpeechRate(rate)
        tts?.setPitch(currentPitch)

        val targetLocale = if (language == "hi") hindiLocale else englishLocale
        val check = tts?.isLanguageAvailable(targetLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED

        if (check >= TextToSpeech.LANG_AVAILABLE) {
            tts?.language = targetLocale
            applyVoice(targetLocale, isFemale = _isFemaleVoice.value)
        } else {
            tts?.language = englishUsLocale
            applyVoice(englishUsLocale, isFemale = _isFemaleVoice.value)
        }

        val utteranceId = "TG_UTTERANCE_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
