package id.tentuin.agent.ui.screen.claim

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
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
import id.tentuin.agent.ui.component.ClaimCodeDialog
import id.tentuin.agent.ui.component.EmptyState
import id.tentuin.agent.ui.component.SkeletonBox
import id.tentuin.agent.ui.component.UniversityClaimCard
import id.tentuin.agent.ui.component.toast.TentuinToast
import id.tentuin.agent.ui.component.toast.ToastType
import id.tentuin.agent.ui.theme.Background

@Composable
fun ClaimUniversityScreen(
    navController: NavController,
    viewModel: ClaimViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) { viewModel.loadUniversities() }

    LaunchedEffect(pullRefreshState.isRefreshing) {
        if (pullRefreshState.isRefreshing) viewModel.loadUniversities()
    }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && pullRefreshState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            ClaimTopBar(title = "Klaim Kampus", onBack = { navController.popBackStack() })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Background),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(pullRefreshState.nestedScrollConnection),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when {
                        state.isLoading && state.universities.isEmpty() -> items(5) {
                            SkeletonBox(
                                modifier = Modifier.fillMaxWidth(),
                                height = 110.dp,
                                cornerRadius = 12.dp,
                            )
                        }
                        state.universities.isEmpty() -> item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmptyState(
                                    title = "Belum ada kampus",
                                    description = "Daftar kampus mitra belum tersedia. Tarik untuk refresh.",
                                    icon = Icons.Outlined.AccountBalance,
                                )
                            }
                        }
                        else -> items(state.universities, key = { it.id }) { uni ->
                            val claim = state.universityClaimsByUniId[uni.id]
                            UniversityClaimCard(
                                university = uni,
                                claim      = claim,
                                isMyClaim  = claim != null && claim.agentId == state.myUserId,
                                isClaiming = state.claimingId == uni.id,
                                onClaim    = { viewModel.claimUniversity(uni.id) },
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
            description = "Bagikan kode ini ke PIC kampus agar klaim disetujui:",
            onDismiss = { viewModel.dismissCodeDialog() },
        )
    }
}
