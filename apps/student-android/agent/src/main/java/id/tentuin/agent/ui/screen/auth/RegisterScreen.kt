package id.tentuin.agent.ui.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RegisterScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daftar Agen", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { navController.popBackStack() }) {
            Text("Kembali")
        }
    }
}
