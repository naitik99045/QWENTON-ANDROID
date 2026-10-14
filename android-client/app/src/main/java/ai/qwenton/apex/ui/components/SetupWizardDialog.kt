package ai.qwenton.apex.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ai.qwenton.apex.domain.ConnectionStatus
import ai.qwenton.apex.service.ServerProvisioner
import ai.qwenton.apex.service.ShizukuManager
import ai.qwenton.apex.ui.theme.*

@Composable
fun SetupWizardDialog(
    connectionStatus: ConnectionStatus,
    onTestConnection: () -> Unit,
    onSaveAiConfig: (provider: String, apiKey: String, model: String, baseUrl: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    // Step 3 state
    var selectedProvider by remember { mutableStateOf("gemini") }
    var apiKey by remember { mutableStateOf("") }
    var modelName by remember { mutableStateOf("gemini-1.5-pro") }
    var customBaseUrl by remember { mutableStateOf("https://openrouter.ai/api/v1") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "On-Device Setup Wizard",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "100% Android Native • No PC / Laptop Needed",
                            style = MonospaceHeader.copy(fontSize = 10.sp),
                            color = PrimaryVioletLight
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Step Progression Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("1. Termux Server", "2. Shizuku / ADB", "3. AI Model").forEachIndexed { idx, label ->
                        val stepNum = idx + 1
                        val isActive = currentStep == stepNum
                        val isDone = currentStep > stepNum
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isActive) PrimaryVioletDark else if (isDone) CharcoalSurfaceVariant else DarkCharcoalBg)
                                .border(1.dp, if (isActive) PrimaryVioletLight else CharcoalBorder, RoundedCornerShape(6.dp))
                                .clickable { currentStep = stepNum }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MonospaceHeader.copy(fontSize = 10.sp),
                                color = if (isActive || isDone) TextPrimary else TextMuted
                            )
                        }
                    }
                }

                // Step 1: Termux Local Server
                if (currentStep == 1) {
                    Text(
                        text = "Step 1: Start Termux Local Server",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Qwenton runs a local FastAPI engine on 127.0.0.1:8765. Copy this command and paste it inside the Termux app on your phone:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    // Monospace Command Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TerminalBg)
                            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "# 1-Line Termux Setup & Launch:",
                            style = MonospaceHeader.copy(fontSize = 10.sp),
                            color = PrimaryVioletLight
                        )
                        Text(
                            text = ServerProvisioner.ONE_LINE_SETUP_COMMAND,
                            style = MonospaceBody.copy(fontSize = 10.sp),
                            color = TerminalText,
                            maxLines = 4
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ServerProvisioner.exportServerPayload(context)
                                ServerProvisioner.copyToClipboard(context, ServerProvisioner.ONE_LINE_SETUP_COMMAND)
                                Toast.makeText(context, "Server files exported & command copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1-Tap Copy & Setup", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val res = ServerProvisioner.launchTermuxApp(context)
                                if (res.isFailure) {
                                    Toast.makeText(context, res.exceptionOrNull()?.localizedMessage, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletLight),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryVioletLight),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open Termux", fontSize = 12.sp)
                        }
                    }

                    // Connection status verification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (connectionStatus) {
                                is ConnectionStatus.Connected -> "Server: CONNECTED ✅"
                                is ConnectionStatus.Connecting -> "Server: CHECKING..."
                                else -> "Server: WAITING FOR START"
                            },
                            style = MonospaceHeader.copy(fontSize = 11.sp),
                            color = if (connectionStatus is ConnectionStatus.Connected) StatusGreen else StatusAmber
                        )
                        TextButton(onClick = onTestConnection) {
                            Text("Check Ping", color = PrimaryVioletLight, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = { currentStep = 2 },
                        colors = ButtonDefaults.buttonColors(containerColor = CharcoalSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Next: Setup Shizuku / Wireless ADB →", color = TextPrimary)
                    }
                }

                // Step 2: Shizuku / Wireless ADB
                if (currentStep == 2) {
                    Text(
                        text = "Step 2: Connect Shizuku & Wireless ADB",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Shizuku enables rootless shell permissions (screen taps, app launching, terminal commands) using Android's built-in Wireless Debugging:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    // Sub-steps list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("A. Install / Open Shizuku", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text("Open Shizuku app or get it from Play Store", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Button(
                                onClick = { ServerProvisioner.launchOrInstallShizuku(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryVioletDark),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Open Shizuku", fontSize = 11.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("B. Grant Notification Access", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text("Enables Qwenton to auto-intercept pairing codes", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            OutlinedButton(
                                onClick = { ServerProvisioner.openNotificationListenerSettings(context) },
                                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Grant", fontSize = 11.sp, color = PrimaryVioletLight)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("C. Enable Wireless Debugging", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                Text("Go to Developer Options > Wireless Debugging", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Button(
                                onClick = { ServerProvisioner.openDeveloperOptions(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Dev Options", fontSize = 11.sp)
                            }
                        }
                    }

                    val isShizukuActive = ShizukuManager.isShizukuAvailable()
                    Text(
                        text = if (isShizukuActive) "Shizuku: RUNNING & CONNECTED ✅" else "Shizuku: Not yet paired (Turn on Wireless Debugging)",
                        style = MonospaceHeader.copy(fontSize = 11.sp),
                        color = if (isShizukuActive) StatusGreen else StatusAmber
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            modifier = Modifier.weight(1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder)
                        ) {
                            Text("← Back", color = TextMuted)
                        }
                        Button(
                            onClick = { currentStep = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Next: AI Keys →")
                        }
                    }
                }

                // Step 3: AI Model & Key Vault
                if (currentStep == 3) {
                    Text(
                        text = "Step 3: Connect AI Model",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                    Text(
                        text = "Enter your API key. (For 100% free usage, Google Gemini provides a free tier at ai.google.dev):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    // Provider Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            "gemini" to "Gemini",
                            "openai" to "OpenAI",
                            "custom" to "Custom",
                            "local" to "Local"
                        ).forEach { (id, label) ->
                            val isSelected = selectedProvider == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) PrimaryVioletDark else CharcoalSurfaceVariant)
                                    .border(1.dp, if (isSelected) PrimaryVioletLight else CharcoalBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedProvider = id
                                        modelName = when (id) {
                                            "gemini" -> "gemini-1.5-pro"
                                            "openai" -> "gpt-4o"
                                            "custom" -> "deepseek/deepseek-chat"
                                            else -> "llama-3.2-3b-q4"
                                        }
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

                    // Custom Base URL input (for OpenRouter, Groq, DeepSeek, Together, etc.)
                    if (selectedProvider == "custom") {
                        OutlinedTextField(
                            value = customBaseUrl,
                            onValueChange = { customBaseUrl = it },
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

                        OutlinedTextField(
                            value = modelName,
                            onValueChange = { modelName = it },
                            label = { Text("Model Name") },
                            placeholder = { Text("deepseek/deepseek-chat or gpt-4o", color = TextMuted) },
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

                    if (selectedProvider != "local") {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("API Key ($selectedProvider)") },
                            placeholder = { Text(if (selectedProvider == "custom") "sk-or-v1-... or Bearer Token" else "API Key", color = TextMuted) },
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

                    Button(
                        onClick = {
                            onSaveAiConfig(
                                selectedProvider,
                                apiKey.trim(),
                                modelName.trim(),
                                if (selectedProvider == "custom") customBaseUrl.trim() else ""
                            )
                            Toast.makeText(context, "Setup Complete! Welcome to Qwenton Apex.", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Finish Setup & Enter Qwenton HUD 🚀")
                    }
                }
            }
        }
    }
}
