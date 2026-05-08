package id.tentuin.agent.ui.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.tentuin.agent.ui.theme.*

@Composable
fun ActivityScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(Background).padding(16.dp)) {
        Text("Aktivitas", style = TentuinAgentTypography.headlineMedium, color = TextPrimary)
    }
}
