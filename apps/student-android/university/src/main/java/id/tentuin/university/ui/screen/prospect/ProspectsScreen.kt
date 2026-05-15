package id.tentuin.university.ui.screen.prospect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.core.util.relativeTime
import id.tentuin.university.data.model.Prospect
import id.tentuin.university.data.model.ProspectFollowup
import id.tentuin.university.ui.component.EmptyState
import id.tentuin.university.ui.component.StatusBadge
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun ProspectsScreen(navController: NavController, vm: ProspectsViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text("Prospek", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            TabRow(
                selectedTabIndex = if (s.tab == ProspectTab.DISCOVER) 0 else 1,
                containerColor = Background,
                contentColor = Primary,
            ) {
                Tab(selected = s.tab == ProspectTab.DISCOVER, onClick = { vm.setTab(ProspectTab.DISCOVER) }) {
                    Text("Discover", modifier = Modifier.padding(vertical = 12.dp))
                }
                Tab(selected = s.tab == ProspectTab.MY_FOLLOWUPS, onClick = { vm.setTab(ProspectTab.MY_FOLLOWUPS) }) {
                    Text("Follow-up Saya", modifier = Modifier.padding(vertical = 12.dp))
                }
            }
            Spacer(Modifier.height(12.dp))

            when (s.tab) {
                ProspectTab.DISCOVER -> DiscoverTab(s, vm, navController)
                ProspectTab.MY_FOLLOWUPS -> FollowupsTab(s, navController)
            }
        }

        s.toast?.let {
            TentuinToast(it, ToastType.SUCCESS, modifier = Modifier.align(Alignment.TopCenter))
            LaunchedEffect(it) { kotlinx.coroutines.delay(2500); vm.clearMessages() }
        }
        s.error?.let {
            TentuinToast(it, ToastType.ERROR, modifier = Modifier.align(Alignment.TopCenter))
            LaunchedEffect(it) { kotlinx.coroutines.delay(3000); vm.clearMessages() }
        }
    }
}

@Composable
private fun DiscoverTab(s: ProspectsUiState, vm: ProspectsViewModel, nav: NavController) {
    OutlinedTextField(
        value = s.query,
        onValueChange = vm::search,
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        placeholder = { Text("Cari nama siswa…") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
    Spacer(Modifier.height(12.dp))

    if (s.loading && s.prospects.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else if (s.prospects.isEmpty()) {
        EmptyState("Tidak ada prospek", "Coba cari dengan kata kunci lain.", icon = Icons.Outlined.Inbox)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(s.prospects, key = { it.id }) { p ->
                ProspectRow(
                    prospect = p,
                    isUnlocking = s.unlockingId == p.id,
                    onUnlock = { vm.unlock(p) { fid -> nav.navigate(Route.Followup.create(fid)) } },
                )
            }
        }
    }
}

@Composable
private fun FollowupsTab(s: ProspectsUiState, nav: NavController) {
    if (s.loading && s.followups.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
    } else if (s.followups.isEmpty()) {
        EmptyState("Belum ada follow-up", "Claim prospek dari tab Discover.", icon = Icons.Outlined.Inbox)
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(s.followups, key = { it.id }) { fu ->
                FollowupRow(fu) { nav.navigate(Route.Followup.create(fu.id)) }
            }
        }
    }
}

@Composable
private fun ProspectRow(prospect: Prospect, isUnlocking: Boolean, onUnlock: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(prospect.fullName ?: "Siswa", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                Text(
                    prospect.school?.name ?: prospect.schoolName ?: "—",
                    style = TentuinUniversityTypography.bodyMedium, color = TextSub,
                )
                prospect.testResults?.firstOrNull()?.riasecCode?.let {
                    Text("RIASEC: $it", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                }
            }
            Spacer(Modifier.width(8.dp))
            TentuinButton(text = "Buka", onClick = onUnlock, isLoading = isUnlocking)
        }
    }
}

@Composable
private fun FollowupRow(fu: ProspectFollowup, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp, onClick = onClick) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
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
