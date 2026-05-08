package id.tentuin.agent.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.tentuin.agent.ui.theme.*

@Composable
fun ProfileScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(Background).padding(16.dp)) {
        Text("Profile Agen", style = TentuinAgentTypography.headlineMedium, color = TextPrimary)
    }
}
