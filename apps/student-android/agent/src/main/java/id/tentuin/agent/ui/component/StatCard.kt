package id.tentuin.agent.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import id.tentuin.agent.ui.theme.TentuinAgentTypography
import id.tentuin.agent.ui.theme.Primary

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Primary
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, style = TentuinAgentTypography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = TentuinAgentTypography.titleLarge, color = color)
        }
    }
}
