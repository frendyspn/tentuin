package id.tentuin.agent.ui.screen.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import id.tentuin.agent.data.model.School
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.ui.component.*
import id.tentuin.agent.ui.component.toast.TentuinToast
import id.tentuin.agent.ui.component.toast.ToastType
import id.tentuin.agent.ui.theme.*

@Composable
fun ClaimSchoolScreen(
    navController: NavController,
    viewModel: ClaimViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { viewModel.loadSchools() }

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.loadSchools()
    }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && pullRefreshState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            ClaimTopBar(title = "Klaim Sekolah", onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background),
        ) {
            SearchField(
                query = state.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when {
                        state.isLoading && state.schools.isEmpty() -> items(5) {
                            SkeletonBox(
                                modifier = Modifier.fillMaxWidth(),
                                height = 110.dp,
                                cornerRadius = 12.dp,
                            )
                        }
                        state.schools.isEmpty() -> item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmptyState(
                                    title = "Belum ada sekolah",
                                    description = "Coba ubah kata kunci atau tarik untuk refresh.",
                                    icon = Icons.Outlined.School,
                                )
                            }
                        }
                        else -> items(state.schools, key = { it.id }) { school ->
                            val claim = state.schoolClaimsBySchoolId[school.id]
                            SchoolListItem(
                                school     = school,
                                claim      = claim,
                                isMyClaim  = claim != null && claim.agentId == state.myUserId,
                                isClaiming = state.claimingId == school.id,
                                onClaim    = { viewModel.claimSchool(school.id) },
                                onTapCode  = { viewModel.showCodeDialog(it) },
                            )
                        }
                    }
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
                    viewModel.clearMessage()
                }
            }
            state.successMessage?.let {
                TentuinToast(message = it, type = ToastType.SUCCESS)
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(2000)
                    viewModel.clearMessage()
                }
            }
        }
    }

    state.dialogCode?.let { code ->
        ClaimCodeDialog(
            code = code,
            description = "Bagikan kode ini ke PIC sekolah agar klaim disetujui:",
            onDismiss = { viewModel.dismissCodeDialog() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClaimTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, style = TentuinAgentTypography.titleLarge, color = TextPrimary) },
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

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Cari nama sekolah...") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedBorderColor = Primary,
            unfocusedBorderColor = Border,
        ),
    )
}

@Composable
private fun SchoolListItem(
    school: School,
    claim: SchoolClaim?,
    isMyClaim: Boolean,
    isClaiming: Boolean,
    onClaim: () -> Unit,
    onTapCode: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(school.name, style = TentuinAgentTypography.titleMedium, color = TextPrimary)
            Text(
                text = "${school.city}, ${school.province}",
                style = TentuinAgentTypography.bodySmall,
                color = TextMuted,
            )
            school.npsn?.let {
                Text(
                    text = "NPSN: $it",
                    style = TentuinAgentTypography.labelSmall,
                    color = TextSub,
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${school.totalStudents} siswa",
                    style = TentuinAgentTypography.labelSmall,
                    color = TextSub,
                )

                ClaimAction(
                    claim = claim,
                    isMyClaim = isMyClaim,
                    isClaiming = isClaiming,
                    onClaim = onClaim,
                    onTapCode = onTapCode,
                )
            }
        }
    }
}

@Composable
private fun ClaimAction(
    claim: SchoolClaim?,
    isMyClaim: Boolean,
    isClaiming: Boolean,
    onClaim: () -> Unit,
    onTapCode: (String) -> Unit,
) {
    when {
        claim == null -> {
            TentuinButton(
                text = "Klaim",
                onClick = onClaim,
                modifier = Modifier.height(36.dp),
                isLoading = isClaiming,
            )
        }
        isMyClaim && claim.status == "pending" -> {
            Column(horizontalAlignment = Alignment.End) {
                ClaimStatusChip(label = "Menunggu Approval", color = Warning)
                Spacer(Modifier.height(4.dp))
                claim.claimCode?.let { code ->
                    ClaimCodeChip(code = code, onClick = { onTapCode(code) })
                }
            }
        }
        isMyClaim && claim.status == "active" -> {
            ClaimStatusChip(label = "Aktif", color = Success)
        }
        else -> {
            ClaimStatusChip(label = "Diklaim Agen Lain", color = TextMuted)
        }
    }
}
