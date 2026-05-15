package id.tentuin.schoolpic.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import id.tentuin.schoolpic.ui.theme.Primary
import id.tentuin.schoolpic.ui.theme.Surface
import id.tentuin.schoolpic.ui.theme.TentuinSchoolPicTypography
import id.tentuin.schoolpic.ui.theme.TextMuted

private data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val items = listOf(
    NavItem(Route.Commission.route,    "Komisi", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance),
    NavItem(Route.Students.route,      "Siswa",  Icons.Filled.Group,          Icons.Outlined.Group),
    NavItem(Route.SchoolProfile.route, "Sekolah", Icons.Filled.School,         Icons.Outlined.School),
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
                        popUpTo(Route.Commission.route) { saveState = true }
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
                label = { Text(item.label, style = TentuinSchoolPicTypography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                ),
            )
        }
    }
}
