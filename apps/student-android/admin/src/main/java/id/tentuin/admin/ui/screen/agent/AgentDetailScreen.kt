package id.tentuin.admin.ui.screen.agent

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
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.component.TentuinButton
import id.tentuin.admin.ui.component.TentuinToast
import id.tentuin.admin.ui.component.ToastType
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Error
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun AgentDetailScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") agentId: String,
    viewModel: AgentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(Modifier.width(8.dp))
                Text("Detail Agen", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.agent == null -> Text(
                    state.error ?: "Agen tidak ditemukan",
                    style = TentuinAdminTypography.bodyMedium,
                    color = TextMuted,
                )
                else -> {
                    val a = state.agent!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(a.fullName, style = TentuinAdminTypography.titleLarge, color = TextPrimary, modifier = Modifier.weight(1f))
                                StatusBadge(status = a.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            DetailRow("Email",      a.email)
                            DetailRow("Telp",       a.phone ?: "-")
                            DetailRow("Kode",       a.referralCode)
                            DetailRow("Bank",       a.bankName ?: "-")
                            DetailRow("No. Rek",    a.bankAccountNumber ?: "-")
                            DetailRow("Atas Nama",  a.bankAccountName ?: "-")
                            DetailRow("Last Active",a.lastActiveAt ?: "-")
                            DetailRow("Catatan",    a.notes ?: "-")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    val isActive = a.status == "active"
                    TentuinButton(
                        text = if (isActive) "Suspend Agen" else "Aktifkan Kembali",
                        onClick = { viewModel.toggleSuspend() },
                        isLoading = state.mutating,
                        containerColor = if (isActive) Error else Success,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (state.toast != null) {
            TentuinToast(
                message = state.toast!!,
                type = ToastType.SUCCESS,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, style = TentuinAdminTypography.labelMedium, color = TextMuted, modifier = Modifier.width(110.dp))
        Text(value, style = TentuinAdminTypography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
    }
}
