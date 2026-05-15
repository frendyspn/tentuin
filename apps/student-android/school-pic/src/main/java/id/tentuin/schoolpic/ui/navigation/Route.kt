package id.tentuin.schoolpic.ui.navigation

sealed class Route(val route: String) {
    object Splash         : Route("splash")
    object Login          : Route("auth/login")
    object Register       : Route("auth/register")
    object Commission     : Route("main/commission")
    object Students       : Route("main/students")
    object SchoolProfile  : Route("main/school-profile")
}

val bottomNavRoutes = listOf(
    Route.Commission.route,
    Route.Students.route,
    Route.SchoolProfile.route,
)
