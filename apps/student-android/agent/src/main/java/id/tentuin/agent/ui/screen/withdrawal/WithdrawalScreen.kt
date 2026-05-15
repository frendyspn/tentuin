package id.tentuin.agent.ui.screen.withdrawal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.core.util.formatDateId
import id.tentuin.agent.core.util.toRupiah
import id.tentuin.agent.data.model.AgentWithdrawal
import id.tentuin.agent.ui.component.*
import id.tentuin.agent.ui.component.toast.TentuinToast
import id.tentuin.agent.ui.component.toast.ToastType
import id.tentuin.agent.ui.navigation.Route
import id.tentuin.agent.ui.theme.*

@Composable
fun WithdrawalScreen(
    navController: NavController,
    viewModel: WithdrawalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.refresh()
    }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && pullRefreshState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            WithdrawalTopBar(onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { BalanceCard(state = state) }

                    item { StatRow(state = state) }

                    if (!state.hasBankInfo) {
                        item {
                            BankInfoMissingCard(onGoToProfile = {
                                navController.navigate(Route.Profile.route)
                            })
                        }
                    }

                    item {
                        WithdrawalForm(
                            state    = state,
                            onChange = viewModel::onAmountChange,
                            onSubmit = { showConfirmDialog = true },
                        )
                    }

                    item {
                        Text(
                            text = "Riwayat Penarikan",
                            style = TentuinAgentTypography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    when {
                        state.isLoading && state.withdrawals.isEmpty() -> items(3) {
                            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 80.dp, cornerRadius = 12.dp)
                        }
                        state.withdrawals.isEmpty() -> item {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                                EmptyState(
                                    title = "Belum ada riwayat penarikan",
                                    description = "Permintaan penarikan akan muncul di sini.",
                                    icon = Icons.Outlined.Payments,
                                )
                            }
                        }
                        else -> items(state.withdrawals, key = { it.id }) { wd ->
                            WithdrawalCard(withdrawal = wd)
                        }
                    }
                }

                if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }

            state.error?.let {
                TentuinToast(message = it, type = ToastType.ERROR)
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearMessage()
                }
            }
            state.successMessage?.let {
                TentuinToast(message = it, type = ToastType.SUCCESS)
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearMessage()
                }
            }
        }
    }

    if (showConfirmDialog) {
        ConfirmWithdrawalDialog(
            amount  = state.parsedAmount,
            agent   = state,
            onConfirm = {
                showConfirmDialog = false
                viewModel.submitWithdrawal()
            },
            onDismiss = { showConfirmDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WithdrawalTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Tarik Komisi", style = TentuinAgentTypography.titleLarge, color = TextPrimary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
    )
}

@Composable
private fun BalanceCard(state: WithdrawalUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text  = "Saldo Bisa Ditarik",
                style = TentuinAgentTypography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = state.availableBalance.toRupiah(),
                style = TentuinAgentTypography.headlineMedium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun StatRow(state: WithdrawalUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label    = "Sedang Diproses",
            value    = state.pendingAmount.toRupiah(),
            modifier = Modifier.weight(1f),
            color    = Warning,
        )
        StatCard(
            label    = "Sudah Ditarik",
            value    = state.totalWithdrawn.toRupiah(),
            modifier = Modifier.weight(1f),
            color    = Success,
        )
    }
}

@Composable
private fun BankInfoMissingCard(onGoToProfile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AccountBalance, contentDescription = null, tint = Warning)
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Rekening belum diatur",
                    style = TentuinAgentTypography.titleMedium,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "Atur rekening dulu sebelum bisa menarik komisi.",
                style = TentuinAgentTypography.bodySmall,
                color = TextSub,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onGoToProfile) {
                Text("Atur di Profil →", color = Primary)
            }
        }
    }
}

@Composable
private fun WithdrawalForm(
    state: WithdrawalUiState,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text  = "Ajukan Penarikan",
                style = TentuinAgentTypography.titleMedium,
                color = TextPrimary,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.amountInput,
                onValueChange = onChange,
                label = { Text("Jumlah (Rp)") },
                placeholder = { Text("Min. ${MIN_WITHDRAWAL_AMOUNT.toRupiah()}") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = if (state.parsedAmount > 0) {
                    @Composable {
                        Text(
                            text  = state.parsedAmount.toRupiah(),
                            color = Primary,
                        )
                    }
                } else null,
            )

            Spacer(Modifier.height(12.dp))

            TentuinButton(
                text = "Ajukan Penarikan",
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                isLoading = state.isSubmitting,
                enabled = state.hasBankInfo &&
                          state.parsedAmount >= MIN_WITHDRAWAL_AMOUNT &&
                          state.parsedAmount <= state.availableBalance,
            )
        }
    }
}

@Composable
private fun ConfirmWithdrawalDialog(
    amount: Int,
    agent: WithdrawalUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfirmasi Penarikan", style = TentuinAgentTypography.titleLarge) },
        text = {
            Column {
                Text("Tarik ${amount.toRupiah()} ke rekening:", style = TentuinAgentTypography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = agent.agent?.bankName ?: "-",
                    style = TentuinAgentTypography.titleMedium,
                    color = TextPrimary,
                )
                Text(agent.agent?.bankAccountNumber ?: "-", style = TentuinAgentTypography.bodyMedium, color = TextSub)
                Text("a.n. ${agent.agent?.bankAccountName ?: "-"}", style = TentuinAgentTypography.bodyMedium, color = TextSub)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Konfirmasi", color = Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSub)
            }
        },
        containerColor = Surface,
    )
}

@Composable
private fun WithdrawalCard(withdrawal: AgentWithdrawal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = withdrawal.amount.toRupiah(),
                    style = TentuinAgentTypography.titleMedium,
                    color = TextPrimary,
                )
                StatusChipFor(status = withdrawal.status)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text  = "Diajukan: ${formatDateId(withdrawal.requestedAt)}",
                style = TentuinAgentTypography.labelSmall,
                color = TextMuted,
            )
            withdrawal.processedAt?.let {
                Text(
                    text  = "Diproses: ${formatDateId(it)}",
                    style = TentuinAgentTypography.labelSmall,
                    color = TextMuted,
                )
            }
            withdrawal.adminNotes?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = "Catatan: $it",
                    style = TentuinAgentTypography.bodySmall,
                    color = TextSub,
                )
            }
        }
    }
}

@Composable
private fun StatusChipFor(status: String) {
    val (label, color) = when (status) {
        "transferred" -> "Ditransfer" to Success
        "approved"    -> "Disetujui"  to Primary
        "requested"   -> "Diajukan"   to Warning
        "rejected"    -> "Ditolak"    to Error
        else          -> status to TextMuted
    }
    ClaimStatusChip(label = label, color = color)
}
