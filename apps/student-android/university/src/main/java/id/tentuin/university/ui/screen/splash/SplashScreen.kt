package id.tentuin.university.ui.screen.splash

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
import id.tentuin.university.core.datastore.SessionDataStore
import id.tentuin.university.data.repository.AccountRepository
import id.tentuin.university.ui.navigation.Route
import id.tentuin.university.ui.theme.Background
import id.tentuin.university.ui.theme.Primary
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val session:    SessionDataStore,
    private val accountRepo: AccountRepository,
) : ViewModel() {
    suspend fun getStartDestination(): String {
        val token = session.accessToken.first() ?: return Route.Login.route
        // Cek apakah user sudah punya account aktif
        val acc = accountRepo.getMyAccount().getOrNull()
        return if (acc != null) Route.Dashboard.route else Route.AccountSetup.route
    }
}

@Composable
fun SplashScreen(navController: NavController, viewModel: SplashViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        val destination = viewModel.getStartDestination()
        navController.navigate(destination) { popUpTo(0) { inclusive = true } }
    }
    Box(modifier = Modifier.fillMaxSize().background(Background), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Primary)
    }
}
