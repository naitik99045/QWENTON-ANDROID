package ai.qwenton.apex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.ui.theme.*

data class QuickAction(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val prompt: String
)

@Composable
fun QuickCommandRail(
    onActionClick: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val actions = listOf(
        QuickAction(
            id = "wizard",
            title = "Setup Wizard (No PC)",
            icon = Icons.Default.RocketLaunch,
            prompt = "Open on-device setup wizard"
        ),
        QuickAction(
            id = "workspace",
            title = "Select Folder",
            icon = Icons.Default.FolderOpen,
            prompt = "Select workspace directory"
        ),
        QuickAction(
            id = "pentest",
            title = "Run Pentest",
            icon = Icons.Default.Security,
            prompt = "Run full security pentest and SAST audit on current workspace"
        ),
        QuickAction(
            id = "adb",
            title = "ADB Device Status",
            icon = Icons.Default.PhoneAndroid,
            prompt = "Inspect device battery, running packages, and ADB shell status"
        ),
        QuickAction(
            id = "git",
            title = "Git Status",
            icon = Icons.Default.Code,
            prompt = "Check git status and modified files in current workspace"
        ),
        QuickAction(
            id = "battery",
            title = "Battery Telemetry",
            icon = Icons.Default.BatteryChargingFull,
            prompt = "Check battery status"
        )
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            QuickActionChip(
                action = action,
                onClick = { onActionClick(action) }
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    action: QuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CharcoalSurfaceVariant)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = PrimaryVioletLight,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = action.title,
                style = MonospaceHeader.copy(fontSize = 11.sp),
                color = TextPrimary
            )
        }
    }
}
