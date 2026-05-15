package id.tentuin.admin.ui.screen.agent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.Agent
import id.tentuin.admin.data.repository.AgentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentDetailUiState(
    val isLoading: Boolean = true,
    val agent:     Agent?  = null,
    val error:     String? = null,
    val mutating:  Boolean = false,
    val toast:     String? = null,
)

@HiltViewModel
class AgentDetailViewModel @Inject constructor(
    private val repo:    AgentRepository,
    savedState:          SavedStateHandle,
) : ViewModel() {

    val agentId: String = savedState["id"] ?: ""

    private val _uiState = MutableStateFlow(AgentDetailUiState())
    val uiState: StateFlow<AgentDetailUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (agentId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getById(agentId)
                .onSuccess { a -> _uiState.update { it.copy(isLoading = false, agent = a) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal memuat") } }
        }
    }

    fun toggleSuspend() {
        val current = _uiState.value.agent ?: return
        val newStatus = if (current.status == "active") "suspended" else "active"
        viewModelScope.launch {
            _uiState.update { it.copy(mutating = true) }
            repo.setStatus(current.id, newStatus, current.status)
                .onSuccess {
                    _uiState.update { it.copy(mutating = false, toast = "Status diperbarui ke $newStatus") }
                    refresh()
                }
                .onFailure { e -> _uiState.update { it.copy(mutating = false, toast = e.message ?: "Gagal") } }
        }
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }
}
