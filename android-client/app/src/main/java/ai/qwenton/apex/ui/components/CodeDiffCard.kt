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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.ui.theme.*

@Composable
fun CodeDiffCard(
    filePath: String,
    diffContent: String,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TerminalBg)
            .border(1.dp, CharcoalBorder, RoundedCornerShape(8.dp))
    ) {
        // File Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TerminalHeaderBg)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "DIFF: $filePath",
                style = MonospaceHeader.copy(fontSize = 11.sp),
                color = PrimaryVioletLight
            )
            Text(
                text = "SURGICAL PATCH",
                style = MonospaceHeader.copy(fontSize = 9.sp),
                color = TextMuted
            )
        }

        // Diff Lines Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            diffContent.lines().forEach { line ->
                val isAddition = line.startsWith("+")
                val isDeletion = line.startsWith("-")
                val isHunkHeader = line.startsWith("@@")

                val bgColor = when {
                    isAddition -> DiffAddBg.copy(alpha = 0.5f)
                    isDeletion -> DiffRemoveBg.copy(alpha = 0.5f)
                    else -> TerminalBg
                }

                val textColor = when {
                    isAddition -> DiffAddText
                    isDeletion -> DiffRemoveText
                    isHunkHeader -> StatusCyan
                    else -> TextSecondary
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = line,
                        style = MonospaceBody.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}
