package id.tentuin.student.ui.screen.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.ui.theme.riasecColor
import id.tentuin.student.core.util.riasecTypeName
import id.tentuin.student.ui.component.RiasecChip
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextMuted
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

private val riasecCodes = listOf("R", "I", "A", "S", "E", "C")

@Composable
fun TestEntryScreen(
    navController: NavController,
    viewModel: TestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface)
                    .statusBarsPadding()
                    .padding(20.dp),
            ) {
                Text(text = "Test RIASEC", style = TentuinTypography.headlineMedium, color = TextPrimary)
                Text(text = "Kenali tipe kepribadianmu", style = TentuinTypography.bodyMedium, color = TextSub)
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            // Info row
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    InfoItem(icon = Icons.Default.Quiz, label = "60 Soal")
                    InfoItem(icon = Icons.Default.AccessTime, label = "~20 Menit")
                    InfoItem(icon = Icons.Default.CheckCircle, label = "Gratis")
                }
            }
        }

        item {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "6 Tipe Kepribadian RIASEC",
                style = TentuinTypography.titleLarge,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        items(count = riasecCodes.size) { i ->
            val code = riasecCodes[i]
            RiasecTypeCard(code = code, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
        }

        item {
            Spacer(Modifier.height(24.dp))
            TentuinButton(
                text = "Mulai Test",
                onClick = {
                    if (!viewModel.canStartTest()) {
                        navController.navigate(Route.Login.route)
                    } else {
                        viewModel.resetTest()
                        navController.navigate(Route.TestSession.route)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
            )
            if (state.isGuest) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "* Masuk diperlukan untuk memulai test",
                    style = TentuinTypography.bodySmall,
                    color = id.tentuin.student.ui.theme.Error,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = TentuinTypography.labelMedium, color = TextSub)
    }
}

@Composable
private fun RiasecTypeCard(code: String, modifier: Modifier = Modifier) {
    val color = riasecColor(code)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.07f)),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp),
        ) {
            RiasecChip(code = code)
            Spacer(Modifier.size(12.dp))
            Text(text = riasecTypeName(code), style = TentuinTypography.titleSmall, color = TextPrimary)
        }
    }
}
