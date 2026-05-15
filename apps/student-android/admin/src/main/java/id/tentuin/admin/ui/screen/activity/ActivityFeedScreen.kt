package id.tentuin.admin.ui.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.admin.core.util.toRelativeTime
import id.tentuin.admin.data.model.AdminAuditLog
import id.tentuin.admin.ui.component.EmptyState
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun ActivityFeedScreen(
    navController: NavController,
    viewModel: ActivityFeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Text("Activity Feed", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        }
        Text("Audit log aksi admin", style = TentuinAdminTypography.bodyMedium, color = TextMuted)
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            state.logs.isEmpty() -> EmptyState(title = "Belum ada aktivitas", subtitle = state.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.logs, key = { it.id }) { log -> AuditRow(log) }
            }
        }
    }
}

@Composable
private fun AuditRow(log: AdminAuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(humanizeAction(log.action),
                    style = TentuinAdminTypography.titleMedium, color = TextPrimary,
                    modifier = Modifier.weight(1f))
                Text(log.createdAt.toRelativeTime(),
                    style = TentuinAdminTypography.labelSmall, color = TextMuted)
            }
            Spacer(Modifier.height(2.dp))
            Text("oleh ${log.admin?.fullName ?: "admin"} • ${log.resourceType}",
                style = TentuinAdminTypography.labelSmall, color = TextMuted)
            if (log.newValues != null) {
                Spacer(Modifier.height(6.dp))
                Text(log.newValues.toString(),
                    style = TentuinAdminTypography.labelSmall, color = Primary)
            }
        }
    }
}

private fun humanizeAction(action: String): String = when (action) {
    "withdrawal.approve"          -> "Setujui withdrawal"
    "withdrawal.reject"           -> "Tolak withdrawal"
    "withdrawal.transfer"         -> "Tandai withdrawal transferred"
    "agent.status.suspended"      -> "Suspend agen"
    "agent.status.active"         -> "Aktifkan agen"
    "university.subscribe.record" -> "Catat subscribe universitas"
    else                          -> action
}
