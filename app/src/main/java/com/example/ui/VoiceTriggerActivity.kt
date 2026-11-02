package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.AssistantAction
import com.example.ai.AssistantBrain
import com.example.ui.components.GlowingOrb
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.IntentExecutor
import com.example.voice.SpeechRecognizerManager
import com.example.voice.VoiceManager
import kotlinx.coroutines.delay

class VoiceTriggerActivity : ComponentActivity() {

    private lateinit var voiceManager: VoiceManager
    private lateinit var speechManager: SpeechRecognizerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = VoiceManager(this)
        speechManager = SpeechRecognizerManager(this) { spokenText ->
            handleSpokenCommand(spokenText)
        }

        setContent {
            MyApplicationTheme {
                FloatingVoiceDialog(
                    speechManager = speechManager,
                    onDismiss = { finish() },
                    onRetry = { startListening() }
                )
            }
        }

        // Check permission and start listening immediately
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startListening()
        } else {
            Toast.makeText(this, "Microphone permission required for TG Assistant.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startListening() {
        voiceManager.stop()
        speechManager.startListening("hi-IN")
    }

    private fun handleSpokenCommand(input: String) {
        val response = AssistantBrain.processQuery(input, "Mitul")

        // Speak response out loud in male voice
        voiceManager.speak(response.spokenText, response.language)

        // Execute action if any
        if (response.action != AssistantAction.None) {
            IntentExecutor.execute(applicationContext, response.action)
        }

        // Close overlay after executing
        window.decorView.postDelayed({
            finish()
        }, 1800)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.destroy()
        voiceManager.shutdown()
    }
}

@Composable
fun FloatingVoiceDialog(
    speechManager: SpeechRecognizerManager,
    onDismiss: () -> Unit,
    onRetry: () -> Unit
) {
    val isListening by speechManager.isListening.collectAsState()
    val partialText by speechManager.partialText.collectAsState()
    val rmsAmplitude by speechManager.rmsAmplitude.collectAsState()
    val errorMessage by speechManager.errorMessage.collectAsState()

    var statusMessage by remember { mutableStateOf("Listening to Mitul bhai...") }

    LaunchedEffect(isListening, partialText, errorMessage) {
        statusMessage = when {
            errorMessage != null -> errorMessage ?: "Tap to retry"
            partialText.isNotBlank() -> "\"$partialText\""
            isListening -> "Listening... Say: \"TGA YouTube khol do\""
            else -> "Processing command..."
        }
    }

    // Outer full screen scrim (tapping outside dismisses)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.5.dp, CyanGlow.copy(alpha = 0.8f)),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* prevent click through */ }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isListening) CyanGlow else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TG Quick Assistant",
                            color = CyanLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound-reactive Glowing Orb
                GlowingOrb(
                    isSpeaking = false,
                    isListening = isListening,
                    rmsAmplitude = rmsAmplitude,
                    size = 110.dp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = statusMessage,
                    color = if (partialText.isNotBlank()) TextPrimary else CyanLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Try: \"TGA YouTube khol do\" • \"WhatsApp kholo\"",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (isListening) {
                                speechManager.stopListening()
                            } else {
                                onRetry()
                            }
                        },
                        containerColor = if (isListening) Color(0xFFEF4444) else CyanPrimary,
                        contentColor = DeepBackground,
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Mic toggle",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
