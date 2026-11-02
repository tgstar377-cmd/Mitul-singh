package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AssistantViewModel
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.RoseError
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val isVoiceMuted by viewModel.isVoiceMuted.collectAsState()
    val speechRate by viewModel.speechRate.collectAsState()

    var editingName by remember { mutableStateOf(userName) }
    var currentSpeed by remember { mutableFloatStateOf(speechRate) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val sharedAppUrl = "https://ais-pre-4lb7emuiojo2m2alh4rlgc-23744220967.asia-northeast1.run.app"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Branding Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_tg_assistant),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "TG AI Assistant",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Specially configured for $userName",
                        color = CyanLight,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Version 1.0 • Bilingual AI Companion",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // User Customization Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Profile",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = editingName,
                    onValueChange = {
                        editingName = it
                        viewModel.setUserName(it)
                    },
                    label = { Text("Your Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanGlow,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Voice Controls Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, SurfaceCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice & Speech Settings",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Voice Response", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "Speak answers out loud in Hindi & English", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = !isVoiceMuted,
                        onCheckedChange = { viewModel.toggleVoiceMute() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanGlow,
                            checkedTrackColor = CyanPrimary.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val isMaleVoice by viewModel.isMaleVoice.collectAsState()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Ladke Ki Awaaz (Male Voice)", color = CyanLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Deep & confident young male tone for $userName", color = TextMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = isMaleVoice,
                        onCheckedChange = { viewModel.setMaleVoice(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanGlow,
                            checkedTrackColor = CyanPrimary.copy(alpha = 0.4f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Speech Speed: ${String.format(java.util.Locale.US, "%.2f", currentSpeed)}x",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Slider(
                    value = currentSpeed,
                    onValueChange = {
                        currentSpeed = it
                        viewModel.setSpeechRate(it)
                    },
                    valueRange = 0.7f..1.4f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanGlow,
                        activeTrackColor = CyanPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Voice Selection Studio (आवाज़ सुनकर सेलेक्ट करें):",
                    color = CyanLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap 'Play Sample' to hear each voice speak, then tap 'Select' to set your favorite!",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                val selectedProfileId by viewModel.selectedVoiceProfileId.collectAsState()

                viewModel.availableVoiceProfiles.forEach { profile ->
                    val isSelected = profile.id == selectedProfileId
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else DeepBackground
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) CyanGlow else SurfaceCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = profile.name,
                                    color = if (isSelected) CyanGlow else TextPrimary,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profile.badge,
                                    color = if (isSelected) CyanLight else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = profile.subtitle,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.previewVoice(profile) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
                                    border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Play voice",
                                        tint = CyanGlow,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Play Sample (सुनें)", color = CyanGlow, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.selectVoiceProfile(profile.id)
                                        Toast.makeText(context, "Voice updated: ${profile.name}", Toast.LENGTH_SHORT).show()
                                        viewModel.voiceManager.speak("Haan Mitul bhai! Ab main isi aawaz mein aapse baat karunga.", "hi")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) CyanPrimary else SurfaceCard
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) CyanGlow else SurfaceCardBorder),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (isSelected) "Active ✓ (सक्रिय)" else "Select (यह चुनें)",
                                        color = if (isSelected) DeepBackground else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // APK Download & Installation Guide Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download App & APK",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "App Download Karne Ka Tareeka (Instructions for $userName):",
                    color = CyanLight,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "1. Upar right side AI Studio Settings/Export menu par tap karein.\n" +
                            "2. 'Download APK' ya 'Export as ZIP' select karein.\n" +
                            "3. Apne mobile mein download hui APK file install karein (Unknown sources allow karein).\n" +
                            "4. Ya niche diye gaye live web preview link se bhi run kar sakte hain:",
                    color = TextSecondary,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepBackground, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = sharedAppUrl,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("TG Assistant Link", sharedAppUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy link",
                            tint = CyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Clear Chat History Button
        Button(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.buttonColors(containerColor = RoseError.copy(alpha = 0.15f)),
            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("clear_history_button")
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = RoseError,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Clear Chat History", color = RoseError, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = { Text("Clear All Messages?", color = TextPrimary) },
                text = { Text("This will permanently clear conversation history with TG Assistant.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearChatHistory()
                            showClearConfirm = false
                            Toast.makeText(context, "Chat cleared.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                    ) {
                        Text("Clear", color = TextPrimary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearConfirm = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceCard
            )
        }
    }
}
