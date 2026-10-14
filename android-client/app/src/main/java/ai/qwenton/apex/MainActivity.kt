package ai.qwenton.apex

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.qwenton.apex.domain.*
import ai.qwenton.apex.service.TermuxServiceHelper
import ai.qwenton.apex.ui.*
import ai.qwenton.apex.ui.theme.*
import ai.qwenton.apex.voice.SpeechRecognizerManager
import ai.qwenton.apex.voice.TextToSpeechManager

sealed class NavRoute(val route: String, val title: String, val icon: ImageVector) {
    object Chat : NavRoute("chat", "Chat", Icons.Default.Chat)
    object Terminal : NavRoute("terminal", "Terminal", Icons.Default.Terminal)
    object Skills : NavRoute("skills", "Skills", Icons.Default.Build)
    object History : NavRoute("history", "History", Icons.Default.History)
    object Settings : NavRoute("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private var speechRecognizerManager: SpeechRecognizerManager? = null
    private var textToSpeechManager: TextToSpeechManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        textToSpeechManager = TextToSpeechManager(this)

        setContent {
            QwentonApexTheme {
                MainAppScaffold(
                    speechRecognizerManager = speechRecognizerManager,
                    textToSpeechManager = textToSpeechManager,
                    onInitSpeech = { onSpeechResult, onSpeechError ->
                        speechRecognizerManager = SpeechRecognizerManager(
                            context = this,
                            onResult = onSpeechResult,
                            onError = onSpeechError
                        )
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizerManager?.destroy()
        textToSpeechManager?.shutdown()
    }
}

@Composable
fun MainAppScaffold(
    speechRecognizerManager: SpeechRecognizerManager?,
    textToSpeechManager: TextToSpeechManager?,
    onInitSpeech: (onResult: (String) -> Unit, onError: (String) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val viewModel: QwentonViewModel = viewModel(factory = QwentonViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var isListening by remember { mutableStateOf(false) }

    // Audio recording permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onInitSpeech(
                { text ->
                    isListening = false
                    viewModel.sendMessage(text)
                },
                { err ->
                    isListening = false
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
            speechRecognizerManager?.startListening()
            isListening = true
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands.", Toast.LENGTH_LONG).show()
        }
    }

    // Speak incoming assistant responses if TTS is enabled
    LaunchedEffect(uiState.messages.size) {
        val lastMsg = uiState.messages.lastOrNull()
        if (uiState.ttsEnabled && lastMsg != null && lastMsg.role == MessageRole.ASSISTANT && !lastMsg.isStreaming) {
            textToSpeechManager?.speak(lastMsg.content)
        }
    }

    val navItems = listOf(
        NavRoute.Chat,
        NavRoute.Terminal,
        NavRoute.Skills,
        NavRoute.History,
        NavRoute.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkCharcoalBg,
        bottomBar = {
            NavigationBar(
                containerColor = CharcoalSurface,
                tonalElevation = 4.dp
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(NavRoute.Chat.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (selected) PrimaryVioletLight else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                color = if (selected) PrimaryVioletLight else TextMuted,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CharcoalSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Chat.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Chat.route) {
                ChatScreen(
                    connectionStatus = uiState.connectionStatus,
                    messages = uiState.messages,
                    isStreaming = uiState.isStreaming,
                    pendingProposal = uiState.pendingProposal,
                    serverUrl = uiState.serverUrl,
                    activeWorkspace = uiState.activeWorkspace,
                    activeModel = uiState.activeModel,
                    batteryInfo = uiState.batteryInfo,
                    onSendMessage = { text -> viewModel.sendMessage(text) },
                    onStopRequest = {
                        viewModel.stopRequest()
                        textToSpeechManager?.stop()
                    },
                    onMicClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (speechRecognizerManager == null) {
                                onInitSpeech(
                                    { text ->
                                        isListening = false
                                        viewModel.sendMessage(text)
                                    },
                                    { err ->
                                        isListening = false
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            if (isListening) {
                                speechRecognizerManager?.stopListening()
                                isListening = false
                            } else {
                                speechRecognizerManager?.startListening()
                                isListening = true
                                Toast.makeText(context, "Listening...", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onApproveCommand = { cmdId -> viewModel.approveCommand(cmdId) },
                    onRejectCommand = { cmdId -> viewModel.rejectCommand(cmdId) },
                    onRetryConnection = { viewModel.testConnection() },
                    onSelectWorkspace = { path -> viewModel.setWorkspace(path) }
                )
            }

            composable(NavRoute.Terminal.route) {
                TerminalOutputScreen(
                    execution = uiState.activeExecution,
                    onCancelExecution = { cmdId -> viewModel.rejectCommand(cmdId) }
                )
            }

            composable(NavRoute.Skills.route) {
                SkillsScreen(
                    skills = uiState.skills,
                    onToggleSkill = { id, enabled -> viewModel.toggleSkill(id, enabled) }
                )
            }

            composable(NavRoute.History.route) {
                HistoryScreen(
                    messages = uiState.messages,
                    onClearAllHistory = { viewModel.clearHistory() },
                    onDeleteMessage = { id -> viewModel.deleteMessage(id) }
                )
            }

            composable(NavRoute.Settings.route) {
                SettingsScreen(
                    serverUrl = uiState.serverUrl,
                    aiProvider = uiState.aiProvider,
                    apiKey = uiState.apiKey,
                    activeModel = uiState.activeModel,
                    configSyncStatus = uiState.configSyncStatus,
                    onSaveServerUrl = { url -> viewModel.setServerUrl(url) },
                    onTestConnection = { viewModel.testConnection() },
                    onSaveAiConfig = { provider, key, model ->
                        viewModel.setAiConfig(provider, key, model)
                        Toast.makeText(context, "Saving AI config to Termux .env...", Toast.LENGTH_SHORT).show()
                    },
                    onStartTermuxServer = {
                        val result = TermuxServiceHelper.startApexServer(context)
                        if (result.isSuccess) {
                            Toast.makeText(context, "Dispatched RUN_COMMAND to Termux.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    isTermuxInstalled = TermuxServiceHelper.isTermuxInstalled(context),
                    ttsEnabled = uiState.ttsEnabled,
                    onToggleTts = { enabled ->
                        viewModel.toggleTts(enabled)
                        if (!enabled) textToSpeechManager?.stop()
                    },
                    onEmergencyStop = {
                        viewModel.stopRequest()
                        textToSpeechManager?.stop()
                    }
                )
            }
        }
    }
}
