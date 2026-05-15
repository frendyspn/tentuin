package id.tentuin.university.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.*

@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            val target = if (state.needsAccountSetup) Route.AccountSetup.route else Route.Dashboard.route
            navController.navigate(target) { popUpTo(Route.Login.route) { inclusive = true } }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().background(Background)
                .statusBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
        ) {
            Spacer(Modifier.height(48.dp))
            Text("Tentuin Universitas", style = TentuinUniversityTypography.headlineMedium, color = Primary)
            Text("Masuk ke akun universitas Anda", style = TentuinUniversityTypography.bodyMedium, color = TextSub)

            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearError() },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearError() },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, contentDescription = "Toggle password")
                    }
                },
            )
            Spacer(Modifier.height(24.dp))
            TentuinButton("Masuk", { viewModel.login(email, password) }, isLoading = state.isLoading, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { navController.navigate(Route.Register.route) }, modifier = Modifier.fillMaxWidth()) {
                Text("Belum punya akun? Daftar sekarang")
            }
        }
        if (state.error != null) {
            TentuinToast(state.error!!, ToastType.ERROR, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
}
