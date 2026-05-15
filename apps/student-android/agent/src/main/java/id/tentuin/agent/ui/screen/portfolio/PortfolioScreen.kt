package id.tentuin.agent.ui.screen.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.core.util.formatDateId
import id.tentuin.agent.core.util.daysUntil
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.ui.component.*
import id.tentuin.agent.ui.component.toast.TentuinToast
import id.tentuin.agent.ui.component.toast.ToastType
import id.tentuin.agent.ui.theme.*

private enum class PortfolioTab(val label: String, val icon: ImageVector) {
    Sekolah("Sekolah", Icons.Outlined.School),
    Kampus("Kampus",  Icons.Outlined.AccountBalance),
}

@Composable
fun PortfolioScreen(
    navController: NavController,
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(PortfolioTab.Sekolah) }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.load()
    }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && pullRefreshState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            PortfolioTopBar(onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background),
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Surface,
                contentColor = Primary,
            ) {
                PortfolioTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.label) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
            ) {
                when (selectedTab) {
                    PortfolioTab.Sekolah -> SchoolPortfolioList(
                        claims    = state.schoolClaims,
                        isLoading = state.isLoading,
                        onTapCode = viewModel::showCodeDialog,
                    )
                    PortfolioTab.Kampus -> UniversityPortfolioList(
                        claims    = state.universityClaims,
                        isLoading = state.isLoading,
                        onTapCode = viewModel::showCodeDialog,
                    )
                }

                if (pullRefreshState.isRefreshing || pullRefreshState.progress > 0f) {
                    PullToRefreshContainer(
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }

            state.error?.let {
                TentuinToast(message = it, type = ToastType.ERROR)
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.clearError()
                }
            }
        }
    }

    state.dialogCode?.let { code ->
        ClaimCodeDialog(
            code = code,
            title = "Kode Klaim",
            description = "Bagikan kode ini ke PIC untuk verifikasi:",
            onDismiss = { viewModel.dismissCodeDialog() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortfolioTopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("Portfolio Saya", style = TentuinAgentTypography.titleLarge, color = TextPrimary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = TextPrimary,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
    )
}

// ── Schools tab ──────────────────────────────────────────────────────────────

@Composable
private fun SchoolPortfolioList(
    claims: List<SchoolClaim>,
    isLoading: Boolean,
    onTapCode: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            isLoading && claims.isEmpty() -> items(4) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 120.dp, cornerRadius = 12.dp)
            }
            claims.isEmpty() -> item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = "Belum ada klaim sekolah",
                        description = "Klaim sekolah dari menu Aktivitas → Klaim Sekolah.",
                        icon = Icons.Outlined.School,
                    )
                }
            }
            else -> items(claims, key = { it.id }) { claim ->
                SchoolPortfolioCard(claim = claim, onTapCode = onTapCode)
            }
        }
    }
}

@Composable
private fun SchoolPortfolioCard(claim: SchoolClaim, onTapCode: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.school?.name ?: "Sekolah",
                        style = TentuinAgentTypography.titleMedium,
                        color = TextPrimary,
                    )
                    claim.school?.let {
                        Text(
                            text = "${it.city}, ${it.province}",
                            style = TentuinAgentTypography.bodySmall,
                            color = TextMuted,
                        )
                    }
                }
                StatusChipFor(status = claim.status)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(8.dp))

            ClaimMeta(
                claim = claim.toMeta(),
                onTapCode = onTapCode,
            )
        }
    }
}

// ── Universities tab ─────────────────────────────────────────────────────────

