package id.tentuin.agent.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.tentuin.agent.data.model.School
import id.tentuin.agent.ui.theme.TentuinAgentTypography
import id.tentuin.agent.ui.theme.Primary

@Composable
fun SchoolCard(
    school: School,
    onClaimClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = school.name, style = TentuinAgentTypography.titleMedium)
            Text(text = "${school.city}, ${school.province}", style = TentuinAgentTypography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onClaimClick, modifier = Modifier.fillMaxWidth()) {
                Text("Klaim Sekolah")
            }
        }
    }
}
