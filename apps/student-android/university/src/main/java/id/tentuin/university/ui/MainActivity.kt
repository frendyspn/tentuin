package id.tentuin.university.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import id.tentuin.university.ui.navigation.UniversityNavHost
import id.tentuin.university.ui.navigation.BottomNavBar
import id.tentuin.university.ui.navigation.bottomNavRoutes
import id.tentuin.university.ui.theme.TentuinUniversityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TentuinUniversityTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (bottomNavRoutes.any { currentRoute?.startsWith(it) == true }) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        UniversityNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
