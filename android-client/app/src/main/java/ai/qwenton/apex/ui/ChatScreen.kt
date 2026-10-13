package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.*
import ai.qwenton.apex.ui.theme.*

@Composable
fun ChatScreen(
    connectionStatus: ConnectionStatus,
    messages: List<ChatMessage>,
    isStreaming: Boolean,
    pendingProposal: CommandProposal?,
    serverUrl: String,
    onSendMessage: (String) -> Unit,
    onStopRequest: () -> Unit,
    onMicClick: () -> Unit,
    onApproveCommand: (String) -> Unit,
    onRejectCommand: (String) -> Unit,
    onRetryConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Modal approval dialog if there is a pending proposal
    if (pendingProposal != null) {
        CommandApprovalDialog(
            proposal = pendingProposal,
            onApprove = onApproveCommand,
            onReject = onRejectCommand,
            onDismiss = { onRejectCommand(pendingProposal.commandId) }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        // Top Connection Status Banner
        ConnectionStatusBar(
            status = connectionStatus,
            serverUrl = serverUrl,
            onRetryClick = onRetryConnection,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Message Thread
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Qwenton Apex",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Your private on-device assistant. Ask a question or request device automation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
            }
        }

        // Bottom Input Composer
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Push to Talk",
                        tint = PrimaryVioletLight
                    )
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Type a prompt...", color = TextMuted, fontSize = 14.sp)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    maxLines = 4
                )

                if (isStreaming) {
                    IconButton(
                        onClick = onStopRequest,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Generation",
                            tint = StatusRed
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) PrimaryViolet else TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) PrimaryVioletDark else CharcoalSurfaceVariant
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                if (message.isStreaming) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = PrimaryVioletLight,
                        trackColor = CharcoalBorder
                    )
                }

                if (message.toolProposal != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = TerminalBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Action: ${message.toolProposal.tool}",
                                style = MonospaceHeader,
                                color = PrimaryVioletLight
                            )
                            Text(
                                text = message.toolProposal.explanation,
                                style = MonospaceBody,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
