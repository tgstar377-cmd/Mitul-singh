package com.example.ai

sealed class AssistantAction {
    data class LaunchApp(
        val appName: String,
        val packageName: String,
        val fallbackUrl: String? = null,
        val buttonLabel: String = "Open $appName"
    ) : AssistantAction()

    data class SendWhatsApp(
        val message: String,
        val buttonLabel: String = "Send on WhatsApp"
    ) : AssistantAction()

    data class SendSms(
        val recipientPhone: String? = null,
        val message: String,
        val buttonLabel: String = "Send SMS"
    ) : AssistantAction()

    data class DialPhone(
        val phoneNumber: String? = null,
        val buttonLabel: String = "Open Dialer"
    ) : AssistantAction()

    data class WebSearch(
        val query: String,
        val buttonLabel: String = "Search on Google"
    ) : AssistantAction()

    data class PlayMedia(
        val query: String,
        val buttonLabel: String = "Play on YouTube"
    ) : AssistantAction()

    data class SetTimer(
        val minutes: Int,
        val buttonLabel: String = "Open Clock Timer"
    ) : AssistantAction()

    data class SaveNote(
        val title: String,
        val content: String,
        val isReminder: Boolean = false
    ) : AssistantAction()

    data object OpenCamera : AssistantAction()
    data object OpenSettings : AssistantAction()
    data object ShowSystemInfo : AssistantAction()
    data object None : AssistantAction()
}

data class AssistantResponse(
    val replyText: String,
    val spokenText: String = replyText,
    val action: AssistantAction = AssistantAction.None,
    val language: String = "en" // "en" or "hi"
)
