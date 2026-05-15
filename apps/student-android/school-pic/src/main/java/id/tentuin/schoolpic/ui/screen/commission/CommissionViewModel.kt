package id.tentuin.schoolpic.ui.screen.commission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.schoolpic.core.network.friendlyApiError
import id.tentuin.schoolpic.data.model.SchoolCommission
import id.tentuin.schoolpic.data.repository.CommissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommissionUiState(
    val isLoading: Boolean = true,
    val items:     List<SchoolCommission> = emptyList(),
    val error:     String? = null,
) {
    val totalPaid:    Long get() = items.filter { it.status == "paid" }.sumOf { it.amount }
    val totalPending: Long get() = items.filter { it.status == "pending" }.sumOf { it.amount }
    val totalAll:     Long get() = items.sumOf { it.amount }
}

@HiltViewModel
class CommissionViewModel @Inject constructor(
    private val repository: CommissionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommissionUiState())
    val uiState: StateFlow<CommissionUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getAllCommissions()
                .onSuccess { items -> _uiState.update { it.copy(isLoading = false, items = items) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = friendlyApiError(e)) } }
        }
    }
}
