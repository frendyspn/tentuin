package id.tentuin.student.ui.navigation

sealed class Route(val route: String) {
    object Splash      : Route("splash")
    object Onboarding  : Route("onboarding")

    // Auth
    object Login       : Route("auth/login")
    object Register    : Route("auth/register")

    // Main tabs
    object Home        : Route("main/home")
    object Test        : Route("main/test")
    object Explore     : Route("main/explore?riasecCode={riasecCode}") {
        fun createRoute(riasecCode: String? = null) = 
            if (riasecCode != null) "main/explore?riasecCode=$riasecCode" else "main/explore"
    }
    object Profile     : Route("main/profile")

    // Test flow
    object TestSession : Route("main/test/session")
    object TestResult  : Route("main/test/result?riasecCode={riasecCode}&isHistorical={isHistorical}") {
        fun createRoute(riasecCode: String, isHistorical: Boolean = false) =
            "main/test/result?riasecCode=$riasecCode&isHistorical=$isHistorical"
    }
    object TestHistory : Route("main/test/history")

    // Explore detail
    object UniversityDetail : Route("main/explore/university/{id}") {
        fun createRoute(id: String) = "main/explore/university/$id"
    }

    // Profile sub-screens
    object EditProfile : Route("main/profile/edit")
    object Bookmarks   : Route("main/profile/bookmarks")
}

val bottomNavRoutes = listOf(Route.Home.route, Route.Test.route, Route.Explore.route, Route.Profile.route)
