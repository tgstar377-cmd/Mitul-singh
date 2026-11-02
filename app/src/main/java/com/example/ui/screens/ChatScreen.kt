package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AssistantAction
import com.example.data.ChatMessage
import com.example.ui.AssistantViewModel
import com.example.ui.components.ActionCard
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.AssistantBubbleBg
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UserBubbleBg
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.messages.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val isVoiceMuted by viewModel.isVoiceMuted.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()

    val partialSpeechText by viewModel.partialSpeechText.collectAsState()
    val speechRmsAmplitude by viewModel.speechRmsAmplitude.collectAsState()
    val speechErrorMessage by viewModel.speechErrorMessage.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Runtime Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListeningWithRecognizer(context)
        } else {
            Toast.makeText(context, "Microphone permission is required to speak commands.", Toast.LENGTH_LONG).show()
        }
    }

    // Direct SpeechRecognizer API trigger
    fun toggleSpeechRecognition() {
        if (isListening) {
            viewModel.stopListeningWithRecognizer()
        } else {
            val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.startListeningWithRecognizer(context)
            } else {
                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Show speech error toast if any occurs
    LaunchedEffect(speechErrorMessage) {
        speechErrorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickChips = listOf(
        "▶️ Play YouTube" to "Play top songs on YouTube",
        "💬 WhatsApp" to "Open WhatsApp",
        "📸 Open Camera" to "Open camera",
        "📞 Open Dialer" to "Call dialer kholo",
        "⏰ 5 Min Timer" to "Set 5 minute timer",
        "😂 Hindi Joke" to "Koi mazedaar joke sunao",
        "✨ Inspiring Quote" to "Shayari ya quote sunao",
        "💡 Capabilities" to "Tum kya kar sakte ho?",
        "📝 Quick Note" to "Take a note meeting at 5 PM"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
    ) {
        // Top Assistant Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            border = BorderStroke(1.dp, SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(CyanPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_tg_assistant),
                            contentDescription = "TG Assistant Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "TG AI Assistant",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening || isSpeaking) CyanGlow else Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSpeaking) "Speaking to $userName..." else if (isListening) "Listening..." else "Active for $userName",
                                color = if (isSpeaking || isListening) CyanGlow else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isSpeaking) {
                        IconButton(
                            onClick = { viewModel.stopSpeaking() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop voice",
                                tint = CyanGlow
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleVoiceMute() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isVoiceMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Voice toggle",
                            tint = if (isVoiceMuted) TextMuted else CyanGlow
                        )
                    }
                }
            }
        }

        // Main Chat Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                // Empty Welcome State with Glowing Orb
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GlowingOrb(
                        isSpeaking = isSpeaking,
                        isListening = isListening,
                        rmsAmplitude = speechRmsAmplitude,
                        size = 130.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Namaste $userName ji!",
                        color = CyanLight,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "I am your personal TG AI Assistant.\nAsk questions, launch apps, play music, or speak in Hindi & English.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Quick Actions:",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(messages) { message ->
                        ChatBubble(
                            message = message,
                            lastAction = if (message.sender == "assistant" && message == messages.lastOrNull()) lastAction else null,
                            onExecuteAction = { action -> viewModel.executeAction(context, action) }
                        )
                    }
                }
            }
        }

        // Real-time Speech Listening Indicator with Live Transcription
        AnimatedVisibility(
            visible = isListening,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyanGlow),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Listening via SpeechRecognizer API...",
                            color = CyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (partialSpeechText.isNotBlank()) "\"$partialSpeechText\"" else "Speak your command now, $userName...",
                            color = if (partialSpeechText.isNotBlank()) TextPrimary else TextMuted,
                            fontSize = 13.5.sp,
                            fontWeight = if (partialSpeechText.isNotBlank()) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    IconButton(
                        onClick = { viewModel.stopListeningWithRecognizer() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop listening",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        // Quick Suggestions Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickChips.forEach { (chipLabel, commandText) ->
                SuggestionChip(
                    onClick = { viewModel.sendTextMessage(commandText, context) },
                    label = {
                        Text(
                            text = chipLabel,
                            fontSize = 12.sp,
                            color = CyanLight,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceCard
                    ),
                    border = BorderStroke(1.dp, SurfaceCardBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(34.dp)
                )
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Ask or command in Hindi/English...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                maxLines = 3,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendTextMessage(inputText, context)
                        inputText = ""
                    }
                }),
                trailingIcon = {
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.sendTextMessage(inputText, context)
                                inputText = ""
                            },
                            modifier = Modifier.testTag("send_message_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = CyanGlow
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            // SpeechRecognizer Mic FAB Button
            FloatingActionButton(
                onClick = { toggleSpeechRecognition() },
                containerColor = if (isListening) Color(0xFFEF4444) else CyanPrimary,
                contentColor = DeepBackground,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .size(52.dp)
                    .testTag("voice_mic_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice input",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    lastAction: AssistantAction?,
    onExecuteAction: (AssistantAction) -> Unit
) {
    val isUser = message.sender == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TG",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.8f else 0.85f),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) UserBubbleBg else AssistantBubbleBg
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = BorderStroke(
                    1.dp,
                    if (isUser) ElectricIndigo.copy(alpha = 0.4f) else CyanPrimary.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.5.sp,
                        lineHeight = 21.sp
                    )

                    // Render action button if available
                    if (!isUser && lastAction != null && lastAction != AssistantAction.None) {
                        ActionCard(
                            action = lastAction,
                            onExecute = onExecuteAction
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = timeStr,
                color = TextMuted,
                fontSize = 10.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
