package id.tentuin.schoolpic.ui.screen.commission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.schoolpic.core.util.monthYearId
import id.tentuin.schoolpic.core.util.toRupiah
import id.tentuin.schoolpic.data.model.SchoolCommission
import id.tentuin.schoolpic.ui.component.EmptyState
import id.tentuin.schoolpic.ui.component.ErrorBanner
import id.tentuin.schoolpic.ui.component.StatusBadge
import id.tentuin.schoolpic.ui.theme.*

@Composable
fun CommissionScreen(
    navController: NavController,
    viewModel: CommissionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Surface(
            color = Primary,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Komisi Sekolah", color = androidx.compose.ui.graphics.Color.White,
                    style = TentuinSchoolPicTypography.headlineMedium)
                Text(
                    "Total komisi yang sekolah Anda terima dari prospek siswa.",
                    color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                    style = TentuinSchoolPicTypography.bodyMedium,
                )
            }
        }

        if (state.error != null) {
            ErrorBanner(state.error!!, modifier = Modifier.padding(16.dp))
        }

        SummaryRow(state = state)

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary) }

            state.items.isEmpty() -> EmptyState(
                title = "Belum ada komisi",
                subtitle = "Komisi akan muncul saat siswa dari sekolah Anda dilihat oleh kampus mitra.",
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.items, key = { it.id }) { row -> CommissionCard(row) }
            }
        }
    }
}

@Composable
private fun SummaryRow(state: CommissionUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(label = "Total", value = state.totalAll, modifier = Modifier.weight(1f))
        SummaryCard(label = "Dibayar", value = state.totalPaid, modifier = Modifier.weight(1f))
        SummaryCard(label = "Menunggu", value = state.totalPending, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SummaryCard(label: String, value: Long, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(12.dp),
    ) {
        Text(label, style = TentuinSchoolPicTypography.labelSmall, color = TextMuted)
        Spacer(Modifier.height(4.dp))
        Text(
            value.toRupiah(),
            style = TentuinSchoolPicTypography.titleMedium,
            color = TextPrimary,
        )
    }
}

@Composable
private fun CommissionCard(row: SchoolCommission) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(monthYearId(row.month, row.year),
                style = TentuinSchoolPicTypography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(row.amount.toRupiah(),
                style = TentuinSchoolPicTypography.bodyMedium, color = TextSub)
        }
        StatusBadge(row.status)
    }
}
