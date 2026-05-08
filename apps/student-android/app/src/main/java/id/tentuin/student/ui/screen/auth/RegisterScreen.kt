package id.tentuin.student.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.component.TentuinTextButton
import id.tentuin.student.ui.component.TentuinTextField
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Error
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub
import id.tentuin.student.ui.theme.TentuinTypography

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    var fullName by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navController.navigate(Route.Home.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
        }

        Spacer(Modifier.height(16.dp))

        Text(text = "Buat Akun", style = TentuinTypography.displayMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(text = "Daftar untuk menyimpan hasil test dan favoritmu", style = TentuinTypography.bodyMedium, color = TextSub)

        Spacer(Modifier.height(32.dp))

        TentuinTextField(
            value = fullName,
            onValueChange = { fullName = it; viewModel.clearError() },
            label = "Nama Lengkap",
            placeholder = "Masukkan nama lengkapmu",
        )

        Spacer(Modifier.height(16.dp))

        TentuinTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = "Email",
            placeholder = "contoh@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        Spacer(Modifier.height(16.dp))

        TentuinTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = "Password",
            placeholder = "Minimal 8 karakter",
            isPassword = true,
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(text = uiState.error!!, style = TentuinTypography.bodySmall, color = Error)
        }

        Spacer(Modifier.height(24.dp))

        TentuinButton(
            text = "Daftar",
            onClick = { viewModel.register(email, password, fullName) },
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Sudah punya akun? ", style = TentuinTypography.bodyMedium, color = TextSub)
            TentuinTextButton(text = "Masuk", onClick = { navController.popBackStack() })
        }

        Spacer(Modifier.height(24.dp))
    }
}
