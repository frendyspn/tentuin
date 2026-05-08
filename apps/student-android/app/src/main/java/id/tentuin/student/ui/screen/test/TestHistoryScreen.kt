package id.tentuin.student.ui.screen.test

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.TestResult
import id.tentuin.student.data.repository.TestRepository
import id.tentuin.student.ui.component.RiasecChip
import id.tentuin.student.ui.navigation.Route
import id.tentuin.student.ui.theme.Background
import id.tentuin.student.ui.theme.Border
import id.tentuin.student.ui.theme.Primary
import id.tentuin.student.ui.theme.Surface
import id.tentuin.student.ui.theme.TentuinTypography
import id.tentuin.student.ui.theme.TextPrimary
import id.tentuin.student.ui.theme.TextSub
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TestHistoryScreen(
    navController: NavController,
    testRepository: TestRepository, // Ideally through a dedicated ViewModel
    sessionDataStore: SessionDataStore,
) {
    var history by remember { mutableStateOf<List<TestResult>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val userId = sessionDataStore.userId.first()
        if (userId != null) {
            testRepository.getHistory(userId)
                .onSuccess {
                    history = it
                    isLoading = false
                }
                .onFailure {
                    error = "Gagal memuat riwayat test"
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface)
                .padding(vertical = 8.dp),
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = TextPrimary)
            }
            Text(text = "Riwayat Test", style = TentuinTypography.titleLarge, color = TextPrimary)
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = error!!, style = TentuinTypography.bodyMedium, color = TextPrimary)
            }
        } else if (history.isNullOrEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Belum ada riwayat test", style = TentuinTypography.bodyMedium, color = TextSub)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(history!!) { result ->
                    HistoryCard(result = result) {
                        navController.navigate(Route.TestResult.createRoute(result.riasecCode, true))
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(result: TestResult, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateStr = try {
        dateFormat.format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(result.createdAt)!!)
    } catch (e: Exception) {
        result.createdAt
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Hasil: ${result.riasecCode}", style = TentuinTypography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(text = dateStr, style = TentuinTypography.bodySmall, color = TextSub)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                result.riasecCode.take(3).forEach { char ->
                    RiasecChip(code = char.toString(), modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}
