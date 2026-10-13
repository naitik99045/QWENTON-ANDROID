package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.ChatMessage
import ai.qwenton.apex.ui.theme.*

@Composable
fun HistoryScreen(
    messages: List<ChatMessage>,
    onClearAllHistory: () -> Unit,
    onDeleteMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Clear All History?", color = TextPrimary) },
            text = {
                Text(
                    "This permanently deletes all saved conversations and execution logs from your local Room database.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onClearAllHistory()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = CharcoalSurface
        )
    }

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
                text = "History & Audit Logs",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )

            if (messages.isNotEmpty()) {
                IconButton(onClick = { showConfirmDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = StatusRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp))
                    .background(CharcoalSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history recorded yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Surface(
                        color = CharcoalSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = msg.role.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (msg.role.name == "USER") PrimaryVioletLight else StatusGreen
                                )
                                Text(
                                    text = msg.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    maxLines = 2
                                )
                            }
                            IconButton(onClick = { onDeleteMessage(msg.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Item",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
