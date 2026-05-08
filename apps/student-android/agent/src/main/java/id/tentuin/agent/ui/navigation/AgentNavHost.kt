package id.tentuin.agent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.tentuin.agent.ui.screen.auth.LoginScreen
import id.tentuin.agent.ui.screen.auth.RegisterScreen
import id.tentuin.agent.ui.screen.dashboard.DashboardScreen

@Composable
fun AgentNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Login.route,
        modifier = modifier,
    ) {
        composable(Route.Login.route) { LoginScreen(navController = navController) }
        composable(Route.Register.route) { RegisterScreen(navController = navController) }
        composable(Route.Dashboard.route) { DashboardScreen(navController = navController) }
    }
}
