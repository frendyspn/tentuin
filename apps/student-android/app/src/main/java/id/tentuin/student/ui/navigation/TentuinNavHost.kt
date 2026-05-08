package id.tentuin.student.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.tentuin.student.ui.screen.auth.LoginScreen
import id.tentuin.student.ui.screen.auth.RegisterScreen
import id.tentuin.student.ui.screen.explore.ExploreScreen
import id.tentuin.student.ui.screen.home.HomeScreen
import id.tentuin.student.ui.screen.onboarding.OnboardingScreen
import id.tentuin.student.ui.screen.profile.ProfileScreen
import id.tentuin.student.ui.screen.splash.SplashScreen
import id.tentuin.student.ui.screen.test.TestEntryScreen
import id.tentuin.student.ui.screen.test.TestSessionScreen

@Composable
fun TentuinNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier
    ) {
        composable(Route.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Route.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        
        // Auth
        composable(Route.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Route.Register.route) {
            RegisterScreen(navController = navController)
        }

        // Main Tabs
        composable(Route.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Route.Test.route) {
            TestEntryScreen(navController = navController)
        }
        composable(Route.Explore.route) {
            ExploreScreen(navController = navController)
        }
        composable(Route.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // Test Flow
        composable(Route.TestSession.route) {
            TestSessionScreen(navController = navController)
        }
        // TODO: Add TestResult, TestHistory, Details, etc.
    }
}
