package id.tentuin.student.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.tentuin.student.ui.screen.auth.LoginScreen
import id.tentuin.student.ui.screen.auth.RegisterScreen
import id.tentuin.student.ui.screen.explore.ExploreScreen
import id.tentuin.student.ui.screen.explore.UniversityDetailScreen
import id.tentuin.student.ui.screen.home.HomeScreen
import id.tentuin.student.ui.screen.onboarding.OnboardingScreen
import id.tentuin.student.ui.screen.profile.BookmarksScreen
import id.tentuin.student.ui.screen.profile.EditProfileScreen
import id.tentuin.student.ui.screen.profile.ProfileScreen
import id.tentuin.student.ui.screen.splash.SplashScreen
import id.tentuin.student.ui.screen.test.TestEntryScreen
import id.tentuin.student.ui.screen.test.TestHistoryScreen
import id.tentuin.student.ui.screen.test.TestResultScreen
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
        composable(
            route = Route.Explore.route,
            arguments = listOf(
                androidx.navigation.navArgument("riasecCode") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ExploreScreen(navController = navController)
        }
        composable(
            route = Route.UniversityDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            UniversityDetailScreen(navController = navController, universityId = id)
        }
        composable(Route.Profile.route) {
            ProfileScreen(navController = navController)
        }

        // Test Flow
        composable(Route.TestSession.route) {
            TestSessionScreen(navController = navController)
        }
        composable(
            route = Route.TestResult.route,
            arguments = listOf(
                androidx.navigation.navArgument("riasecCode") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("isHistorical") {
                    type = androidx.navigation.NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val riasecCode = backStackEntry.arguments?.getString("riasecCode") ?: ""
            val isHistorical = backStackEntry.arguments?.getBoolean("isHistorical") ?: false
            TestResultScreen(
                navController = navController,
                riasecCode = riasecCode,
                isHistorical = isHistorical
            )
        }
        composable(Route.TestHistory.route) {
            TestHistoryScreen(navController = navController)
        }

        // Profile sub-screens
        composable(Route.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Route.Bookmarks.route) {
            BookmarksScreen(navController = navController)
        }
    }
}
