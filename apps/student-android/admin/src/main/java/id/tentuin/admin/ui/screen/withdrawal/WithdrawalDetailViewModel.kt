package id.tentuin.admin.ui.screen.withdrawal

import androidx.lifecycle.SavedStateHandle
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

data class WithdrawalDetailUiState(
    val isLoading: Boolean = true,
    val mutating:  Boolean = false,
    val item:      WithdrawalWithAgent? = null,
    val error:     String? = null,
    val toast:     String? = null,
    val finished:  Boolean = false,
)

@HiltViewModel
class WithdrawalDetailViewModel @Inject constructor(
    private val repo: WithdrawalRepository,
    savedState:        SavedStateHandle,
) : ViewModel() {

    val id: String = savedState["id"] ?: ""

    private val _uiState = MutableStateFlow(WithdrawalDetailUiState())
    val uiState: StateFlow<WithdrawalDetailUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (id.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getById(id)
                .onSuccess { w -> _uiState.update { it.copy(isLoading = false, item = w) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal memuat") } }
        }
    }

    fun approve() = mutate { repo.approve(id, _uiState.value.item?.status ?: "requested") }
    fun reject(notes: String) = mutate { repo.reject(id, notes, _uiState.value.item?.status ?: "requested") }
    fun markTransferred() = mutate { repo.markTransferred(id, _uiState.value.item?.status ?: "approved") }

    private fun mutate(block: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(mutating = true) }
            block()
                .onSuccess {
                    _uiState.update { it.copy(mutating = false, toast = "Berhasil", finished = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutating = false, toast = e.message ?: "Gagal") }
                }
        }
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }
}
