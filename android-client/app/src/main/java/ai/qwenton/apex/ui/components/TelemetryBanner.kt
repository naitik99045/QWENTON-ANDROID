package ai.qwenton.apex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.ConnectionStatus
import ai.qwenton.apex.ui.theme.*

@Composable
fun TelemetryBanner(
    connectionStatus: ConnectionStatus,
    activeWorkspace: String,
    activeModel: String = "Gemini 1.5",
    batteryInfo: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Loopback Connection Status Chip
        TelemetryChip(
            label = "SRV",
            value = when (connectionStatus) {
                is ConnectionStatus.Connected -> "127.0.0.1:8765 [SYNC]"
                is ConnectionStatus.Connecting -> "CONNECTING..."
                is ConnectionStatus.Disconnected -> "OFFLINE"
                is ConnectionStatus.Error -> "REFUSED"
            },
            statusColor = when (connectionStatus) {
                is ConnectionStatus.Connected -> StatusGreen
                is ConnectionStatus.Connecting -> StatusAmber
                is ConnectionStatus.Disconnected -> StatusRed
                is ConnectionStatus.Error -> StatusRed
            }
        )

        // 2. Active AI Engine Chip
        TelemetryChip(
            label = "ENGINE",
            value = activeModel,
            statusColor = PrimaryVioletLight
        )

        // 3. Workspace Directory Chip
        TelemetryChip(
            label = "WORKSPACE",
            value = activeWorkspace.ifBlank { "~/workspace" },
            statusColor = StatusCyan
        )

        // 4. Battery / Power Telemetry Chip
        if (!batteryInfo.isNullOrBlank()) {
            TelemetryChip(
                label = "POWER",
                value = batteryInfo,
                statusColor = StatusGreen
            )
        }
    }
}

@Composable
private fun TelemetryChip(
    label: String,
    value: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CharcoalSurface)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Blinking / solid indicator dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(statusColor)
            )

            Text(
                text = "$label:",
                style = MonospaceHeader.copy(fontSize = 10.sp),
                color = TextMuted
            )

            Text(
                text = value,
                style = MonospaceBody.copy(fontSize = 11.sp),
                color = TextPrimary
            )
        }
    }
}
