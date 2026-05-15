package id.tentuin.university.ui.screen.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.data.model.AccountMember
import id.tentuin.university.ui.component.EmptyState
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun TeamScreen(navController: NavController, vm: TeamViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            Text("Tim", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            when {
                s.loading && s.account == null -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                s.account?.accountType == "personal" -> {
                    EmptyState(
                        title = "Account Personal",
                        description = "Account personal tidak punya tim. Upgrade ke Enterprise untuk mengundang anggota.",
                        icon = Icons.Outlined.Person,
                    )
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(s.account?.displayName ?: "Tim", style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
                            Text("${s.members.size} anggota aktif", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
                        }
                        TentuinButton("+ Member", onClick = { navController.navigate(Route.AddMember.route) })
                    }
                    Spacer(Modifier.height(16.dp))

                    if (s.members.isEmpty()) {
                        EmptyState(title = "Belum ada anggota", description = "Klik + Member untuk menambah anggota.", icon = Icons.Outlined.GroupAdd)
                    } else {
                        s.members.forEach { m ->
                            MemberRow(m)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
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
private fun MemberRow(m: AccountMember) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Person, null, tint = TextMuted, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(m.profile?.fullName ?: "Anggota", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                Text("ID: ${m.userId.take(8)}…", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
            }
            Text(
                if (m.role == "owner") "Owner" else "Member",
                style = TentuinUniversityTypography.labelSmall,
                color = if (m.role == "owner") Primary else TextSub,
            )
        }
    }
}
