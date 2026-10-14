package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.ConnectionStatus
import ai.qwenton.apex.ui.components.SetupWizardDialog
import androidx.compose.material.icons.filled.Terminal
import ai.qwenton.apex.ui.theme.*

@Composable
fun SettingsScreen(
    serverUrl: String,
    connectionStatus: ConnectionStatus = ConnectionStatus.Disconnected("Not connected"),
    aiProvider: String = "gemini",
    apiKey: String = "",
    activeModel: String = "gemini-1.5-pro",
    customBaseUrl: String = "",
    configSyncStatus: String? = null,
    ttsEnabled: Boolean = false,
    wakeWordEnabled: Boolean = false,
    isTermuxInstalled: Boolean = true,
    shizukuAvailable: Boolean = false,
    onSaveServerUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onSaveAiConfig: (provider: String, apiKey: String, model: String, baseUrl: String) -> Unit,
    onStartTermuxServer: () -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onToggleWakeWord: (Boolean) -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showWizardDialog by remember { mutableStateOf(false) }
    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var selectedProvider by remember(aiProvider) { mutableStateOf(aiProvider) }
    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var modelInput by remember(activeModel) { mutableStateOf(activeModel) }
    var baseUrlInput by remember(customBaseUrl) { mutableStateOf(customBaseUrl.ifBlank { "https://openrouter.ai/api/v1" }) }
    var keyVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val providers = listOf(
        "gemini" to "Google Gemini",
        "openai" to "OpenAI",
        "anthropic" to "Anthropic",
        "local" to "Local GGUF (llama.cpp)",
        "custom" to "Custom / OpenRouter"
    )

    if (showWizardDialog) {
        SetupWizardDialog(
            connectionStatus = connectionStatus,
            onTestConnection = onTestConnection,
            onSaveAiConfig = onSaveAiConfig,
            onDismiss = { showWizardDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Policy Vault",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // 1. AI Engine & Key Vault Card
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Engine & Key Vault",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "SECRETS VAULT",
                        style = MonospaceHeader.copy(fontSize = 9.sp),
                        color = PrimaryVioletLight
                    )
                }

                Text(
                    text = "Configure API keys or local offline models. Keys are stored securely in Termux and never logged.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Provider Selector Chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Active AI Provider:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        providers.take(3).forEach { (id, label) ->
                            val isSelected = selectedProvider.lowercase() == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PrimaryVioletDark else CharcoalSurfaceVariant)
                                    .border(1.dp, if (isSelected) PrimaryVioletLight else CharcoalBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedProvider = id
                                        modelInput = when (id) {
                                            "gemini" -> "gemini-1.5-pro"
                                            "openai" -> "gpt-4o"
                                            "anthropic" -> "claude-3-5-sonnet-20241022"
                                            else -> modelInput
                                        }
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.split(" ")[0],
                                    style = MonospaceHeader.copy(fontSize = 11.sp),
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        providers.drop(3).forEach { (id, label) ->
                            val isSelected = selectedProvider.lowercase() == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PrimaryVioletDark else CharcoalSurfaceVariant)
                                    .border(1.dp, if (isSelected) PrimaryVioletLight else CharcoalBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedProvider = id
                                        modelInput = if (id == "local") "llama-3.2-3b-q4" else modelInput
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MonospaceHeader.copy(fontSize = 10.sp),
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            }
                        }
                    }
                }

                // API Key Field (if not purely local)
                if (selectedProvider != "local") {
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key ($selectedProvider)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (keyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { keyVisible = !keyVisible }) {
                                Icon(
                                    imageVector = if (keyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Key Visibility",
                                    tint = TextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = CharcoalBorder
                        ),
                        singleLine = true
                    )
                }

                // Custom Base URL input (if custom provider selected)
                if (selectedProvider == "custom") {
                    OutlinedTextField(
                        value = baseUrlInput,
                        onValueChange = { baseUrlInput = it },
                        label = { Text("Base URL (e.g. OpenRouter / Groq / DeepSeek)") },
                        placeholder = { Text("https://openrouter.ai/api/v1", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = CharcoalBorder
                        ),
                        singleLine = true
                    )
                }

                // Model ID Input
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Model Identifier") },
                    placeholder = { Text(if (selectedProvider == "custom") "deepseek/deepseek-chat or gpt-4o" else "Model ID", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        onSaveAiConfig(
                            selectedProvider,
                            keyInput.trim(),
                            modelInput.trim(),
                            if (selectedProvider == "custom") baseUrlInput.trim() else ""
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save to Termux Key Vault")
                }

                if (!configSyncStatus.isNullOrBlank()) {
                    Text(
                        text = configSyncStatus,
                        style = MonospaceBody.copy(fontSize = 11.sp),
                        color = StatusGreen
                    )
                }
            }
        }

        // 2. OS Automation & Permissions Hub
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "OS Automation & Permissions Hub",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                // Shizuku Shell Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Shizuku Privileged Shell (UID 2000)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = if (shizukuAvailable) "Shizuku service active" else "Not running (start Shizuku)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (shizukuAvailable) StatusGreen else StatusAmber
                        )
                    }
                    Text(
                        text = if (shizukuAvailable) "CONNECTED" else "OFFLINE",
                        style = MonospaceHeader.copy(fontSize = 10.sp),
                        color = if (shizukuAvailable) StatusGreen else StatusAmber
                    )
                }

                Divider(color = CharcoalBorder)

                // Wireless Debugging Notification Listener
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Wireless Debugging Auto-Pairing",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Intercept pairing code notifications automatically",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    OutlinedButton(
                        onClick = onOpenNotificationSettings,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                    ) {
                        Text("Grant Access")
                    }
                }
            }
        }

        // 3. Hands-Free Voice & Wake-Word
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Voice & Wake-Word Engine",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                // TTS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Text-to-Speech (TTS)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Speak assistant responses automatically",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = ttsEnabled,
                        onCheckedChange = onToggleTts,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryVioletLight,
                            checkedTrackColor = PrimaryVioletDark,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CharcoalBorder
                        )
                    )
                }

                Divider(color = CharcoalBorder)

                // Hands-Free Wake Word
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hands-Free Wake Word ('Hey Qwenton')",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Continuously detect wake-word in background foreground service",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = wakeWordEnabled,
                        onCheckedChange = onToggleWakeWord,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryVioletLight,
                            checkedTrackColor = PrimaryVioletDark,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CharcoalBorder
                        )
                    )
                }
            }
        }

        // 4. Server Configuration Card
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Termux Loopback Address",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Server URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = CharcoalBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSaveServerUrl(urlInput.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save URL")
                    }

                    OutlinedButton(
                        onClick = onTestConnection,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletLight),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryVioletLight)
                    ) {
                        Text("Test Ping")
                    }
                }

                Divider(color = CharcoalBorder)

                Button(
                    onClick = { showWizardDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryVioletDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = PrimaryVioletLight
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Launch On-Device Setup Wizard (No PC)", color = PrimaryVioletLight)
                }
            }
        }

        // 5. Emergency Stop & Reset
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Emergency Safeguards",
                    style = MaterialTheme.typography.titleMedium,
                    color = StatusRed
                )
                Text(
                    text = "Instantly abort all pending operations, active subprocesses, and streaming requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Button(
                    onClick = onEmergencyStop,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Emergency Abort All")
                }
            }
        }
    }
}
