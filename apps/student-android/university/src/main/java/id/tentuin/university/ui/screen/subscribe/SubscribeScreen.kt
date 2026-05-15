package id.tentuin.university.ui.screen.subscribe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.university.core.util.formatDateId
import id.tentuin.university.core.util.toRupiah
import id.tentuin.university.data.model.SubscriptionPlan
import id.tentuin.university.ui.component.TentuinButton
import id.tentuin.university.ui.component.toast.TentuinToast
import id.tentuin.university.ui.component.toast.ToastType
import id.tentuin.university.ui.theme.*

@Composable
fun SubscribeScreen(
    navController: NavController,
    vm: SubscribeViewModel = hiltViewModel(),
) {
    val s by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding()
                .verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Kembali", tint = TextPrimary)
                }
                Text("Top-up Kuota", style = TentuinUniversityTypography.headlineMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(8.dp))

            s.account?.let { acc ->
                Card(colors = CardDefaults.cardColors(containerColor = PrimaryLight)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Sisa kuota saat ini", style = TentuinUniversityTypography.labelSmall, color = TextSub)
                        Text("${acc.quotaBalance} data", style = TentuinUniversityTypography.titleLarge, color = PrimaryDark)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            Text("Pilih Paket", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))

            if (s.loading && s.plans.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else {
                s.plans.forEach { plan ->
                    PlanCard(plan, isLoading = s.submitting, onSubscribe = { vm.subscribe(plan.code) })
                    Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Riwayat", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            if (s.history.isEmpty()) {
                Text("Belum ada riwayat top-up.", style = TentuinUniversityTypography.bodyMedium, color = TextMuted)
            } else {
                s.history.forEach { log ->
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), color = Surface) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${log.quotaPurchased} data", style = TentuinUniversityTypography.titleMedium, color = TextPrimary)
                                Text(formatDateId(log.subscribedAt), style = TentuinUniversityTypography.labelSmall, color = TextMuted)
                            }
                            Text(log.amount.toRupiah(), style = TentuinUniversityTypography.titleMedium, color = Primary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        s.toast?.let {
            TentuinToast(it, ToastType.SUCCESS, modifier = Modifier.align(Alignment.TopCenter))
            LaunchedEffect(it) { kotlinx.coroutines.delay(2500); vm.clearMessages() }
        }
        s.error?.let {
            TentuinToast(it, ToastType.ERROR, modifier = Modifier.align(Alignment.TopCenter))
            LaunchedEffect(it) { kotlinx.coroutines.delay(3000); vm.clearMessages() }
        }
    }
}

@Composable
private fun PlanCard(plan: SubscriptionPlan, isLoading: Boolean, onSubscribe: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Surface, tonalElevation = 1.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(plan.name, style = TentuinUniversityTypography.titleLarge, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("${plan.quota} data prospek", style = TentuinUniversityTypography.bodyMedium, color = TextSub)
            Spacer(Modifier.height(8.dp))
            Text(plan.price.toRupiah(), style = TentuinUniversityTypography.headlineMedium, color = Primary)
            Spacer(Modifier.height(12.dp))
            TentuinButton("Subscribe", onClick = onSubscribe, isLoading = isLoading, modifier = Modifier.fillMaxWidth())
        }
    }
}
