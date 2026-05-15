package id.tentuin.admin.ui.screen.withdrawal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.admin.core.util.toRelativeTime
import id.tentuin.admin.core.util.toRupiah
import id.tentuin.admin.data.model.WithdrawalWithAgent
import id.tentuin.admin.ui.component.EmptyState
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun WithdrawalListScreen(
    navController: NavController,
    viewModel: WithdrawalListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Withdrawal",
            style = TentuinAdminTypography.headlineMedium,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(8.dp))

        TabRow(selectedTabIndex = tab, contentColor = Primary) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text("Menunggu (${state.pending.size})") },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text("Riwayat (${state.all.size})") },
            )
        }

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            else -> {
                val list = if (tab == 0) state.pending else state.all
                if (list.isEmpty()) {
                    EmptyState(
                        title = if (tab == 0) "Tidak ada withdraw pending" else "Belum ada withdrawal",
                    )
                } else {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(list, key = { it.id }) { w ->
                            WithdrawalCard(w) {
                                navController.navigate(Route.WithdrawalDetail.build(w.id))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WithdrawalCard(w: WithdrawalWithAgent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(w.agent?.fullName ?: "(agen tidak ditemukan)",
                    style = TentuinAdminTypography.titleMedium, color = TextPrimary, modifier = Modifier.weight(1f))
                StatusBadge(status = w.status)
            }
            Spacer(Modifier.height(4.dp))
            Text(w.amount.toRupiah(), style = TentuinAdminTypography.titleLarge, color = Primary)
            Spacer(Modifier.height(4.dp))
            Text(w.requestedAt.toRelativeTime(), style = TentuinAdminTypography.labelSmall, color = TextMuted)
        }
    }
}
