package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ai.qwenton.apex.domain.CommandProposal
import ai.qwenton.apex.domain.RiskLevel
import ai.qwenton.apex.ui.components.CodeDiffCard
import ai.qwenton.apex.ui.theme.*

@Composable
fun CommandApprovalDialog(
    proposal: CommandProposal,
    onApprove: (commandId: String) -> Unit,
    onReject: (commandId: String) -> Unit,
    onDismiss: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with Risk & Cryptographic Seal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Operation Authorization",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = StatusGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "HMAC-SHA256 Cryptographic Seal",
                                style = MonospaceHeader.copy(fontSize = 10.sp),
                                color = StatusGreen
                            )
                        }
                    }
                    RiskBadge(risk = proposal.risk)
                }

                Text(
                    text = proposal.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // If this is a surgical code patch, render the interactive Code Diff Card!
                if (proposal.tool == "patch_file" && proposal.arguments.size >= 3) {
                    val filePath = proposal.arguments[0]
                    val searchChunk = proposal.arguments[1]
                    val replaceChunk = proposal.arguments[2]
                    val syntheticDiff = buildString {
                        appendLine("--- a/$filePath")
                        appendLine("+++ b/$filePath")
                        searchChunk.lines().forEach { appendLine("-$it") }
                        replaceChunk.lines().forEach { appendLine("+$it") }
                    }
                    CodeDiffCard(
                        filePath = filePath,
                        diffContent = syntheticDiff
                    )
                }

                // Technical Command Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalBg)
                        .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "TOOL: ${proposal.tool}",
                        style = MonospaceHeader.copy(fontSize = 11.sp),
                        color = PrimaryVioletLight
                    )
                    Text(
                        text = "EXEC: ${proposal.program}",
                        style = MonospaceBody.copy(fontSize = 11.sp),
                        color = TerminalText
                    )
                    Text(
                        text = "ARGS: [${proposal.arguments.joinToString(", ")}]",
                        style = MonospaceBody.copy(fontSize = 10.sp),
                        color = TextPrimary
                    )
                    Text(
                        text = "CWD:  ${proposal.workingDirectory}",
                        style = MonospaceBody.copy(fontSize = 10.sp),
                        color = TextMuted
                    )
                    Text(
                        text = "HASH: ${proposal.commandId.take(16)}...",
                        style = MonospaceBody.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }

                Text(
                    text = "Single-use approval token. Execution window expires in 120 seconds.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject(proposal.commandId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StatusRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                    ) {
                        Text("Deny")
                    }

                    Button(
                        onClick = { onApprove(proposal.commandId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (proposal.risk == RiskLevel.HIGH) StatusRed else PrimaryViolet
                        )
                    ) {
                        Text("Authorize")
                    }
                }
            }
        }
    }
}
