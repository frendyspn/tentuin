package id.tentuin.admin.ui.screen.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.admin.core.util.toRupiah
import id.tentuin.admin.core.util.toRelativeTime
import id.tentuin.admin.ui.component.EmptyState
import id.tentuin.admin.ui.component.StatCard
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Border
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.Success
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary
import id.tentuin.admin.ui.theme.Warning

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* no-op */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("Halo, ${state.adminName ?: "Admin"}", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        Text("Ringkasan aktivitas terkini", style = TentuinAdminTypography.bodyMedium, color = TextMuted)

        Spacer(Modifier.height(24.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Withdraw Pending", "${state.pendingWithdrawals}", color = Warning, modifier = Modifier.weight(1f))
                StatCard("Agen Aktif",       "${state.activeAgents}/${state.totalAgents}", color = Success, modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            Text("Withdraw Menunggu Approval", style = TentuinAdminTypography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            if (state.recentPending.isEmpty()) {
                EmptyState(title = "Tidak ada withdraw pending", subtitle = "Semua sudah diproses")
            } else {
                state.recentPending.forEach { w ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                navController.navigate(Route.WithdrawalDetail.build(w.id))
                            },
                        colors = CardDefaults.cardColors(containerColor = id.tentuin.admin.ui.theme.Surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(w.agent?.fullName ?: "(agen tidak ditemukan)",
                                style = TentuinAdminTypography.titleMedium, color = TextPrimary)
                            Spacer(Modifier.height(2.dp))
                            Text(w.amount.toRupiah(), style = TentuinAdminTypography.titleLarge, color = Primary)
                            Spacer(Modifier.height(4.dp))
                            Text(w.requestedAt.toRelativeTime(),
                                style = TentuinAdminTypography.labelSmall, color = TextMuted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
    }
}
