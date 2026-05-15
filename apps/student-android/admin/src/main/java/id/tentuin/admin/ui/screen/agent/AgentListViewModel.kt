package id.tentuin.admin.ui.screen.agent

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

data class AgentListUiState(
    val isLoading: Boolean = true,
    val agents:    List<Agent> = emptyList(),
    val query:     String = "",
    val error:     String? = null,
)

@HiltViewModel
class AgentListViewModel @Inject constructor(
    private val repo: AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AgentListUiState())
    val uiState: StateFlow<AgentListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.list()
                .onSuccess { agents ->
                    _uiState.update { it.copy(isLoading = false, agents = agents) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Gagal memuat") }
                }
        }
    }

    fun setQuery(q: String) = _uiState.update { it.copy(query = q) }

    fun filtered(): List<Agent> {
        val q = _uiState.value.query.trim().lowercase()
        if (q.isEmpty()) return _uiState.value.agents
        return _uiState.value.agents.filter {
            it.fullName.lowercase().contains(q) ||
            it.referralCode.lowercase().contains(q) ||
            it.email.lowercase().contains(q)
        }
    }
}
