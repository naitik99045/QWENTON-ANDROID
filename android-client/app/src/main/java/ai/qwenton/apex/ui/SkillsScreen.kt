package ai.qwenton.apex.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.qwenton.apex.domain.RiskLevel
import ai.qwenton.apex.domain.SkillInfo
import ai.qwenton.apex.ui.theme.*

@Composable
fun SkillsScreen(
    skills: List<SkillInfo>,
    onToggleSkill: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCharcoalBg)
            .padding(14.dp)
    ) {
        Text(
            text = "Skills & Tool Policy",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        Text(
            text = "Deterministic execution policy prevents arbitrary shell execution. Tools require explicit activation.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(skills, key = { it.id }) { skill ->
                SkillCard(
                    skill = skill,
                    onToggle = { isChecked -> onToggleSkill(skill.id, isChecked) }
                )
            }
        }
    }
}

@Composable
fun SkillCard(
    skill: SkillInfo,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        color = CharcoalSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    RiskBadge(risk = skill.riskLevel)
                }

                Switch(
                    checked = skill.isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PrimaryVioletLight,
                        checkedTrackColor = PrimaryVioletDark,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = CharcoalBorder
                    )
                )
            }

            Text(
                text = skill.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Text(
                text = "Permission: ${skill.permissionRequired}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
