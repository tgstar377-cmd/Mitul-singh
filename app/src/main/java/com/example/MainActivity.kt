package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AssistantTab
import com.example.ui.AssistantViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CommandsScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            com.example.service.AssistantForegroundService.startService(this)
        } catch (e: Exception) {
            // Android 14+ background start check
        }

        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: AssistantViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // Handle back button: if on sub-tab, return to Chat
    BackHandler(enabled = currentTab != AssistantTab.CHAT) {
        viewModel.setTab(AssistantTab.CHAT)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                contentColor = TextPrimary,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == AssistantTab.CHAT,
                    onClick = { viewModel.setTab(AssistantTab.CHAT) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AssistantTab.CHAT) Icons.Filled.Chat else Icons.Outlined.Chat,
                            contentDescription = "Assistant"
                        )
                    },
                    label = { Text("Assistant", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBackground,
                        selectedTextColor = CyanGlow,
                        indicatorColor = CyanGlow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_chat")
                )

                NavigationBarItem(
                    selected = currentTab == AssistantTab.COMMANDS,
                    onClick = { viewModel.setTab(AssistantTab.COMMANDS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AssistantTab.COMMANDS) Icons.Filled.GridView else Icons.Outlined.GridView,
                            contentDescription = "Actions"
                        )
                    },
                    label = { Text("Actions", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBackground,
                        selectedTextColor = CyanGlow,
                        indicatorColor = CyanGlow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_commands")
                )

                NavigationBarItem(
                    selected = currentTab == AssistantTab.NOTES,
                    onClick = { viewModel.setTab(AssistantTab.NOTES) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AssistantTab.NOTES) Icons.Filled.Description else Icons.Outlined.Description,
                            contentDescription = "Notes"
                        )
                    },
                    label = { Text("Notes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBackground,
                        selectedTextColor = CyanGlow,
                        indicatorColor = CyanGlow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_notes")
                )

                NavigationBarItem(
                    selected = currentTab == AssistantTab.SETTINGS,
                    onClick = { viewModel.setTab(AssistantTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AssistantTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepBackground,
                        selectedTextColor = CyanGlow,
                        indicatorColor = CyanGlow,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AssistantTab.CHAT -> ChatScreen(viewModel = viewModel)
                AssistantTab.COMMANDS -> CommandsScreen(viewModel = viewModel)
                AssistantTab.NOTES -> NotesScreen(viewModel = viewModel)
                AssistantTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
