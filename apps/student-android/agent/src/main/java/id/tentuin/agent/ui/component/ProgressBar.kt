package id.tentuin.agent.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.agent.ui.theme.TentuinAgentTypography

@Composable
fun AgentProgressBar(
    current: Int,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (target > 0) current.toFloat() / target else 0f
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Target:", style = TentuinAgentTypography.labelSmall)
            Text(text = "$current / $target", style = TentuinAgentTypography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}
