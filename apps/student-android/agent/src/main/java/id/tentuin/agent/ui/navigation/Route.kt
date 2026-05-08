package id.tentuin.agent.ui.navigation

sealed class Route(val route: String) {
    object Login              : Route("auth/login")
    object Register           : Route("auth/register")
    object Dashboard          : Route("main/dashboard")
}

val bottomNavRoutes = listOf(
    Route.Dashboard.route,
)
