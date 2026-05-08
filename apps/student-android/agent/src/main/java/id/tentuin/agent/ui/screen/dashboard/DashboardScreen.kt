package id.tentuin.agent.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.core.util.toRupiah
import id.tentuin.agent.ui.component.*
import id.tentuin.agent.ui.theme.*

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Text("Halo, ${state.agent?.fullName ?: "Agen"}", style = TentuinAgentTypography.headlineMedium, color = TextPrimary)
            Text("Kode Referral: ${state.agent?.referralCode ?: "-"}", style = TentuinAgentTypography.bodyMedium, color = TextMuted)
            
            Spacer(Modifier.height(24.dp))
            
            // Komisi Bulan Ini
            Text("Komisi Bulan Ini", style = TentuinAgentTypography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    label = "Stream A",
                    value = state.currentMonthComm?.streamAAmount?.toRupiah() ?: "Rp 0",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Stream B",
                    value = state.currentMonthComm?.streamBAmount?.toRupiah() ?: "Rp 0",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // Statistik Ringkasan
            Text("Statistik", style = TentuinAgentTypography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Pending", state.totalPending.toRupiah(), modifier = Modifier.weight(1f), color = Warning)
                StatCard("Dibayar", state.totalPaid.toRupiah(), modifier = Modifier.weight(1f), color = Success)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Klaim Sekolah", "${state.schoolClaimCount}", modifier = Modifier.weight(1f))
                StatCard("Klaim Kampus", "${state.uniClaimCount}", modifier = Modifier.weight(1f))
            }
        }
    }
}
