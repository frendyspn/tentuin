package id.tentuin.student.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.tentuin.student.ui.navigation.BottomNavBar
import id.tentuin.student.ui.navigation.TentuinNavHost
import id.tentuin.student.ui.theme.TentuinTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TentuinTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Only show bottom bar on main screens
                        if (shouldShowBottomBar(currentRoute)) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    TentuinNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun shouldShowBottomBar(route: String?): Boolean {
        if (route == null) return false
        val mainRoutes = listOf("home", "test", "explore", "profile")
        return mainRoutes.any { route.startsWith(it) }
    }
}
