package id.tentuin.admin.ui.screen.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import id.tentuin.admin.core.util.toDisplayDate
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun SchoolDetailScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") schoolId: String,
    viewModel: SchoolDetailViewModel = hiltViewModel(),
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
            }
            Spacer(Modifier.width(8.dp))
            Text("Detail Sekolah", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        }
        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            state.school == null -> Text(state.error ?: "Sekolah tidak ditemukan",
                style = TentuinAdminTypography.bodyMedium, color = TextMuted)
            else -> {
                val s = state.school!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(s.name, style = TentuinAdminTypography.titleLarge, color = TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        DetailRow("NPSN",     s.npsn ?: "-")
                        DetailRow("Kota",     s.city)
                        DetailRow("Provinsi", s.province)
                        DetailRow("Alamat",   s.address ?: "-")
                        DetailRow("Email",    s.email ?: "-")
                        DetailRow("Telp",     s.phone ?: "-")
                        DetailRow("Siswa",    "${s.totalStudents}")
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Klaim Aktif", style = TentuinAdminTypography.titleLarge, color = TextPrimary)
                Spacer(Modifier.height(8.dp))

                val active  = s.activeClaim
                val pending = s.pendingClaim
                val claim   = active ?: pending
                if (claim == null) {
                    Text("Belum ada klaim", style = TentuinAdminTypography.bodyMedium, color = TextMuted)
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(claim.agent?.fullName ?: "(agen)",
                                    style = TentuinAdminTypography.titleMedium,
                                    color = TextPrimary, modifier = Modifier.weight(1f))
                                StatusBadge(status = claim.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Kode",      claim.agent?.referralCode ?: "-")
                            DetailRow("Telp",      "-")
                            DetailRow("Diklaim",   claim.claimedAt.toDisplayDate())
                            if (claim.status == "pending") {
                                DetailRow("Claim Code", claim.claimCode ?: "-")
                                DetailRow("Expires",    claim.expiresAt.toDisplayDate())
                            } else if (claim.verifiedAt != null) {
                                DetailRow("Diverifikasi", claim.verifiedAt.toDisplayDate())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = TentuinAdminTypography.labelMedium, color = TextMuted, modifier = Modifier.width(110.dp))
        Text(value, style = TentuinAdminTypography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}
