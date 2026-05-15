package id.tentuin.schoolpic.ui.screen.school

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import id.tentuin.schoolpic.data.model.School
import id.tentuin.schoolpic.ui.component.ErrorBanner
import id.tentuin.schoolpic.ui.component.InfoBanner
import id.tentuin.schoolpic.ui.component.PrimaryButton
import id.tentuin.schoolpic.ui.navigation.Route
import id.tentuin.schoolpic.ui.theme.*

@Composable
fun SchoolProfileScreen(
    navController: NavController,
    viewModel: SchoolProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            navController.navigate(Route.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Surface(color = Primary, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Profil Sekolah", color = Color.White, style = TentuinSchoolPicTypography.headlineMedium)
                Text(
                    "Kelola informasi sekolah Anda",
                    color = Color.White.copy(alpha = 0.85f),
                    style = TentuinSchoolPicTypography.bodyMedium,
                )
            }
        }

        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(color = Primary) }

            state.school == null -> Box(
                Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center,
            ) {
                Text(state.error ?: "Data sekolah tidak tersedia.", color = TextSub)
            }

            else -> EditForm(state, viewModel)
        }
    }
}

@Composable
private fun EditForm(state: SchoolProfileUiState, viewModel: SchoolProfileViewModel) {
    val school = state.school!!
    var name    by remember(school.id) { mutableStateOf(school.name) }
    var address by remember(school.id) { mutableStateOf(school.address.orEmpty()) }
    var email   by remember(school.id) { mutableStateOf(school.email.orEmpty()) }
    var phone   by remember(school.id) { mutableStateOf(school.phone.orEmpty()) }
    var logoUrl by remember(school.id) { mutableStateOf(school.logoUrl.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        SchoolHeader(school)

        Spacer(Modifier.height(16.dp))

        if (state.error != null) {
            ErrorBanner(state.error)
            Spacer(Modifier.height(8.dp))
        }
        if (state.info != null && state.error == null) {
            InfoBanner(state.info)
            Spacer(Modifier.height(8.dp))
        }

        ReadOnlyRow("NPSN", school.npsn ?: "-")
        ReadOnlyRow("Kota", school.city)
        ReadOnlyRow("Provinsi", school.province)
        ReadOnlyRow("Total siswa", school.totalStudents.toString())

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; viewModel.clearMessages() },
            label = { Text("Nama sekolah") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = address,
            onValueChange = { address = it; viewModel.clearMessages() },
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it; viewModel.clearMessages() },
            label = { Text("Telepon") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = logoUrl,
            onValueChange = { logoUrl = it; viewModel.clearMessages() },
            label = { Text("URL Logo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
        )

        Spacer(Modifier.height(20.dp))

        PrimaryButton(
            text = "Simpan perubahan",
            onClick = { viewModel.save(name, address, email, phone, logoUrl) },
            isLoading = state.isSaving,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { viewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
        ) {
            Text("Keluar")
        }
    }
}

@Composable
private fun SchoolHeader(school: School) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(PrimaryLight),
            contentAlignment = Alignment.Center,
        ) {
            if (school.logoUrl != null) {
                AsyncImage(
                    model = school.logoUrl, contentDescription = null,
                    modifier = Modifier.size(56.dp).clip(CircleShape),
                )
            } else {
                Icon(Icons.Default.School, contentDescription = null, tint = Primary)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(school.name, style = TentuinSchoolPicTypography.titleLarge, color = TextPrimary)
            Text("${school.city}, ${school.province}",
                style = TentuinSchoolPicTypography.bodyMedium, color = TextSub)
        }
    }
}

@Composable
private fun ReadOnlyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f),
            style = TentuinSchoolPicTypography.bodyMedium, color = TextMuted)
        Text(value, style = TentuinSchoolPicTypography.bodyMedium, color = TextPrimary)
    }
}
