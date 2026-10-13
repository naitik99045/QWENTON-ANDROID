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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ai.qwenton.apex.domain.CommandProposal
import ai.qwenton.apex.domain.RiskLevel
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Command Approval Required",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    RiskBadge(risk = proposal.risk)
                }

                Text(
                    text = proposal.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                // Technical Command Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerminalBg)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Tool: ${proposal.tool}",
                        style = MonospaceHeader,
                        color = PrimaryVioletLight
                    )
                    Text(
                        text = "Executable: ${proposal.program}",
                        style = MonospaceBody,
                        color = TerminalText
                    )
                    Text(
                        text = "Arguments: ${proposal.arguments.joinToString(" ")}",
                        style = MonospaceBody,
                        color = TextPrimary
                    )
                    Text(
                        text = "Working Dir: ${proposal.workingDirectory}",
                        style = MonospaceBody,
                        color = TextMuted
                    )
                }

                Text(
                    text = "This action will execute locally inside your Termux environment with your current user privileges.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
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
                        Text("Reject")
                    }

                    Button(
                        onClick = { onApprove(proposal.commandId) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (proposal.risk == RiskLevel.HIGH) StatusRed else PrimaryViolet
                        )
                    ) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}
