package id.tentuin.student.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import androidx.compose.ui.unit.dp
import id.tentuin.student.ui.theme.TextMuted

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val navItems = listOf(
    BottomNavItem(Route.Home.route,    "Beranda",  Icons.Filled.Home,      Icons.Outlined.Home),
    BottomNavItem(Route.Test.route,    "Test",     Icons.Filled.Psychology, Icons.Outlined.Psychology),
    BottomNavItem("main/explore",      "Jelajah",  Icons.Filled.Explore,   Icons.Outlined.Explore), // Use base route
    BottomNavItem(Route.Profile.route, "Profil",   Icons.Filled.Person,    Icons.Outlined.Person),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backstackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backstackEntry?.destination?.route

    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp,
    ) {
        navItems.forEach { item ->
            // Match the base route so parameters (like ?riasecCode=) don't break selection
            val selected = currentRoute?.startsWith(item.route.substringBefore("?")) == true

            NavigationBarItem(
                selected  = selected,
                onClick   = {
                    navController.navigate(item.route) {
                        popUpTo(Route.Home.route) { 
                            saveState = true 
                        }
                        launchSingleTop = true
                        // Jangan restore state jika kembali ke Beranda agar tumpukan di atasnya (seperti Riwayat) benar-benar di-clear
                        restoreState = (item.route != Route.Home.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(text = item.label, style = TentuinTypography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Primary,
                    selectedTextColor   = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor      = Primary.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
