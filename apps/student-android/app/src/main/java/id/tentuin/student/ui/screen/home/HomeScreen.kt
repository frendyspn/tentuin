package id.tentuin.student.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.component.SkeletonBox
import id.tentuin.student.ui.component.UniversityCard
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.PrimaryLight
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val greeting = if (state.isGuest) "Hai, Tamu!" else "Hai, ${state.profile?.fullName?.split(" ")?.firstOrNull() ?: ""}!"

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        // Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
            ) {
                Text(text = greeting, style = TentuinTypography.headlineMedium, color = TextPrimary)
                Text(text = "Temukan jurusan yang tepat untukmu", style = TentuinTypography.bodyMedium, color = TextSub)
            }
        }

        // CTA card
        item {
            Spacer(Modifier.height(16.dp))
            CtaCard(
                hasResult = state.lastTestResult != null,
                riasecCode = state.lastTestResult?.riasecCode,
                onClick = {
                    if (state.lastTestResult != null) {
                        navController.navigate(
                            Route.TestResult.createRoute(state.lastTestResult!!.riasecCode, isHistorical = true)
                        )
                    } else {
                        navController.navigate(Route.Test.route)
                    }
                },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        // Quick actions
        item {
            Spacer(Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                QuickAction(
                    icon = Icons.Default.Explore,
                    label = "Jelajah Kampus",
                    onClick = { navController.navigate(Route.Explore.route) },
                    modifier = Modifier.weight(1f),
                )
                if (!state.isGuest) {
                    QuickAction(
                        icon = Icons.Default.History,
                        label = "Riwayat Test",
                        onClick = { navController.navigate(Route.TestHistory.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Featured universities
        item {
            Spacer(Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp),
            ) {
                Text(text = "Kampus Rekomendasi", style = TentuinTypography.titleLarge, color = TextPrimary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { navController.navigate(Route.Explore.route) },
                ) {
                    Text(text = "Lihat semua", style = TentuinTypography.labelMedium, color = Primary)
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (state.isLoading) {
            items(3) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
        } else {
            items(state.featuredUniversities, key = { it.id }) { uni ->
                UniversityCard(
                    university = uni,
                    onClick = { navController.navigate(Route.UniversityDetail.createRoute(uni.id)) },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun CtaCard(
    hasResult: Boolean,
    riasecCode: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (hasResult) "Hasil Testmu" else "Mulai Test RIASEC",
                    style = TentuinTypography.titleLarge,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (hasResult && riasecCode != null) "Kode: $riasecCode • Ketuk untuk lihat detail"
                           else "60 soal · ~20 menit · Gratis",
                    style = TentuinTypography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryLight),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = TentuinTypography.labelMedium, color = Primary)
        }
    }
}
