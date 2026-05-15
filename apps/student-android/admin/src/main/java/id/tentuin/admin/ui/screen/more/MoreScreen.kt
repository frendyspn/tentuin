package id.tentuin.admin.ui.screen.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.tentuin.admin.ui.navigation.Route
import id.tentuin.admin.ui.theme.Background
import id.tentuin.admin.ui.theme.Primary
import id.tentuin.admin.ui.theme.PrimaryLight
import id.tentuin.admin.ui.theme.Surface
import id.tentuin.admin.ui.theme.TentuinAdminTypography
import id.tentuin.admin.ui.theme.TextMuted
import id.tentuin.admin.ui.theme.TextPrimary

@Composable
fun MoreScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .padding(24.dp),
    ) {
        Text("Lainnya", style = TentuinAdminTypography.headlineMedium, color = TextPrimary)
        Spacer(Modifier.height(24.dp))

        MoreItem("Sekolah",       "Kelola data sekolah & target",   Icons.Default.School,        Route.SchoolList.route, navController)
        Spacer(Modifier.height(12.dp))
        MoreItem("Universitas",   "Subscribe & kuota partner",      Icons.Default.AccountBalance, Route.UniversityList.route, navController)
        Spacer(Modifier.height(12.dp))
        MoreItem("Laporan Komisi","Rekap bulanan stream A & B",     Icons.Default.Insights,       Route.Report.route, navController)
        Spacer(Modifier.height(12.dp))
        MoreItem("Activity Feed", "Audit log & event sistem",       Icons.Default.History,        Route.Activity.route, navController)
    }
}

@Composable
private fun MoreItem(
    title:    String,
    subtitle: String,
    icon:     ImageVector,
    route:    String,
    nav:      NavController,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { nav.navigate(route) },
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryLight, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = Primary)
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = TentuinAdminTypography.titleMedium, color = TextPrimary)
                Text(subtitle, style = TentuinAdminTypography.bodyMedium, color = TextMuted)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMuted)
        }
    }
}
