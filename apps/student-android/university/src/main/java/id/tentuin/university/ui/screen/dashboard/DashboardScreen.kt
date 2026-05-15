package id.tentuin.university.ui.screen.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.core.util.relativeTime
import id.tentuin.university.data.model.ProspectFollowup
import id.tentuin.university.ui.component.EmptyState
import id.tentuin.university.ui.component.QuotaCard
import id.tentuin.university.ui.component.StatusBadge
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun DashboardScreen(navController: NavController, vm: DashboardViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    Column(
        modifier = Modifier.fillMaxSize().background(Background)
            .statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text("Dashboard", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
        s.account?.let { acc ->
            Text(acc.displayName, style = TentuinUniversityTypography.bodyMedium, color = TextSub)
        }
        Spacer(Modifier.height(16.dp))

        if (s.loading && s.account == null) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            s.account?.let { acc ->
                QuotaCard(
                    quotaBalance   = acc.quotaBalance,
                    totalPurchased = acc.totalQuotaPurchased,
                    accountTypeLabel = if (acc.accountType == "enterprise") "Enterprise" else "Personal",
                    onTopUp = { navController.navigate(Route.Subscribe.route) },
                )
                Spacer(Modifier.height(16.dp))
                Text("Sedang di-Follow-up", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))

                if (s.activeFollowups.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().background(Surface, RoundedCornerShape(12.dp))) {
                        EmptyState(
                            title = "Belum ada prospek aktif",
                            description = "Buka tab Prospek untuk mulai claim siswa.",
                            icon = Icons.Outlined.Inbox,
                        )
                    }
                } else {
                    s.activeFollowups.forEach { fu ->
                        FollowupRow(fu) { navController.navigate(Route.Followup.create(fu.id)) }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
        s.error?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = Error, style = TentuinUniversityTypography.bodyMedium)
        }
    }
}

@Composable
private fun FollowupRow(fu: ProspectFollowup, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Surface,
        onClick = onClick,
        tonalElevation = 1.dp,
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(fu.prospect?.fullName ?: "Prospek", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                Text(
                    fu.prospect?.school?.name ?: fu.prospect?.schoolName ?: "—",
                    style = TentuinUniversityTypography.bodyMedium, color = TextSub,
                )
                Text("Aktivitas: ${relativeTime(fu.lastActivityAt)}", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
            }
            StatusBadge(status = fu.status)
        }
    }
}
