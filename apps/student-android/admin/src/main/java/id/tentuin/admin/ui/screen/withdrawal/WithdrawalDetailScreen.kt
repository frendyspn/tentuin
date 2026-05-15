package id.tentuin.admin.ui.screen.withdrawal

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
import androidx.compose.runtime.LaunchedEffect
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
import id.tentuin.admin.core.util.toDisplayDate
import id.tentuin.admin.core.util.toRupiah
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
fun WithdrawalDetailScreen(
    navController: NavController,
    @Suppress("UNUSED_PARAMETER") id: String,
    viewModel: WithdrawalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var rejectDialog by remember { mutableStateOf(false) }
    var rejectNotes  by remember { mutableStateOf("") }

    LaunchedEffect(state.finished) {
        if (state.finished) {
            kotlinx.coroutines.delay(600)
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Spacer(Modifier.width(8.dp))
                Text("Detail Withdrawal", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.item == null -> Text(
                    state.error ?: "Withdrawal tidak ditemukan",
                    style = TentuinAdminTypography.bodyMedium,
                    color = TextMuted,
                )
                else -> {
                    val w = state.item!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.amount.toRupiah(),
                                    style = TentuinAdminTypography.headlineMedium,
                                    color = Primary,
                                    modifier = Modifier.weight(1f))
                                StatusBadge(status = w.status)
                            }
                            Spacer(Modifier.height(12.dp))
                            DetailRow("Agen",       w.agent?.fullName ?: "-")
                            DetailRow("Kode",       w.agent?.referralCode ?: "-")
                            DetailRow("Bank",       w.agent?.bankName ?: "-")
                            DetailRow("No. Rek",    w.agent?.bankAccountNumber ?: "-")
                            DetailRow("Atas Nama",  w.agent?.bankAccountName ?: "-")
                            DetailRow("Diminta",    w.requestedAt.toDisplayDate())
                            DetailRow("Diproses",   w.processedAt.toDisplayDate())
                            if (!w.adminNotes.isNullOrBlank()) {
                                DetailRow("Catatan", w.adminNotes)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    when (w.status) {
                        "requested" -> {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TentuinButton(
                                    text = "Tolak",
                                    onClick = { rejectDialog = true },
                                    isLoading = state.mutating,
                                    containerColor = Error,
                                    modifier = Modifier.weight(1f),
                                )
                                TentuinButton(
                                    text = "Setujui",
                                    onClick = { viewModel.approve() },
                                    isLoading = state.mutating,
                                    containerColor = Success,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        "approved" -> {
                            TentuinButton(
                                text = "Tandai Sudah Ditransfer",
                                onClick = { viewModel.markTransferred() },
                                isLoading = state.mutating,
                                containerColor = Primary,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        if (state.toast != null) {
            TentuinToast(
                message = state.toast!!,
                type = if (state.finished) ToastType.SUCCESS else ToastType.INFO,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        if (rejectDialog) {
            AlertDialog(
                onDismissRequest = { rejectDialog = false },
                title = { Text("Tolak withdrawal") },
                text = {
                    OutlinedTextField(
                        value = rejectNotes,
                        onValueChange = { rejectNotes = it },
                        label = { Text("Alasan penolakan") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            rejectDialog = false
                            viewModel.reject(rejectNotes.trim())
                        },
                        enabled = rejectNotes.trim().isNotEmpty(),
                    ) { Text("Tolak", color = Error) }
                },
                dismissButton = {
                    TextButton(onClick = { rejectDialog = false }) { Text("Batal") }
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
