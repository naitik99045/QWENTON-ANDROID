package ai.qwenton.apex.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ai.qwenton.apex.ui.theme.*

@Composable
fun WorkspaceDialog(
    currentWorkspace: String,
    onSelectWorkspace: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var customPath by remember { mutableStateOf(currentWorkspace) }

    val presetPaths = listOf(
        "~/workspace",
        "~/projects",
        "~/qwenton-apex-server",
        "/sdcard/Projects",
        "/sdcard/Download"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Coding Workspace",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Text(
                    text = "Target project directory for reading, editing, and pentesting code on device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // Custom Path Input
                OutlinedTextField(
                    value = customPath,
                    onValueChange = { customPath = it },
                    label = { Text("Workspace Path") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PrimaryViolet,
                        unfocusedBorderColor = CharcoalBorder
                    ),
                    singleLine = true
                )

                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )

                // Presets list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 160.dp)
                ) {
                    items(presetPaths) { path ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (customPath == path) CharcoalSurfaceVariant else CharcoalSurface)
                                .border(1.dp, if (customPath == path) PrimaryVioletLight else CharcoalBorder, RoundedCornerShape(8.dp))
                                .clickable { customPath = path }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = PrimaryVioletLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = path,
                                style = MonospaceBody.copy(fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customPath.isNotBlank()) {
                                onSelectWorkspace(customPath.trim())
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet)
                    ) {
                        Text("Set Workspace")
                    }
                }
            }
        }
    }
}
