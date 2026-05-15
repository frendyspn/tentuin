package id.tentuin.agent.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.tentuin.agent.ui.screen.auth.LoginScreen
import id.tentuin.agent.ui.screen.auth.RegisterScreen
import id.tentuin.agent.ui.screen.claim.ClaimSchoolScreen
import id.tentuin.agent.ui.screen.claim.ClaimUniversityScreen
import id.tentuin.agent.ui.screen.commission.CommissionScreen
import id.tentuin.agent.ui.screen.dashboard.DashboardScreen
import id.tentuin.agent.ui.screen.activity.ActivityScreen
import id.tentuin.agent.ui.screen.portfolio.PortfolioScreen
import id.tentuin.agent.ui.screen.profile.ProfileScreen
import id.tentuin.agent.ui.screen.withdrawal.WithdrawalScreen

import id.tentuin.agent.ui.screen.splash.SplashScreen

@Composable
fun AgentNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier,
    ) {
        composable(Route.Splash.route) { SplashScreen(navController = navController) }
        composable(Route.Login.route) { LoginScreen(navController = navController) }
        composable(Route.Register.route) { RegisterScreen(navController = navController) }
        composable(Route.Dashboard.route) { DashboardScreen(navController = navController) }
        composable(Route.Activity.route) { ActivityScreen(navController = navController) }
        composable(Route.ClaimSchool.route) { ClaimSchoolScreen(navController = navController) }
        composable(Route.ClaimUniversity.route) { ClaimUniversityScreen(navController = navController) }
        composable(Route.Portfolio.route) { PortfolioScreen(navController = navController) }
        composable(Route.Commission.route) { CommissionScreen(navController = navController) }
        composable(Route.Withdrawal.route) { WithdrawalScreen(navController = navController) }
        composable(Route.Profile.route) { ProfileScreen(navController = navController) }
    }
}
