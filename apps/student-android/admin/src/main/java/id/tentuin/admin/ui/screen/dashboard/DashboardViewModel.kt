package id.tentuin.admin.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.data.model.WithdrawalWithAgent
import id.tentuin.admin.data.repository.AgentRepository
import id.tentuin.admin.data.repository.WithdrawalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading:          Boolean = true,
    val adminName:          String? = null,
    val pendingWithdrawals: Int = 0,
    val totalAgents:        Int = 0,
    val activeAgents:       Int = 0,
    val recentPending:      List<WithdrawalWithAgent> = emptyList(),
    val error:              String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val session:            SessionDataStore,
    private val withdrawalRepo:     WithdrawalRepository,
    private val agentRepo:          AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val name = session.fullName.first()

            val pending = withdrawalRepo.listPending().getOrElse { emptyList() }
            val agents  = agentRepo.list().getOrElse { emptyList() }

            _uiState.update {
                it.copy(
                    isLoading          = false,
                    adminName          = name,
                    pendingWithdrawals = pending.size,
                    totalAgents        = agents.size,
                    activeAgents       = agents.count { a -> a.status == "active" },
                    recentPending      = pending.take(3),
                )
            }
        }
    }
}
