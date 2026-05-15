package id.tentuin.university.ui.screen.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Person
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
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun AccountSetupScreen(
    navController: NavController,
    viewModel: AccountSetupViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsState()
    var selectedType by remember { mutableStateOf("personal") }
    var displayName by remember { mutableStateOf("") }
    var selectedUniversityId by remember { mutableStateOf<String?>(null) }
    var showUniDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(s.success) {
        if (s.success) {
            navController.navigate(Route.Dashboard.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val selectedUni = s.universities.firstOrNull { it.id == selectedUniversityId }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Background)
                .statusBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text("Pilih Tipe Account", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
            Text("Tentukan bagaimana Anda akan mengakses prospek.", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
            Spacer(Modifier.height(24.dp))

            AccountTypeCard(
                selected = selectedType == "personal",
                title = "Individu",
                description = "Akun pribadi. Beli paket 100 data dengan Rp 100.000.",
                icon = { Icon(Icons.Outlined.Person, null, tint = Primary) },
                onClick = { selectedType = "personal" },
            )
            Spacer(Modifier.height(12.dp))
            AccountTypeCard(
                selected = selectedType == "enterprise",
                title = "Enterprise (Tim)",
                description = "Untuk tim universitas. Paket 5.000 data Rp 5.000.000, bisa undang anggota.",
                icon = { Icon(Icons.Outlined.Business, null, tint = Primary) },
                onClick = { selectedType = "enterprise" },
            )

            Spacer(Modifier.height(24.dp))
            Text("Detail Account", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it; viewModel.clearError() },
                label = { Text(if (selectedType == "enterprise") "Nama Tim / Universitas" else "Nama Account") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))

            // University picker (opsional)
            ExposedDropdownMenuBox(
                expanded = showUniDropdown,
                onExpandedChange = { showUniDropdown = it },
            ) {
                OutlinedTextField(
                    value = selectedUni?.name ?: "(opsional — bisa diisi nanti)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Wakili universitas") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUniDropdown) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = showUniDropdown,
                    onDismissRequest = { showUniDropdown = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("(tidak terkait universitas tertentu)") },
                        onClick = { selectedUniversityId = null; showUniDropdown = false },
                    )
                    s.universities.forEach { uni ->
                        DropdownMenuItem(
                            text = { Text("${uni.name}${uni.city?.let { " — $it" } ?: ""}") },
                            onClick = { selectedUniversityId = uni.id; showUniDropdown = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            TentuinButton(
                text = "Buat Account",
                onClick = { viewModel.submit(selectedType, displayName, selectedUniversityId) },
                isLoading = s.submitting,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
        }
        if (s.error != null) {
            TentuinToast(s.error!!, ToastType.ERROR, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun AccountTypeCard(
    selected: Boolean,
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) Primary else Border
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (selected) PrimaryLight else Surface, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(description, style = TentuinUniversityTypography.bodyMedium, color = TextSub)
        }
        RadioButton(selected = selected, onClick = onClick)
    }
}
