package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AssistantAction
import com.example.ui.AssistantTab
import com.example.ui.AssistantViewModel
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

data class CommandItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val action: AssistantAction? = null,
    val promptText: String? = null
)

@Composable
fun CommandsScreen(
    viewModel: AssistantViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val commandList = listOf(
        CommandItem(
            title = "YouTube",
            description = "Watch videos & music",
            icon = Icons.Default.PlayArrow,
            accentColor = Color(0xFFEF4444),
            action = AssistantAction.LaunchApp("YouTube", "com.google.android.youtube", "https://youtube.com")
        ),
        CommandItem(
            title = "WhatsApp",
            description = "Chat & message friends",
            icon = Icons.Default.Chat,
            accentColor = EmeraldSuccess,
            action = AssistantAction.LaunchApp("WhatsApp", "com.whatsapp", "https://api.whatsapp.com")
        ),
        CommandItem(
            title = "Camera",
            description = "Snap photos instantly",
            icon = Icons.Default.CameraAlt,
            accentColor = CyanGlow,
            action = AssistantAction.OpenCamera
        ),
        CommandItem(
            title = "Phone Dialer",
            description = "Call numbers & contacts",
            icon = Icons.Default.Call,
            accentColor = EmeraldSuccess,
            action = AssistantAction.DialPhone()
        ),
        CommandItem(
            title = "Google Maps",
            description = "Navigation & directions",
            icon = Icons.Default.Map,
            accentColor = ElectricIndigo,
            action = AssistantAction.LaunchApp("Google Maps", "com.google.android.apps.maps", "https://maps.google.com")
        ),
        CommandItem(
            title = "Clock & Timer",
            description = "Set alarms & stopwatch",
            icon = Icons.Default.Alarm,
            accentColor = VioletAccent,
            action = AssistantAction.SetTimer(5)
        ),
        CommandItem(
            title = "Device Settings",
            description = "Wi-Fi, Bluetooth & Display",
            icon = Icons.Default.Settings,
            accentColor = CyanPrimary,
            action = AssistantAction.OpenSettings
        ),
        CommandItem(
            title = "Web Search",
            description = "Search Google instantly",
            icon = Icons.Default.Search,
            accentColor = CyanLight,
            action = AssistantAction.WebSearch("Latest tech news")
        ),
        CommandItem(
            title = "Hindi Chutkula",
            description = "Hear a funny Hindi joke",
            icon = Icons.Default.SentimentSatisfiedAlt,
            accentColor = Color(0xFFF59E0B),
            promptText = "Ek mazedaar Hindi joke sunao"
        ),
        CommandItem(
            title = "Daily Shayari",
            description = "Inspirational quote & shayari",
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFFEC4899),
            promptText = "Ek shaandaar shayari sunao"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Smart Actions Hub",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Tap to trigger phone operations and assistant tasks",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(commandList) { item ->
                CommandCard(
                    item = item,
                    onClick = {
                        if (item.action != null) {
                            viewModel.executeAction(context, item.action)
                        } else if (item.promptText != null) {
                            viewModel.sendTextMessage(item.promptText, context)
                            viewModel.setTab(AssistantTab.CHAT)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CommandCard(
    item: CommandItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("command_card_${item.title.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(item.accentColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.description,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 2
            )
        }
    }
}
