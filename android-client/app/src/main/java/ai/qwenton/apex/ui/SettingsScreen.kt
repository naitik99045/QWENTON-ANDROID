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
import ai.qwenton.apex.ui.theme.*

@Composable
fun SettingsScreen(
    serverUrl: String,
    aiProvider: String = "gemini",
    apiKey: String = "",
    activeModel: String = "gemini-1.5-pro",
    configSyncStatus: String? = null,
    onSaveServerUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onSaveAiConfig: (provider: String, apiKey: String, model: String) -> Unit,
    onStartTermuxServer: () -> Unit,
    isTermuxInstalled: Boolean,
    ttsEnabled: Boolean,
    onToggleTts: (Boolean) -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    var selectedProvider by remember(aiProvider) { mutableStateOf(aiProvider) }
    var keyInput by remember(apiKey) { mutableStateOf(apiKey) }
    var modelInput by remember(activeModel) { mutableStateOf(activeModel) }
    var keyVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val providers = listOf(
        "gemini" to "Google Gemini",
        "openai" to "OpenAI",
        "anthropic" to "Anthropic",
        "local" to "Local GGUF (llama.cpp)",
        "custom" to "Custom / OpenRouter"
    )

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

                // Model ID Input
                OutlinedTextField(
                    value = modelInput,
                    onValueChange = { modelInput = it },
                    label = { Text("Model Identifier") },
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
                        onSaveAiConfig(selectedProvider, keyInput.trim(), modelInput.trim())
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

        // 2. Server Configuration Card
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
            }
        }

        // 3. Termux Service & RUN_COMMAND Integration
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
                    text = "Termux Service Management",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = if (isTermuxInstalled) {
                        "Termux package detected. You can launch or restart the background local server using the official RUN_COMMAND intent."
                    } else {
                        "Termux is not detected. Please install Termux from F-Droid to enable local tool execution."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isTermuxInstalled) TextSecondary else StatusAmber
                )

                Button(
                    onClick = onStartTermuxServer,
                    enabled = isTermuxInstalled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryVioletDark,
                        disabledContainerColor = CharcoalBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Server in Termux (RUN_COMMAND)")
                }
            }
        }

        // 4. Voice Configuration Card
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Voice Synthesis",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

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
