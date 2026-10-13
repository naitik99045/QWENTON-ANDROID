package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.ConnectionStatus
import ai.qwenton.apex.domain.RiskLevel
import ai.qwenton.apex.ui.theme.*

@Composable
fun ConnectionStatusBar(
    status: ConnectionStatus,
    serverUrl: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (status) {
        is ConnectionStatus.Connected -> StatusGreen to "Connected (127.0.0.1:8765)"
        is ConnectionStatus.Connecting -> StatusAmber to "Connecting..."
        is ConnectionStatus.Disconnected -> StatusRed to "Offline: ${status.reason}"
        is ConnectionStatus.Error -> StatusRed to "Error: ${status.message}"
    }

    Surface(
        color = CharcoalSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CharcoalBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (status !is ConnectionStatus.Connected) {
                TextButton(
                    onClick = onRetryClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Retry", color = PrimaryViolet, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RiskBadge(risk: RiskLevel) {
    val (badgeBg, badgeText) = when (risk) {
        RiskLevel.SAFE -> StatusGreen.copy(alpha = 0.15f) to StatusGreen
        RiskLevel.MEDIUM -> StatusAmber.copy(alpha = 0.15f) to StatusAmber
        RiskLevel.HIGH -> StatusRed.copy(alpha = 0.15f) to StatusRed
        RiskLevel.BLOCKED -> Color.Gray.copy(alpha = 0.2f) to Color.LightGray
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeText.copy(alpha = 0.4f))
    ) {
        Text(
            text = risk.label.uppercase(),
            color = badgeText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
