package id.tentuin.university.ui.navigation

sealed class Route(val route: String) {
    object Splash      : Route("splash")
    object Login       : Route("auth/login")
    object Register    : Route("auth/register")
    object AccountSetup: Route("auth/setup")

    object Dashboard   : Route("main/dashboard")
    object Prospects   : Route("main/prospects")
    object Team        : Route("main/team")
    object Profile     : Route("main/profile")

    object Subscribe   : Route("main/subscribe")
    object Followup    : Route("main/followup/{id}") {
        fun create(id: String) = "main/followup/$id"
    }
    object AddMember   : Route("main/team/add")
}

val bottomNavRoutes = listOf(
    Route.Dashboard.route,
    Route.Prospects.route,
    Route.Team.route,
    Route.Profile.route,
)
