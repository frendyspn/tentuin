package id.tentuin.university.ui.screen.followup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.core.util.relativeTime
import id.tentuin.university.data.model.FollowupActivity
import id.tentuin.university.ui.component.StatusBadge
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.theme.*

@Composable
fun FollowupScreen(navController: NavController, vm: FollowupViewModel = hiltViewModel()) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    var showActivitySheet by remember { mutableStateOf(false) }
    var showStatusSheet   by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Kembali", tint = TextPrimary)
                }
                Text("Detail Prospek", style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))

            s.followup?.let { fu ->
                ProspectInfoCard(fu)
                Spacer(Modifier.height(16.dp))
                Row {
                    TentuinButton("Catat Aktivitas", onClick = { showActivitySheet = true }, modifier = Modifier.weight(1f), isLoading = s.submitting)
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { showStatusSheet = true }, modifier = Modifier.weight(1f)) {
                        Text("Ubah Status")
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Aktivitas", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                if (s.activities.isEmpty()) {
                    Text("Belum ada aktivitas.", style = TentuinUniversityTypography.bodyMedium, color = TextMuted)
                } else {
                    s.activities.forEach { act ->
                        ActivityRow(act)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        if (showActivitySheet) {
            LogActivitySheet(
                onDismiss = { showActivitySheet = false },
                onSubmit = { type, note ->
                    vm.logActivity(type, note)
                    showActivitySheet = false
                },
            )
        }
        if (showStatusSheet) {
            ChangeStatusSheet(
                currentStatus = s.followup?.status ?: "claimed",
                onDismiss = { showStatusSheet = false },
                onSubmit = { st, note ->
                    vm.changeStatus(st, note)
                    showStatusSheet = false
                },
            )
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
private fun ProspectInfoCard(fu: id.tentuin.university.data.model.ProspectFollowup) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fu.prospect?.fullName ?: "Prospek", style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
                    Text(
                        fu.prospect?.school?.name ?: fu.prospect?.schoolName ?: "—",
                        style = TentuinUniversityTypography.bodyMedium, color = TextSub,
                    )
                    fu.prospect?.city?.let { Text(it, style = TentuinUniversityTypography.labelSmall, color = TextMuted) }
                }
                StatusBadge(status = fu.status)
            }
            Spacer(Modifier.height(12.dp))
            fu.prospect?.testResults?.firstOrNull()?.let { tr ->
                Text("Hasil RIASEC: ${tr.riasecCode ?: "-"}", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Diklaim: ${relativeTime(fu.claimedAt)} • Aktivitas: ${relativeTime(fu.lastActivityAt)}",
                style = TentuinUniversityTypography.labelSmall, color = TextMuted,
            )
            fu.assignedProfile?.fullName?.let { name ->
                Text("Ditugaskan: $name", style = TentuinUniversityTypography.labelSmall, color = TextSub)
            }
        }
    }
}

@Composable
private fun ActivityRow(act: FollowupActivity) {
    val (icon, label) = activityIconAndLabel(act.activityType)
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Surface) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(label, style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                    Spacer(Modifier.weight(1f))
                    Text(relativeTime(act.createdAt), style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                }
                act.note?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(it, style = TentuinUniversityTypography.bodyMedium, color = TextSub)
                }
                act.user?.fullName?.let {
                    Spacer(Modifier.height(2.dp))
                    Text("oleh $it", style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                }
            }
        }
    }
}

private fun activityIconAndLabel(type: String): Pair<ImageVector, String> = when (type) {
    "call"          -> Icons.Outlined.Call      to "Telepon"
    "whatsapp"      -> Icons.Outlined.Chat      to "WhatsApp"
    "email"         -> Icons.Outlined.Email     to "Email"
    "meeting"       -> Icons.Outlined.EventNote to "Pertemuan"
    "status_change" -> Icons.Outlined.EventNote to "Ubah status"
    else            -> Icons.Outlined.EventNote to "Catatan"
}

@Composable
private fun LogActivitySheet(onDismiss: () -> Unit, onSubmit: (type: String, note: String?) -> Unit) {
    val types = listOf(
        "call" to "Telepon",
        "whatsapp" to "WhatsApp",
        "email" to "Email",
        "meeting" to "Pertemuan",
        "note" to "Catatan",
    )
    var selected by remember { mutableStateOf("call") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Aktivitas") },
        text = {
            Column {
                types.forEach { (code, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = selected == code, onClick = { selected = code })
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(selected, note.ifBlank { null }) }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

@Composable
private fun ChangeStatusSheet(currentStatus: String, onDismiss: () -> Unit, onSubmit: (status: String, note: String?) -> Unit) {
    val options = listOf(
        "contacted" to "Sudah dihubungi",
        "qualified" to "Qualified",
        "converted" to "Converted (jadi mahasiswa)",
        "rejected"  to "Rejected",
        "released"  to "Lepas ke team",
    )
    var selected by remember { mutableStateOf(options.first().first) }
    var note by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Status") },
        text = {
            Column {
                Text("Status saat ini: $currentStatus", style = TentuinUniversityTypography.bodyMedium, color = TextMuted)
                Spacer(Modifier.height(8.dp))
                options.forEach { (code, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        RadioButton(selected = selected == code, onClick = { selected = code })
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Catatan (opsional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSubmit(selected, note.ifBlank { null }) }) { Text("Simpan") } },
        dismissButton  = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
