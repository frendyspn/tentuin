package id.tentuin.agent.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun BottomNavBar(navController: NavController) {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Text("D") },
            label = { Text("Dashboard") }
        )
    }
}
