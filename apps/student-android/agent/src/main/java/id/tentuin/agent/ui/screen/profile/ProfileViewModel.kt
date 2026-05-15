package id.tentuin.agent.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val agent:        Agent?  = null,
    val isLoading:    Boolean = true,
    val isLoggingOut: Boolean = false,
    val loggedOut:    Boolean = false,
    val error:        String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository:  AuthRepository,
    private val agentRepository: AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadAgent() }

    fun refresh() = loadAgent()

    private fun loadAgent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            agentRepository.getCurrentAgent()
                .onSuccess { agent ->
                    _uiState.update { it.copy(agent = agent, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = e.message ?: e.javaClass.simpleName,
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true) }
            authRepository.logout()
            _uiState.update { it.copy(isLoggingOut = false, loggedOut = true) }
        }
    }
}
