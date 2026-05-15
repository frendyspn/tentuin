package id.tentuin.schoolpic.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import id.tentuin.schoolpic.ui.screen.auth.LoginScreen
import id.tentuin.schoolpic.ui.screen.auth.RegisterScreen
import id.tentuin.schoolpic.ui.screen.commission.CommissionScreen
import id.tentuin.schoolpic.ui.screen.school.SchoolProfileScreen
import id.tentuin.schoolpic.ui.screen.splash.SplashScreen
import id.tentuin.schoolpic.ui.screen.student.StudentListScreen

@Composable
fun SchoolPicNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Route.Splash.route,
        modifier = modifier,
    ) {
        composable(Route.Splash.route)        { SplashScreen(navController) }
        composable(Route.Login.route)         { LoginScreen(navController) }
        composable(Route.Register.route)      { RegisterScreen(navController) }
        composable(Route.Commission.route)    { CommissionScreen(navController) }
        composable(Route.Students.route)      { StudentListScreen(navController) }
        composable(Route.SchoolProfile.route) { SchoolProfileScreen(navController) }
    }
}
