package id.tentuin.admin.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.repository.AuthRepository
import id.tentuin.admin.ui.component.TentuinButton
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Error
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val fullName = authRepository.fullName
    val role     = authRepository.role
    val userId   = authRepository.userId

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val name by viewModel.fullName.collectAsState(initial = null)
    val role by viewModel.role.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(24.dp),
    ) {
        Text("Profil Admin", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(24.dp))

        ProfileRow(label = "Nama",  value = name ?: "-")
        Spacer(Modifier.height(12.dp))
        ProfileRow(label = "Role",  value = role ?: "-")

        Spacer(Modifier.height(40.dp))
        TentuinButton(
            text = "Logout",
            onClick = {
                viewModel.logout {
                    navController.navigate(Route.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = Error,
        )
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Column {
        Text(label, style = TentuinAdminTypography.labelMedium, color = TextMuted)
        Spacer(Modifier.height(2.dp))
        Text(value, style = TentuinAdminTypography.titleMedium, color = TextPrimary)
    }
}
