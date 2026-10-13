package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.TerminalExecution
import ai.qwenton.apex.ui.theme.*

@Composable
fun TerminalOutputScreen(
    execution: TerminalExecution?,
    onCancelExecution: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terminal Output",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            if (execution != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val combined = "Command: ${execution.program} ${execution.arguments.joinToString(" ")}\n\nStdout:\n${execution.stdout}\n\nStderr:\n${execution.stderr}"
                            clipboardManager.setText(AnnotatedString(combined))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Output",
                            tint = PrimaryVioletLight
                        )
                    }

                    if (execution.isRunning) {
                        IconButton(onClick = { onCancelExecution(execution.commandId) }) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Cancel Execution",
                                tint = StatusRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (execution == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                    .background(TerminalBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No terminal commands executed yet.",
                    style = MonospaceBody,
                    color = TextMuted
                )
            }
        } else {
            // Execution Status Bar
            Surface(
                color = CharcoalSurface,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${execution.program} ${execution.arguments.joinToString(" ")}",
                        style = MonospaceHeader,
                        color = TerminalText
                    )

                    val statusLabel = when {
                        execution.isRunning -> "RUNNING"
                        execution.exitCode == 0 -> "SUCCESS (0)"
                        else -> "EXIT (${execution.exitCode ?: -1})"
                    }
                    val statusColor = when {
                        execution.isRunning -> StatusAmber
                        execution.exitCode == 0 -> StatusGreen
                        else -> StatusRed
                    }

                    Text(
                        text = statusLabel,
                        style = MonospaceHeader,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Output Terminal Window
            Surface(
                color = TerminalBg,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (execution.stdout.isNotBlank()) {
                        Text(
                            text = execution.stdout,
                            style = MonospaceBody,
                            color = TerminalText
                        )
                    }
                    if (execution.stderr.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = execution.stderr,
                            style = MonospaceBody,
                            color = TerminalError
                        )
                    }
                    if (execution.stdout.isBlank() && execution.stderr.isBlank()) {
                        Text(
                            text = if (execution.isRunning) "Waiting for output..." else "[Process completed with no output]",
                            style = MonospaceBody,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Stats footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ID: ${execution.commandId}",
                    style = MonospaceBody,
                    color = TextMuted,
                    fontSize = 11.sp
                )
                execution.durationMs?.let { ms ->
                    Text(
                        text = "Duration: ${ms}ms",
                        style = MonospaceBody,
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
