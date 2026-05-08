package id.tentuin.agent.ui.navigation

sealed class Route(val route: String) {
    object Splash             : Route("splash")
    object Login              : Route("auth/login")
    object Register           : Route("auth/register")
    object Dashboard          : Route("main/dashboard")
    object Activity           : Route("main/activity")
    object ClaimSchool        : Route("main/activity/claim-school")
    object ClaimUniversity    : Route("main/activity/claim-university")
    object Portfolio          : Route("main/activity/portfolio")
    object Commission         : Route("main/activity/commission")
    object Withdrawal         : Route("main/activity/withdrawal")
    object Profile            : Route("main/profile")
}

val bottomNavRoutes = listOf(
    Route.Dashboard.route,
    Route.Activity.route,
    Route.Profile.route,
)
