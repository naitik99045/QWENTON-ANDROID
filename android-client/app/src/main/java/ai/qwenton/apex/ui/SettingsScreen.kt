package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.ui.theme.*

@Composable
fun SettingsScreen(
    serverUrl: String,
    onSaveServerUrl: (String) -> Unit,
    onTestConnection: () -> Unit,
    onStartTermuxServer: () -> Unit,
    isTermuxInstalled: Boolean,
    ttsEnabled: Boolean,
    onToggleTts: (Boolean) -> Unit,
    onEmergencyStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember(serverUrl) { mutableStateOf(serverUrl) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(14.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings & Policy",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )

        // Server Configuration Card
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
                    text = "Termux Server Connection",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Default local loopback address: http://127.0.0.1:8765",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
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
                        Text("Test Connect")
                    }
                }
            }
        }

        // Termux Automation & RUN_COMMAND Integration
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
                    style = MaterialTheme.typography.titleLarge,
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

        // Voice Configuration Card
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
                    text = "Voice Settings",
                    style = MaterialTheme.typography.titleLarge,
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

        // Emergency Stop & Reset
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
                    text = "Emergency Controls",
                    style = MaterialTheme.typography.titleLarge,
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
                    Text("Emergency Stop All")
                }
            }
        }
    }
}
