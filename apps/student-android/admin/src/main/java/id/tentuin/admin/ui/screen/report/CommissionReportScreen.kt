package id.tentuin.admin.ui.screen.report

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import id.tentuin.admin.core.util.toRupiah
import id.tentuin.admin.ui.component.EmptyState
import id.tentuin.admin.ui.component.StatCard
import id.tentuin.admin.ui.component.StatusBadge
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary
import java.util.Calendar

private val monthLabels = listOf(
    "Januari","Februari","Maret","April","Mei","Juni",
    "Juli","Agustus","September","Oktober","November","Desember",
)

@Composable
fun CommissionReportScreen(
    navController: NavController,
    viewModel: CommissionReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

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
            Text("Laporan Komisi", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MonthDropdown(
                selected = state.month,
                onChange = { viewModel.load(it, state.year) },
                modifier = Modifier.weight(1f),
            )
            YearDropdown(
                selected = state.year,
                onChange = { viewModel.load(state.month, it) },
                modifier = Modifier.width(100.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Stream A", state.totalStreamA.toRupiah(), modifier = Modifier.weight(1f))
            StatCard("Stream B", state.totalStreamB.toRupiah(), modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        StatCard("Total", state.totalAll.toRupiah(), modifier = Modifier.fillMaxWidth(), color = Success)

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            state.commissions.isEmpty() -> EmptyState(
                title = "Belum ada komisi",
                subtitle = "${monthLabels[state.month - 1]} ${state.year}",
            )
            else -> {
                state.commissions.forEach { c ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.agent?.fullName ?: "(agen)",
                                        style = TentuinAdminTypography.titleMedium, color = TextPrimary)
                                    Text(c.agent?.referralCode ?: "-",
                                        style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                }
                                StatusBadge(status = c.status)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("A: ${c.streamAAmount.toRupiah()}",
                                    style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                Text("B: ${c.streamBAmount.toRupiah()}",
                                    style = TentuinAdminTypography.labelSmall, color = TextMuted)
                                Text("Total: ${c.totalAmount.toRupiah()}",
                                    style = TentuinAdminTypography.titleMedium, color = Primary,
                                    modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDropdown(
    selected: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = monthLabels[selected - 1],
            onValueChange = {},
            readOnly = true,
            label = { Text("Bulan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            monthLabels.forEachIndexed { idx, label ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = { expanded = false; onChange(idx + 1) },
                )
            }
        }
    }
}

@Composable
private fun YearDropdown(
    selected: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear downTo currentYear - 2).toList()

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = "$selected",
            onValueChange = {},
            readOnly = true,
            label = { Text("Tahun") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            years.forEach { y ->
                DropdownMenuItem(
                    text = { Text("$y") },
                    onClick = { expanded = false; onChange(y) },
                )
            }
        }
    }
}
