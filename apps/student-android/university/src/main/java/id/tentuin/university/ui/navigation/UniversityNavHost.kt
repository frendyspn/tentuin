package id.tentuin.university.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import id.tentuin.university.ui.screen.auth.LoginScreen
import id.tentuin.university.ui.screen.auth.RegisterScreen
import id.tentuin.university.ui.screen.dashboard.DashboardScreen
import id.tentuin.university.ui.screen.followup.FollowupScreen
import id.tentuin.university.ui.screen.profile.ProfileScreen
import id.tentuin.university.ui.screen.prospect.ProspectsScreen
import id.tentuin.university.ui.screen.setup.AccountSetupScreen
import id.tentuin.university.ui.screen.splash.SplashScreen
import id.tentuin.university.ui.screen.subscribe.SubscribeScreen
import id.tentuin.university.ui.screen.team.AddMemberScreen
import id.tentuin.university.ui.screen.team.TeamScreen

@Composable
fun UniversityNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier,
    ) {
        composable(Route.Splash.route)       { SplashScreen(navController) }
        composable(Route.Login.route)        { LoginScreen(navController) }
        composable(Route.Register.route)     { RegisterScreen(navController) }
        composable(Route.AccountSetup.route) { AccountSetupScreen(navController) }

        composable(Route.Dashboard.route)    { DashboardScreen(navController) }
        composable(Route.Prospects.route)    { ProspectsScreen(navController) }
        composable(Route.Team.route)         { TeamScreen(navController) }
        composable(Route.Profile.route)      { ProfileScreen(navController) }

        composable(Route.Subscribe.route)    { SubscribeScreen(navController) }
        composable(Route.AddMember.route)    { AddMemberScreen(navController) }

        composable(
            route = Route.Followup.route,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { FollowupScreen(navController) }
    }
}
