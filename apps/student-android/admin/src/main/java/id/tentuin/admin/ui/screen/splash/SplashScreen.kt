package id.tentuin.admin.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.data.repository.AuthRepository
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {
    suspend fun getStartDestination(): String {
        val token = sessionDataStore.accessToken.first()
        val role  = sessionDataStore.role.first()
        val isAdmin = role in AuthRepository.ADMIN_ROLES
        return if (token != null && isAdmin) Route.Dashboard.route else Route.Login.route
    }
}

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        val destination = viewModel.getStartDestination()
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Primary)
    }
}
