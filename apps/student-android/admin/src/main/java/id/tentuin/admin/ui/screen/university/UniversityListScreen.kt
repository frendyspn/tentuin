package id.tentuin.admin.ui.screen.university

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.admin.ui.component.EmptyState
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun UniversityListScreen(
    navController: NavController,
    viewModel: UniversityListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val list = viewModel.filtered()

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
            Text("Universitas", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        }
        Text("${state.universities.size} universitas",
            style = TentuinAdminTypography.bodyMedium, color = TextMuted)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            label = { Text("Cari nama/kota") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            list.isEmpty() -> EmptyState(title = "Tidak ada universitas", subtitle = state.error)
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(list, key = { it.id }) { u ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Route.UniversityDetail.build(u.id)) },
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(u.name, style = TentuinAdminTypography.titleMedium, color = TextPrimary)
                                    Text("${u.city ?: "-"} • Kuota ${u.quotaBalance}",
                                        style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                }
                                if (u.isPartner) {
                                    Text(
                                        text = u.partnerTier?.uppercase() ?: "PARTNER",
                                        style = TentuinAdminTypography.labelSmall,
                                        color = Success,
                                    )
                                }
                            }
                            val claim = u.activeClaim ?: u.pendingClaim
                            if (claim != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Agen: ${claim.agent?.fullName ?: "-"}",
                                        style = TentuinAdminTypography.labelSmall, color = TextMuted,
                                        modifier = Modifier.weight(1f))
                                    StatusBadge(status = claim.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
