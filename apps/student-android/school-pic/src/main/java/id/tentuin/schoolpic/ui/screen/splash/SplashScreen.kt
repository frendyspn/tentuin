package id.tentuin.schoolpic.ui.screen.splash

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
import id.tentuin.schoolpic.core.datastore.SessionDataStore
import id.tentuin.schoolpic.data.repository.AuthRepository
import id.tentuin.schoolpic.ui.navigation.Route
import id.tentuin.schoolpic.ui.theme.Background
import id.tentuin.schoolpic.ui.theme.Primary
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val authRepository:   AuthRepository,
) : ViewModel() {
    suspend fun getStartDestination(): String {
        val token = sessionDataStore.accessToken.first() ?: return Route.Login.route
        // Pastikan school_id terbaru tersinkron (bisa berubah kalau admin reset).
        val schoolId = runCatching { authRepository.syncSchoolIdFromProfile().getOrNull() }.getOrNull()
        return if (schoolId != null) Route.Commission.route else Route.Login.route
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
