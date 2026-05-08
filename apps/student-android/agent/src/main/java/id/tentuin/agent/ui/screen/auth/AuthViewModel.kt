package id.tentuin.agent.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.data.repository.AgentRepository
import id.tentuin.agent.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading:    Boolean = false,
    val error:        String? = null,
    val isSuccess:    Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val agentRepository: AgentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess {
                    agentRepository.updateLastActive()
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Login gagal") }
                }
        }
    }

    fun register(email: String, password: String, fullName: String, phone: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email, password, fullName, phone)
                .onSuccess {
                    agentRepository.getOrCreateAgent(fullName, email, phone)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Registrasi gagal") }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
