package id.tentuin.agent.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.AgentCommission
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.ClaimRepository
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val agent:              Agent? = null,
    val currentMonthComm:  AgentCommission? = null,
    val totalPending:       Int = 0,
    val totalPaid:          Int = 0,
    val schoolClaimCount:   Int = 0,
    val uniClaimCount:      Int = 0,
    val isLoading:          Boolean = true,
    val error:              String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val agentRepository: AgentRepository,
    private val claimRepository: ClaimRepository,
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun refresh() = loadDashboard()

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar months are 0-indexed

            val agent = agentRepository.getCurrentAgent().getOrNull()
            val commissions = commissionRepository.getCommissions(year).getOrDefault(emptyList())
            val schoolClaims = claimRepository.getMySchoolClaims().getOrDefault(emptyList())
            val uniClaims = claimRepository.getMyUniversityClaims().getOrDefault(emptyList())

            val currentMonthComm = commissions.firstOrNull { it.month == month }
            val totalPending = commissions.filter { it.status == "pending" }.sumOf { it.totalAmount }
            val totalPaid    = commissions.filter { it.status == "paid" }.sumOf { it.totalAmount }

            _uiState.update {
                it.copy(
                    agent            = agent,
                    currentMonthComm = currentMonthComm,
                    totalPending     = totalPending,
                    totalPaid        = totalPaid,
                    schoolClaimCount = schoolClaims.size,
                    uniClaimCount    = uniClaims.size,
                    isLoading        = false,
                )
            }
        }
    }
}
