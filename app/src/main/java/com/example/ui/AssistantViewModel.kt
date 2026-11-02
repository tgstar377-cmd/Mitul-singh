package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AssistantAction
import com.example.ai.AssistantBrain
import com.example.ai.AssistantResponse
import com.example.data.AssistantDatabase
import com.example.data.AssistantNote
import com.example.data.ChatMessage
import com.example.util.IntentExecutor
import com.example.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AssistantTab {
    CHAT, COMMANDS, NOTES, SETTINGS
}

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AssistantDatabase.getDatabase(application)
    private val chatDao = db.chatMessageDao()
    private val noteDao = db.assistantNoteDao()

    val voiceManager = VoiceManager(application)
    private var activeContext: Context? = null

    val speechRecognizerManager = com.example.voice.SpeechRecognizerManager(application) { spokenText ->
        processInput(spokenText, activeContext)
    }

    val messages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<AssistantNote>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val isMaleVoice: StateFlow<Boolean> = voiceManager.isMaleVoice
    val availableVoiceProfiles = voiceManager.availableProfiles
    val selectedVoiceProfileId: StateFlow<String> = voiceManager.selectedProfileId

    val isListening: StateFlow<Boolean> = speechRecognizerManager.isListening
    val partialSpeechText: StateFlow<String> = speechRecognizerManager.partialText
    val speechRmsAmplitude: StateFlow<Float> = speechRecognizerManager.rmsAmplitude
    val speechErrorMessage: StateFlow<String?> = speechRecognizerManager.errorMessage

    private val _isVoiceMuted = MutableStateFlow(false)
    val isVoiceMuted: StateFlow<Boolean> = _isVoiceMuted.asStateFlow()

    private val _currentTab = MutableStateFlow(AssistantTab.CHAT)
    val currentTab: StateFlow<AssistantTab> = _currentTab.asStateFlow()

    private val _userName = MutableStateFlow("Mitul")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _speechRate = MutableStateFlow(0.95f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _lastAction = MutableStateFlow<AssistantAction?>(null)
    val lastAction: StateFlow<AssistantAction?> = _lastAction.asStateFlow()

    init {
        // Seed initial greeting if chat is completely empty
        viewModelScope.launch {
            // Note: chatDao.getAllMessages() is a Flow; we insert welcome if empty
            // We do a one-time check
        }
    }

    fun setTab(tab: AssistantTab) {
        _currentTab.value = tab
    }

    fun setUserName(name: String) {
        if (name.isNotBlank()) {
            _userName.value = name.trim()
        }
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
    }

    fun setMaleVoice(enabled: Boolean) {
        voiceManager.setMaleVoiceEnabled(enabled)
    }

    fun selectVoiceProfile(profileId: String) {
        voiceManager.selectVoiceProfile(profileId)
    }

    fun previewVoice(profile: com.example.voice.VoiceProfile) {
        voiceManager.previewVoice(profile)
    }

    fun toggleVoiceMute() {
        val next = !_isVoiceMuted.value
        _isVoiceMuted.value = next
        if (next) {
            voiceManager.stop()
        }
    }

    fun stopSpeaking() {
        voiceManager.stop()
    }

    fun startListeningWithRecognizer(context: Context) {
        activeContext = context
        voiceManager.stop()
        speechRecognizerManager.startListening()
    }

    fun stopListeningWithRecognizer() {
        speechRecognizerManager.stopListening()
    }

    fun toggleListening(context: Context) {
        if (speechRecognizerManager.isListening.value) {
            stopListeningWithRecognizer()
        } else {
            startListeningWithRecognizer(context)
        }
    }

    fun onUserSpeechResult(text: String, context: Context? = null) {
        if (text.isNotBlank()) {
            processInput(text, context ?: activeContext)
        }
    }

    fun sendTextMessage(text: String, context: Context? = null) {
        if (text.isNotBlank()) {
            processInput(text.trim(), context)
        }
    }

    private fun processInput(input: String, context: Context?) {
        viewModelScope.launch {
            // 1. Insert user message
            val userMsg = ChatMessage(
                sender = "user",
                text = input,
                timestamp = System.currentTimeMillis()
            )
            chatDao.insertMessage(userMsg)

            // 2. Process with Assistant Brain
            val response: AssistantResponse = AssistantBrain.processQuery(input, _userName.value)

            // 3. Handle specific action side effects
            when (val act = response.action) {
                is AssistantAction.SaveNote -> {
                    val newNote = AssistantNote(
                        title = act.title,
                        content = act.content,
                        isReminder = act.isReminder,
                        timestamp = System.currentTimeMillis()
                    )
                    noteDao.insertNote(newNote)
                }
                else -> Unit
            }

            // 4. Insert Assistant message
            val actionType = when (response.action) {
                is AssistantAction.LaunchApp -> "launch_app"
                is AssistantAction.PlayMedia -> "play_media"
                is AssistantAction.SendWhatsApp -> "send_whatsapp"
                is AssistantAction.SendSms -> "send_sms"
                is AssistantAction.DialPhone -> "dial_phone"
                is AssistantAction.WebSearch -> "web_search"
                is AssistantAction.SetTimer -> "set_timer"
                is AssistantAction.SaveNote -> "save_note"
                is AssistantAction.OpenCamera -> "open_camera"
                is AssistantAction.OpenSettings -> "open_settings"
                is AssistantAction.ShowSystemInfo -> "show_system_info"
                AssistantAction.None -> null
            }

            val assistantMsg = ChatMessage(
                sender = "assistant",
                text = response.replyText,
                timestamp = System.currentTimeMillis() + 10,
                actionType = actionType,
                language = response.language
            )
            chatDao.insertMessage(assistantMsg)
            _lastAction.value = response.action

            // 5. Speak out if not muted
            if (!_isVoiceMuted.value) {
                voiceManager.speak(
                    text = response.spokenText,
                    language = response.language,
                    speechRate = _speechRate.value
                )
            }

            // Auto-trigger direct actions if context is available so Mitul's commands execute instantly
            if (context != null) {
                when (response.action) {
                    is AssistantAction.LaunchApp,
                    is AssistantAction.PlayMedia,
                    is AssistantAction.OpenCamera,
                    is AssistantAction.OpenSettings,
                    is AssistantAction.SetTimer -> {
                        IntentExecutor.execute(context, response.action)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun executeAction(context: Context, action: AssistantAction) {
        IntentExecutor.execute(context, action)
    }

    fun addNote(title: String, content: String, isReminder: Boolean) {
        viewModelScope.launch {
            val note = AssistantNote(
                title = title.ifBlank { "Note" },
                content = content,
                isReminder = isReminder,
                timestamp = System.currentTimeMillis()
            )
            noteDao.insertNote(note)
        }
    }

    fun toggleNoteCompletion(note: AssistantNote) {
        viewModelScope.launch {
            noteDao.updateNote(note.copy(isCompleted = !note.isCompleted))
        }
    }

    fun deleteNote(note: AssistantNote) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
            voiceManager.stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
        speechRecognizerManager.destroy()
    }
}
