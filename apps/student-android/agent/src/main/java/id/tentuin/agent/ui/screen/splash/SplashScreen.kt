package id.tentuin.agent.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.ui.navigation.Route
import id.tentuin.agent.ui.theme.Background
import id.tentuin.agent.ui.theme.Primary
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : ViewModel() {
    suspend fun getStartDestination(): String {
        val token = sessionDataStore.accessToken.first()
        return if (token != null) Route.Dashboard.route else Route.Login.route
    }
}

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        val destination = viewModel.getStartDestination()
        navController.navigate(destination) {
            popUpTo(0) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}
