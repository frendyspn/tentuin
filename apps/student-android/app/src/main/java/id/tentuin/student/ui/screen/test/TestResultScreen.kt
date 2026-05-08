package id.tentuin.student.ui.screen.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import id.tentuin.student.core.util.riasecColor
import id.tentuin.student.core.util.riasecTypeDescription
import id.tentuin.student.core.util.riasecTypeName
import id.tentuin.student.ui.component.RiasecChip
import id.tentuin.student.ui.component.TentuinButton
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub

@Composable
fun TestResultScreen(
    navController: NavController,
    riasecCode: String,
    isHistorical: Boolean = false,
) {
    val codes = riasecCode.map { it.toString() }

    LazyColumn(
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
                    .padding(vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
                    }
                    Text(
                        text = if (isHistorical) "Hasil Test" else "Hasil Test Kamu",
                        style = TentuinTypography.titleLarge,
                        color = TextPrimary,
                    )
                }
            }
        }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            ) {
                Text(
                    text = "Tipe Kepribadian Dominan:",
                    style = TentuinTypography.bodyMedium,
                    color = TextSub,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    codes.forEach { code ->
                        RiasecChip(code = code, modifier = Modifier.size(64.dp))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = riasecCode,
                    style = TentuinTypography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "Interpretasi RIASEC",
                    style = TentuinTypography.titleMedium,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        items(codes.size) { index ->
            val code = codes[index]
            RiasecInterpretationCard(code = code, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        }

        item {
            Spacer(Modifier.height(24.dp))
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                TentuinButton(
                    text = "Lihat Rekomendasi Jurusan",
                    onClick = { /* TODO: Navigate to recommendation */ },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun RiasecInterpretationCard(code: String, modifier: Modifier = Modifier) {
    val color = riasecColor(code)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiasecChip(code = code)
                Spacer(Modifier.size(12.dp))
                Text(
                    text = riasecTypeName(code),
                    style = TentuinTypography.titleSmall,
                    color = TextPrimary,
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(12.dp))
            Text(
                text = riasecTypeDescription(code),
                style = TentuinTypography.bodyMedium,
                color = TextSub,
                lineHeight = TentuinTypography.bodyMedium.lineHeight * 1.2,
            )
        }
    }
}
