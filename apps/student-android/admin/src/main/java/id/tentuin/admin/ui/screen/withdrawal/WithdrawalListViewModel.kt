package id.tentuin.admin.ui.screen.withdrawal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.WithdrawalWithAgent
import id.tentuin.admin.data.repository.WithdrawalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WithdrawalListUiState(
    val isLoading:      Boolean = true,
    val pending:        List<WithdrawalWithAgent> = emptyList(),
    val all:            List<WithdrawalWithAgent> = emptyList(),
    val error:          String? = null,
)

@HiltViewModel
class WithdrawalListViewModel @Inject constructor(
    private val repo: WithdrawalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawalListUiState())
    val uiState: StateFlow<WithdrawalListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val pending = repo.listPending().getOrElse { emptyList() }
            val all     = repo.listAll().getOrElse { emptyList() }
            _uiState.update { it.copy(isLoading = false, pending = pending, all = all) }
        }
    }
}
