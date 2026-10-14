package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.*
import ai.qwenton.apex.ui.components.*
import ai.qwenton.apex.ui.theme.*

@Composable
fun ChatScreen(
    connectionStatus: ConnectionStatus,
    messages: List<ChatMessage>,
    isStreaming: Boolean,
    pendingProposal: CommandProposal?,
    serverUrl: String,
    activeWorkspace: String = "~/workspace",
    activeModel: String = "Gemini 1.5",
    batteryInfo: String? = null,
    onSendMessage: (String) -> Unit,
    onStopRequest: () -> Unit,
    onMicClick: () -> Unit,
    onApproveCommand: (String) -> Unit,
    onRejectCommand: (String) -> Unit,
    onRetryConnection: () -> Unit,
    onSelectWorkspace: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    var showWorkspaceDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Scroll to bottom on new messages or stream updates
    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Modal workspace folder selection dialog
    if (showWorkspaceDialog) {
        WorkspaceDialog(
            currentWorkspace = activeWorkspace,
            onSelectWorkspace = { path ->
                onSelectWorkspace(path)
                onSendMessage("Set active workspace to: $path")
            },
            onDismiss = { showWorkspaceDialog = false }
        )
    }

    // Cryptographic Approval Dialog with HMAC Verification
    if (pendingProposal != null) {
        CommandApprovalDialog(
            proposal = pendingProposal,
            onApprove = onApproveCommand,
            onReject = onRejectCommand,
            onDismiss = { onRejectCommand(pendingProposal.commandId) }
        )
    }

    // Determine Reactor Core State
    val reactorState = when {
        pendingProposal != null -> ReactorState.APPROVAL_REQUIRED
        isStreaming -> ReactorState.THINKING
        connectionStatus is ConnectionStatus.Connected -> ReactorState.IDLE
        else -> ReactorState.IDLE
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // 1. Futuristic Header HUD: Reactor Core + Telemetry Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Reactor Core Orb
            QwentonReactorCore(
                state = reactorState,
                size = 90.dp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Live Real-Time Telemetry Bar
            TelemetryBanner(
                connectionStatus = connectionStatus,
                activeWorkspace = activeWorkspace,
                activeModel = activeModel,
                batteryInfo = batteryInfo,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Quick Command Action Rail
            QuickCommandRail(
                onActionClick = { action ->
                    if (action.id == "workspace") {
                        showWorkspaceDialog = true
                    } else {
                        onSendMessage(action.prompt)
                    }
                },
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // 2. Message Thread
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = CharcoalSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "QWENTON APEX OPERATING SYSTEM",
                            style = MonospaceHeader.copy(fontSize = 13.sp),
                            color = PrimaryVioletLight
                        )
                        Text(
                            text = "Air-gapped on-device AI operations center. Autonomous coding, SAST security audits, and Shizuku/ADB automation ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageBubble(message = message)
                }
            }
        }

        // 3. Cybernetic Composer Input
        Surface(
            color = CharcoalSurface,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.size(38.dp)
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
                        Text("Instruct Qwenton...", color = TextMuted, fontSize = 13.sp)
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
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Abort Stream",
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
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Execute Prompt",
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
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 14.dp
            ),
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder) else null,
            modifier = Modifier.widthIn(max = 330.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = TextPrimary
                )

                if (message.isStreaming) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        color = PrimaryVioletLight,
                        trackColor = CharcoalBorder
                    )
                }

                if (message.toolProposal != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = TerminalBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "ACTION: ${message.toolProposal.tool}",
                                style = MonospaceHeader.copy(fontSize = 11.sp),
                                color = PrimaryVioletLight
                            )
                            Text(
                                text = message.toolProposal.explanation,
                                style = MonospaceBody.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
