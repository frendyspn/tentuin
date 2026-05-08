package id.tentuin.agent.ui.screen.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.tentuin.agent.ui.navigation.Route
import id.tentuin.agent.ui.theme.*

data class ActivityMenuItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ActivityScreen(navController: NavController) {
    val menuItems = listOf(
        ActivityMenuItem("Klaim Sekolah", Icons.Default.School, Route.ClaimSchool.route),
        ActivityMenuItem("Klaim Kampus", Icons.Default.AccountBalance, Route.ClaimUniversity.route),
        ActivityMenuItem("Portfolio", Icons.Default.Folder, Route.Portfolio.route),
        ActivityMenuItem("Komisi", Icons.Default.Payments, Route.Commission.route),
        ActivityMenuItem("Tarik Komisi", Icons.Default.Payments, Route.Withdrawal.route)
    )

    Column(modifier = Modifier.fillMaxSize().background(Background)) {
        Surface(shadowElevation = 2.dp) {
            Text(
                "Aktivitas Agen", 
                style = TentuinAgentTypography.headlineMedium, 
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            )
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(menuItems) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { navController.navigate(item.route) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface)
                ) {
                    ListItem(
                        headlineContent = { Text(item.title, style = TentuinAgentTypography.titleMedium) },
                        leadingContent = { Icon(item.icon, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = Surface)
                    )
                }
            }
        }
    }
}
