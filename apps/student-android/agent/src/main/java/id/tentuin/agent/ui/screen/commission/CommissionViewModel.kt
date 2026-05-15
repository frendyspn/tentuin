package id.tentuin.agent.ui.screen.commission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.network.friendlyApiError
import id.tentuin.agent.data.model.AgentCommission
import id.tentuin.agent.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CommissionUiState(
    val year:         Int                 = Calendar.getInstance().get(Calendar.YEAR),
    val commissions:  List<AgentCommission> = emptyList(),
    val totalPending: Int                 = 0,
    val totalPaid:    Int                 = 0,
    val totalAll:     Int                 = 0,
    val isLoading:    Boolean             = true,
    val error:        String?             = null,
)

@HiltViewModel
class CommissionViewModel @Inject constructor(
    private val commissionRepository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionUiState())
    val uiState: StateFlow<CommissionUiState> = _uiState.asStateFlow()

    init { load(_uiState.value.year) }

    fun setYear(year: Int) {
        if (year == _uiState.value.year) return
        _uiState.update { it.copy(year = year) }
        load(year)
    }

    fun refresh() = load(_uiState.value.year)

    fun clearError() = _uiState.update { it.copy(error = null) }

    private fun load(year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            commissionRepository.getCommissions(year)
                .onSuccess { list ->
                    val pending = list.filter { it.status == "pending" }.sumOf { it.totalAmount }
                    val paid    = list.filter { it.status == "paid"    }.sumOf { it.totalAmount }
                    val all     = list.sumOf { it.totalAmount }
                    _uiState.update {
                        it.copy(
                            commissions  = list,
                            totalPending = pending,
                            totalPaid    = paid,
                            totalAll     = all,
                            isLoading    = false,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = friendlyApiError(e, "Gagal memuat komisi."),
                        )
                    }
                }
        }
    }
}
