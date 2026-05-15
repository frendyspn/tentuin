package id.tentuin.admin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.tentuin.admin.ui.screen.agent.AgentDetailScreen
import id.tentuin.admin.ui.screen.agent.AgentListScreen
import id.tentuin.admin.ui.screen.auth.LoginScreen
import id.tentuin.admin.ui.screen.dashboard.DashboardScreen
import id.tentuin.admin.ui.screen.activity.ActivityFeedScreen
import id.tentuin.admin.ui.screen.more.MoreScreen
import id.tentuin.admin.ui.screen.profile.ProfileScreen
import id.tentuin.admin.ui.screen.report.CommissionReportScreen
import id.tentuin.admin.ui.screen.school.SchoolDetailScreen
import id.tentuin.admin.ui.screen.school.SchoolListScreen
import id.tentuin.admin.ui.screen.university.UniversityDetailScreen
import id.tentuin.admin.ui.screen.university.UniversityListScreen
import id.tentuin.admin.ui.screen.splash.SplashScreen
import id.tentuin.admin.ui.screen.withdrawal.WithdrawalDetailScreen
import id.tentuin.admin.ui.screen.withdrawal.WithdrawalListScreen

@Composable
fun AdminNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController    = navController,
        startDestination = Route.Splash.route,
        modifier         = modifier,
    ) {
        composable(Route.Splash.route)          { SplashScreen(navController) }
        composable(Route.Login.route)           { LoginScreen(navController) }
        composable(Route.Dashboard.route)       { DashboardScreen(navController) }

        composable(Route.WithdrawalList.route)  { WithdrawalListScreen(navController) }
        composable(
            route = Route.WithdrawalDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            WithdrawalDetailScreen(navController, id)
        }

        composable(Route.AgentList.route)       { AgentListScreen(navController) }
        composable(
            route = Route.AgentDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            AgentDetailScreen(navController, id)
        }

        composable(Route.More.route)            { MoreScreen(navController) }
        composable(Route.Profile.route)         { ProfileScreen(navController) }

        composable(Route.SchoolList.route)      { SchoolListScreen(navController) }
        composable(
            route = Route.SchoolDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            SchoolDetailScreen(navController, id)
        }
        composable(Route.UniversityList.route)  { UniversityListScreen(navController) }
        composable(
            route = Route.UniversityDetail.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            UniversityDetailScreen(navController, id)
        }
        composable(Route.Report.route)          { CommissionReportScreen(navController) }
        composable(Route.Activity.route)        { ActivityFeedScreen(navController) }
    }
}
