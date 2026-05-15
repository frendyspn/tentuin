package id.tentuin.university.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController, vm: ProfileViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(s.loggedOut) {
        if (s.loggedOut) {
            navController.navigate(Route.Login.route) { popUpTo(0) { inclusive = true } }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Background).statusBarsPadding().padding(16.dp)) {
        Text("Profil", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(16.dp))

        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(s.user?.userMetadata?.fullName ?: "—", style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
                Text(s.user?.email ?: "—", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
                Spacer(Modifier.height(8.dp))
                Text("User ID", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                Text(s.user?.id ?: "—", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
            }
        }
        Spacer(Modifier.height(12.dp))

        s.account?.let { acc ->
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Account", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                    Text(acc.displayName, style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                    Text(if (acc.accountType == "enterprise") "Enterprise" else "Personal",
                        style = TentuinUniversityTypography.bodyMedium, color = Primary)
                    Spacer(Modifier.height(4.dp))
                    Text("Sisa kuota: ${acc.quotaBalance} data", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
                }
            }
            if (acc.accountType == "enterprise" && acc.ownerUserId != s.user?.id) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { showLeaveDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Keluar dari Tim", color = Error)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        TentuinButton(
            text = "Logout",
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            containerColor = Error,
        )
        Spacer(Modifier.height(16.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Outlined.Logout, null, tint = Error) },
            title = { Text("Logout?") },
            text = { Text("Anda akan keluar dari aplikasi.") },
            confirmButton = { TextButton(onClick = { showLogoutDialog = false; vm.logout() }) { Text("Logout") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } },
        )
    }
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Keluar tim?") },
            text = { Text("Semua follow-up Anda akan dilepas dan kembali ke kolam team.") },
            confirmButton = { TextButton(onClick = { showLeaveDialog = false; vm.leaveTeam() }) { Text("Keluar") } },
            dismissButton = { TextButton(onClick = { showLeaveDialog = false }) { Text("Batal") } },
        )
    }
}
