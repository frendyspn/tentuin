package id.tentuin.university.ui.screen.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.theme.*

@Composable
fun AddMemberScreen(navController: NavController, vm: TeamViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    var userId by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { vm.load() }
    LaunchedEffect(s.toast) {
        if (s.toast != null) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Kembali", tint = TextPrimary)
                }
                Text("Tambah Anggota", style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Tempelkan User ID (UUID) dari orang yang ingin Anda undang. Jika ia punya account personal aktif, kuotanya akan di-merge ke tim ini.",
                style = TentuinUniversityTypography.bodyMedium, color = TextSub,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                placeholder = { Text("00000000-0000-0000-0000-000000000000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(24.dp))
            TentuinButton(
                "Tambahkan",
                onClick = { vm.addMember(userId) },
                isLoading = s.submitting,
                enabled = userId.length > 30,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        s.toast?.let {
            TentuinToast(it, ToastType.SUCCESS, modifier = Modifier.align(Alignment.TopCenter))
        }
        s.error?.let {
            TentuinToast(it, ToastType.ERROR, modifier = Modifier.align(Alignment.TopCenter))
            LaunchedEffect(it) { kotlinx.coroutines.delay(3000); vm.clearMessages() }
        }
    }
}
