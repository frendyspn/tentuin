package id.tentuin.admin.ui.navigation

sealed class Route(val route: String) {
    object Splash             : Route("splash")
    object Login              : Route("auth/login")

    object Dashboard          : Route("main/dashboard")
    object WithdrawalList     : Route("main/withdrawal")
    object WithdrawalDetail   : Route("main/withdrawal/{id}") {
        fun build(id: String) = "main/withdrawal/$id"
    }

    object AgentList          : Route("main/agent")
    object AgentDetail        : Route("main/agent/{id}") {
        fun build(id: String) = "main/agent/$id"
    }

    object More               : Route("main/more")
    object Profile            : Route("main/profile")

    // ── Sub-screens di balik "More"
    object SchoolList         : Route("main/more/school")
    object SchoolDetail       : Route("main/more/school/{id}") {
        fun build(id: String) = "main/more/school/$id"
    }
    object UniversityList     : Route("main/more/university")
    object UniversityDetail   : Route("main/more/university/{id}") {
        fun build(id: String) = "main/more/university/$id"
    }
    object Report             : Route("main/more/report")
    object Activity           : Route("main/more/activity")
}

val bottomNavRoutes = listOf(
    Route.Dashboard.route,
    Route.WithdrawalList.route,
    Route.AgentList.route,
    Route.More.route,
    Route.Profile.route,
)
