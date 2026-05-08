package id.tentuin.student.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.component.ForceUpdateDialog
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.TentuinTypography

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is SplashState.Navigate) {
            val route = (state as SplashState.Navigate).route
            navController.navigate(route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
    ) {
        Text(
            text = "Tentuin",
            style = TentuinTypography.displayLarge,
            color = Color.White,
        )
    }

    if (state is SplashState.ForceUpdate) {
        ForceUpdateDialog(storeUrl = (state as SplashState.ForceUpdate).storeUrl)
    }
}