@Composable
private fun UniversityPortfolioList(
    claims: List<UniversityClaim>,
    isLoading: Boolean,
    onTapCode: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            isLoading && claims.isEmpty() -> items(4) {
                SkeletonBox(modifier = Modifier.fillMaxWidth(), height = 120.dp, cornerRadius = 12.dp)
            }
            claims.isEmpty() -> item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        title = "Belum ada klaim kampus",
                        description = "Klaim kampus dari menu Aktivitas → Klaim Kampus.",
                        icon = Icons.Outlined.AccountBalance,
                    )
                }
            }
            else -> items(claims, key = { it.id }) { claim ->
                UniversityPortfolioCard(claim = claim, onTapCode = onTapCode)
            }
        }
    }
}

@Composable
private fun UniversityPortfolioCard(claim: UniversityClaim, onTapCode: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.university?.name ?: "Kampus",
                        style = TentuinAgentTypography.titleMedium,
                        color = TextPrimary,
                    )
                    claim.university?.let { uni ->
                        Text(
                            text = "${uni.shortName} • ${uni.city}",
                            style = TentuinAgentTypography.bodySmall,
                            color = TextMuted,
                        )
                        if (uni.isPartner) {
                            Spacer(Modifier.height(4.dp))
                            val tier = uni.partnerTier?.uppercase() ?: "BASIC"
                            ClaimStatusChip(label = tier, color = Primary)
                        }
                    }
                }
                StatusChipFor(status = claim.status)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Border)
            Spacer(Modifier.height(8.dp))

            ClaimMeta(
                claim = claim.toMeta(),
                onTapCode = onTapCode,
            )

            claim.university?.let { uni ->
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Sisa kuota", style = TentuinAgentTypography.labelSmall, color = TextMuted)
                    Text(
                        text  = "${uni.quotaBalance}",
                        style = TentuinAgentTypography.bodyMedium,
                        color = TextPrimary,
                    )
                }
            }
        }
    }
}

// ── Shared sub-components ────────────────────────────────────────────────────

@Composable
private fun StatusChipFor(status: String) {
    val (label, color) = when (status) {
        "active"    -> "Aktif" to Success
        "pending"   -> "Pending" to Warning
        "expired"   -> "Kadaluarsa" to TextMuted
        "cancelled" -> "Dibatalkan" to Error
        else        -> status to TextMuted
    }
    ClaimStatusChip(label = label, color = color)
}

private data class ClaimMetaInfo(
    val status:     String,
    val claimCode:  String?,
    val verifiedAt: String?,
    val expiresAt:  String?,
    val claimedAt:  String,
)

private fun SchoolClaim.toMeta() = ClaimMetaInfo(
    status     = status,
    claimCode  = claimCode,
    verifiedAt = verifiedAt,
    expiresAt  = expiresAt,
    claimedAt  = claimedAt,
)

private fun UniversityClaim.toMeta() = ClaimMetaInfo(
    status     = status,
    claimCode  = claimCode,
    verifiedAt = verifiedAt,
    expiresAt  = expiresAt,
    claimedAt  = claimedAt,
)

@Composable
private fun ClaimMeta(claim: ClaimMetaInfo, onTapCode: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        MetaRow("Diklaim pada", formatDateId(claim.claimedAt))

        when (claim.status) {
            "pending" -> {
                claim.claimCode?.let { code ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Kode Klaim", style = TentuinAgentTypography.labelSmall, color = TextMuted)
                        ClaimCodeChip(code = code, onClick = { onTapCode(code) })
                    }
                }
                val days = daysUntil(claim.expiresAt)
                MetaRow(
                    label = "Berakhir",
                    value = when {
                        days == null              -> formatDateId(claim.expiresAt)
                        days <= 0L                -> "Hari ini / lewat"
                        days == 1L                -> "Besok"
                        else                      -> "$days hari lagi"
                    },
                )
            }
            "active" -> {
                MetaRow("Diverifikasi pada", formatDateId(claim.verifiedAt))
            }
            else -> {
                MetaRow("Berakhir", formatDateId(claim.expiresAt))
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = TentuinAgentTypography.labelSmall, color = TextMuted)
        Text(value, style = TentuinAgentTypography.bodyMedium, color = TextPrimary)
    }
}
