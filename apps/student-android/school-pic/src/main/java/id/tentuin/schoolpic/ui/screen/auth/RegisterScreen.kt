package id.tentuin.schoolpic.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.schoolpic.ui.component.ErrorBanner
import id.tentuin.schoolpic.ui.component.InfoBanner
import id.tentuin.schoolpic.ui.component.PrimaryButton
import id.tentuin.schoolpic.ui.navigation.Route
import id.tentuin.schoolpic.ui.theme.Background
import id.tentuin.schoolpic.ui.theme.Primary
import id.tentuin.schoolpic.ui.theme.TentuinSchoolPicTypography
import id.tentuin.schoolpic.ui.theme.TextSub

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var fullName  by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var claimCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            navController.navigate(Route.Commission.route) {
                popUpTo(Route.Login.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
            }
        }

        Spacer(Modifier.height(8.dp))
        Text("Daftar PIC Sekolah", style = TentuinSchoolPicTypography.headlineMedium, color = Primary)
        Text(
            "Masukkan kode registrasi yang diberikan agen Tentuin saat klaim sekolah Anda.",
            style = TentuinSchoolPicTypography.bodyMedium,
            color = TextSub,
        )

        Spacer(Modifier.height(24.dp))

        if (state.error != null) {
            ErrorBanner(state.error!!)
            Spacer(Modifier.height(12.dp))
        }
        if (state.info != null && state.error == null) {
            InfoBanner(state.info!!)
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearError() },
            label = { Text("Nama lengkap") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = { Text("Password (min. 6 karakter)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(icon, contentDescription = "Toggle password")
                }
            },
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = claimCode,
            onValueChange = { claimCode = it.uppercase(); viewModel.clearError() },
            label = { Text("Kode registrasi (mis. TNT-XXXXXX)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Daftar",
            onClick = { viewModel.register(fullName, email, password, claimCode) },
            isLoading = state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
