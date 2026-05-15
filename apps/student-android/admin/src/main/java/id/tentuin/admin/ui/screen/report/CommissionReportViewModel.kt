package id.tentuin.admin.ui.screen.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.AgentCommissionWithAgent
import id.tentuin.admin.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CommissionReportUiState(
    val isLoading:    Boolean = true,
    val month:        Int = currentMonth(),
    val year:         Int = currentYear(),
    val commissions:  List<AgentCommissionWithAgent> = emptyList(),
    val error:        String? = null,
) {
    val totalStreamA: Int get() = commissions.sumOf { it.streamAAmount }
    val totalStreamB: Int get() = commissions.sumOf { it.streamBAmount }
    val totalAll:     Int get() = commissions.sumOf { it.totalAmount }
}

private fun currentMonth() = Calendar.getInstance().get(Calendar.MONTH) + 1
private fun currentYear()  = Calendar.getInstance().get(Calendar.YEAR)

@HiltViewModel
class CommissionReportViewModel @Inject constructor(
    private val repo: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionReportUiState())
    val uiState: StateFlow<CommissionReportUiState> = _uiState.asStateFlow()

    init { load(_uiState.value.month, _uiState.value.year) }

    fun load(month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, month = month, year = year, error = null) }
            repo.listFor(month, year)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, commissions = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
