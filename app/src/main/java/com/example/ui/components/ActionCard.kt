package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AssistantAction
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground

@Composable
fun ActionCard(
    action: AssistantAction,
    onExecute: (AssistantAction) -> Unit,
    modifier: Modifier = Modifier
) {
    if (action == AssistantAction.None) return

    val (label, icon) = when (action) {
        is AssistantAction.LaunchApp -> action.buttonLabel to Icons.Default.PlayArrow
        is AssistantAction.PlayMedia -> action.buttonLabel to Icons.Default.PlayArrow
        is AssistantAction.SendWhatsApp -> action.buttonLabel to Icons.Default.Chat
        is AssistantAction.SendSms -> action.buttonLabel to Icons.Default.Chat
        is AssistantAction.DialPhone -> action.buttonLabel to Icons.Default.Call
        is AssistantAction.WebSearch -> action.buttonLabel to Icons.Default.Search
        is AssistantAction.SetTimer -> action.buttonLabel to Icons.Default.Timer
        is AssistantAction.OpenCamera -> "Open Camera" to Icons.Default.CameraAlt
        is AssistantAction.OpenSettings -> "Open Settings" to Icons.Default.Settings
        is AssistantAction.SaveNote -> "View Notes" to Icons.Default.Chat
        is AssistantAction.ShowSystemInfo -> "System Info" to Icons.Default.Settings
        AssistantAction.None -> return
    }

    Button(
        onClick = { onExecute(action) },
        colors = ButtonDefaults.buttonColors(
            containerColor = CyanPrimary.copy(alpha = 0.2f),
            contentColor = CyanGlow
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.6f)),
        modifier = modifier
            .padding(top = 8.dp)
            .testTag("action_execute_button")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = CyanGlow
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = CyanGlow
            )
        }
    }
}
