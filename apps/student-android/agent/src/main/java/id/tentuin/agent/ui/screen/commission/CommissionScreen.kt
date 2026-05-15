package id.tentuin.agent.ui.screen.commission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.core.util.toRupiah
import id.tentuin.agent.ui.component.*
import id.tentuin.agent.ui.component.toast.TentuinToast
import id.tentuin.agent.ui.component.toast.ToastType
import id.tentuin.agent.ui.theme.*
import java.util.Calendar

@Composable
fun CommissionScreen(
    navController: NavController,
    viewModel: CommissionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.refresh()
    }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && pullRefreshState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            CommissionTopBar(onBack = { navController.popBackStack() })
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
                    item { YearSelector(year = state.year, onYearChange = viewModel::setYear) }

                    item { SummaryRow(state = state) }

                    item {
                        Text(
                            text = "Rincian per Bulan",
                            style = TentuinAgentTypography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }

                    when {
                        state.isLoading && state.commissions.isEmpty() -> items(4) {
                            SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 140.dp, cornerRadius = 12.dp)
                        }
                        state.commissions.isEmpty() -> item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                                EmptyState(
                                    title       = "Belum ada komisi tahun ${state.year}",
                                    description = "Komisi akan muncul saat ada prospek atau langganan kampus.",
                                    icon        = Icons.Outlined.Payments,
                                )
                            }
                        }
                        else -> items(state.commissions, key = { it.id }) { commission ->
                            CommissionCard(commission = commission)
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
                    viewModel.clearError()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommissionTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Komisi", style = TentuinAgentTypography.titleLarge, color = TextPrimary) },
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
private fun YearSelector(year: Int, onYearChange: (Int) -> Unit) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val canForward = year < currentYear

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(onClick = { onYearChange(year - 1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Tahun sebelumnya", tint = Primary)
            }
            Text(
                text  = year.toString(),
                style = TentuinAgentTypography.titleLarge,
                color = TextPrimary,
            )
            IconButton(
                onClick = { if (canForward) onYearChange(year + 1) },
                enabled = canForward,
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Tahun berikutnya",
                    tint = if (canForward) Primary else TextMuted,
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(state: CommissionUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text  = "Total Komisi ${state.year}",
                    style = TentuinAgentTypography.labelMedium,
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = state.totalAll.toRupiah(),
                    style = TentuinAgentTypography.headlineMedium,
                    color = androidx.compose.ui.graphics.Color.White,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label    = "Pending",
                value    = state.totalPending.toRupiah(),
                modifier = Modifier.weight(1f),
                color    = Warning,
            )
            StatCard(
                label    = "Dibayar",
                value    = state.totalPaid.toRupiah(),
                modifier = Modifier.weight(1f),
                color    = Success,
            )
        }
    }
}
