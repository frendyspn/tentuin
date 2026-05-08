package id.tentuin.student.ui.screen.auth

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.BuildConfig
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.component.TentuinOutlinedButton
import id.tentuin.student.ui.component.TentuinTextButton
import id.tentuin.student.ui.component.TentuinTextField
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    sessionDataStore: SessionDataStore? = null 
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Observe session for OAuth success
    val sessionToken by viewModel.accessToken.collectAsState(initial = null)

    LaunchedEffect(uiState.isSuccess, sessionToken) {
        if (uiState.isSuccess || sessionToken != null) {
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
        Spacer(Modifier.height(48.dp))

        Text(text = "Selamat Datang", style = TentuinTypography.displayMedium, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(text = "Masuk untuk melanjutkan perjalananmu", style = TentuinTypography.bodyMedium, color = TextSub)

        Spacer(Modifier.height(40.dp))

        TentuinTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError() },
            label = "Email",
            placeholder = "contoh@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            error = if (uiState.error?.contains("email", ignoreCase = true) == true) uiState.error else null,
        )

        Spacer(Modifier.height(16.dp))

        TentuinTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError() },
            label = "Password",
            placeholder = "Minimal 8 karakter",
            isPassword = true,
            error = if (uiState.error?.contains("password", ignoreCase = true) == true) uiState.error else null,
        )

        if (uiState.error != null &&
            !uiState.error!!.contains("email", ignoreCase = true) &&
            !uiState.error!!.contains("password", ignoreCase = true)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(text = uiState.error!!, style = TentuinTypography.bodySmall, color = id.tentuin.student.ui.theme.Error)
        }

        Spacer(Modifier.height(24.dp))

        TentuinButton(
            text = "Masuk",
            onClick = { viewModel.login(email, password) },
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
            Text(
                text = "  atau  ",
                style = TentuinTypography.bodySmall,
                color = TextMuted,
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
        }

        Spacer(Modifier.height(16.dp))

        TentuinOutlinedButton(
            text = "Masuk dengan Google",
            onClick = {
                val supabaseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
                val callbackUrl = "id.tentuin.student://auth/callback"
                val oauthUrl = "$supabaseUrl/auth/v1/authorize" +
                    "?provider=google" +
                    "&redirect_to=${Uri.encode(callbackUrl)}"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(oauthUrl)))
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Belum punya akun? ",
                style = TentuinTypography.bodyMedium,
                color = TextSub,
            )
            TentuinTextButton(
                text = "Daftar",
                onClick = { navController.navigate(Route.Register.route) },
            )
        }

        Spacer(Modifier.height(16.dp))

        TentuinTextButton(
            text = "Lanjut tanpa akun →",
            onClick = {
                navController.navigate(Route.Home.route) {
                    popUpTo(Route.Login.route) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            color = TextMuted,
        )

        Spacer(Modifier.height(24.dp))
    }
}
