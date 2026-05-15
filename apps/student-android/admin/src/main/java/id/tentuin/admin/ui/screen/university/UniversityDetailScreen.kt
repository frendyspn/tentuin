package id.tentuin.admin.ui.screen.university

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.admin.core.util.toDisplayDate
import id.tentuin.admin.core.util.toRupiah
import id.tentuin.admin.ui.component.StatCard
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.component.TentuinButton
import id.tentuin.admin.ui.component.TentuinToast
import id.tentuin.admin.ui.component.ToastType
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun UniversityDetailScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") universityId: String,
    viewModel: UniversityDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showSubscribeDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(Modifier.width(8.dp))
                Text("Detail Universitas", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.university == null -> Text(
                    state.error ?: "Universitas tidak ditemukan",
                    style = TentuinAdminTypography.bodyMedium, color = TextMuted,
                )
                else -> {
                    val u = state.university!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.name, style = TentuinAdminTypography.titleLarge,
                                    color = TextPrimary, modifier = Modifier.weight(1f))
                                if (u.isPartner) Text(
                                    text  = u.partnerTier?.uppercase() ?: "PARTNER",
                                    style = TentuinAdminTypography.labelSmall,
                                    color = Success,
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            DetailRow("Kota",     u.city ?: "-")
                            DetailRow("PIC",      u.picName ?: "-")
                            DetailRow("PIC Telp", u.picPhone ?: "-")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Sisa Kuota", "${u.quotaBalance}", modifier = Modifier.weight(1f))
                        StatCard("Total Beli", "${u.totalQuotaPurchased}", modifier = Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Klaim", style = TentuinAdminTypography.titleLarge, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    val claim = u.activeClaim ?: u.pendingClaim
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
                                DetailRow("Kode",    claim.agent?.referralCode ?: "-")
                                DetailRow("Diklaim", claim.claimedAt.toDisplayDate())
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TentuinButton(
                        text = "Catat Subscribe",
                        onClick = { showSubscribeDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(24.dp))
                    Text("Riwayat Subscribe", style = TentuinAdminTypography.titleLarge, color = TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    if (state.subscribeLogs.isEmpty()) {
                        Text("Belum ada", style = TentuinAdminTypography.bodyMedium, color = TextMuted)
                    } else {
                        state.subscribeLogs.forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(log.amount.toRupiah(), style = TentuinAdminTypography.titleMedium,
                                            color = Primary, modifier = Modifier.weight(1f))
                                        Text("+${log.quotaPurchased} kuota",
                                            style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(log.subscribedAt.toDisplayDate(),
                                        style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                    if (log.commissionAgent > 0) {
                                        Text("Komisi agen: ${log.commissionAgent.toRupiah()}",
                                            style = TentuinAdminTypography.labelSmall, color = Success)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.toast != null) {
            TentuinToast(
                message  = state.toast!!,
                type     = ToastType.SUCCESS,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (showSubscribeDialog) {
            RecordSubscribeDialog(
                isLoading = state.mutating,
                onDismiss = { showSubscribeDialog = false },
                onSubmit  = { amount, quota ->
                    viewModel.submitSubscribe(amount, quota) { showSubscribeDialog = false }
                },
            )
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

@Composable
private fun RecordSubscribeDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit:  (amount: Int, quota: Int) -> Unit,
) {
    var amountStr by remember { mutableStateOf("") }
    var quotaStr  by remember { mutableStateOf("") }
    val amount = amountStr.toIntOrNull() ?: 0
    val quota  = quotaStr.toIntOrNull() ?: 0
    val valid  = amount > 0 && quota > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Subscribe") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah Bayar (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = quotaStr,
                    onValueChange = { quotaStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Kuota Dibeli") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (valid) {
                    Spacer(Modifier.height(8.dp))
                    Text("Komisi agen 10%: ${(amount * 10 / 100).toRupiah()}",
                        style = TentuinAdminTypography.labelSmall, color = TextMuted)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid && !isLoading,
                onClick = { onSubmit(amount, quota) },
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}
