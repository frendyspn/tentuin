package id.tentuin.agent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import id.tentuin.agent.ui.theme.Primary
import id.tentuin.agent.ui.theme.TentuinAgentTypography
import id.tentuin.agent.ui.theme.TextMuted
import id.tentuin.agent.ui.theme.Surface

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val items = listOf(
    NavItem(Route.Dashboard.route, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavItem(Route.Activity.route,  "Aktivitas", Icons.AutoMirrored.Filled.List, Icons.AutoMirrored.Outlined.List),
    NavItem(Route.Profile.route,   "Profil",    Icons.Filled.Person,    Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(containerColor = Surface) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(Route.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label, style = TentuinAgentTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
