package id.tentuin.agent.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.ui.component.AgentCard
import id.tentuin.agent.ui.component.SkeletonBox
import id.tentuin.agent.ui.navigation.Route
import id.tentuin.agent.ui.theme.*

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) {
            navController.navigate(Route.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Text(
            text = "Profil",
            style = TentuinAgentTypography.headlineMedium,
            color = TextPrimary,
        )

        Spacer(Modifier.height(16.dp))

        when {
            state.isLoading -> SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 100.dp, cornerRadius = 16.dp)
            state.agent != null -> AgentCard(agent = state.agent!!)
            else -> Text(
                text = state.error ?: "Data agen tidak ditemukan",
                style = TentuinAgentTypography.bodyMedium,
                color = Error,
            )
        }

        Spacer(Modifier.height(24.dp))

        BankInfoSection(agent = state.agent, isLoading = state.isLoading)

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Error.copy(alpha = 0.1f),
                contentColor   = Error,
            ),
            shape = RoundedCornerShape(8.dp),
            enabled = !state.isLoggingOut,
        ) {
            if (state.isLoggingOut) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Error,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Keluar", style = TentuinAgentTypography.labelLarge)
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Keluar dari akun?", style = TentuinAgentTypography.titleMedium) },
            text  = { Text("Kamu perlu login lagi untuk masuk kembali.", style = TentuinAgentTypography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout()
                }) {
                    Text("Keluar", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal", color = TextSub)
                }
            },
            containerColor = Surface,
        )
    }
}

@Composable
private fun BankInfoSection(agent: Agent?, isLoading: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.AccountBalance,
                    contentDescription = null,
                    tint = Primary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Rekening Bank",
                    style = TentuinAgentTypography.titleMedium,
                    color = TextPrimary,
                )
            }

            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 14.dp)
                    Spacer(Modifier.height(6.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 14.dp)
                }
                agent?.bankName.isNullOrBlank() -> {
                    Text(
                        text = "Belum diatur. Atur rekening dulu sebelum menarik komisi.",
                        style = TentuinAgentTypography.bodySmall,
                        color = TextMuted,
                    )
                }
                else -> {
                    InfoRow("Bank",      agent?.bankName ?: "-")
                    InfoRow("No. Rek.",  agent?.bankAccountNumber ?: "-")
                    InfoRow("Atas Nama", agent?.bankAccountName ?: "-")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = TentuinAgentTypography.bodySmall, color = TextMuted)
        Text(value, style = TentuinAgentTypography.bodyMedium, color = TextPrimary)
    }
}
