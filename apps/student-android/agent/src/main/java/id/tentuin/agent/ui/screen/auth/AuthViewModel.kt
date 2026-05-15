package id.tentuin.agent.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.network.friendlyApiError
import id.tentuin.agent.core.network.friendlyAuthError
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
            if (email.isBlank() || password.isBlank()) {
                _uiState.update { it.copy(error = "Email dan password wajib diisi.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email.trim(), password)
                .onSuccess {
                    // Self-heal: pastikan row di tabel `agents` ada. Kalau gagal, jangan
                    // lanjut ke dashboard — surface error supaya bisa diperbaiki.
                    agentRepository.ensureAgentExists().fold(
                        onSuccess = {
                            agentRepository.updateLastActive()
                            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        },
                        onFailure = { e ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Login OK, tapi profil agen gagal terbuat: " +
                                            friendlyApiError(e) +
                                            " Pastikan migration 008 (RLS fixes) sudah dijalankan.",
                                )
                            }
                        }
                    )
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyAuthError(e)) }
                }
        }
    }

    fun register(email: String, password: String, fullName: String, phone: String?) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                _uiState.update { it.copy(error = "Nama, email, dan password wajib diisi.") }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email.trim(), password, fullName.trim(), phone?.trim())
                .onSuccess {
                    agentRepository.getOrCreateAgent(fullName.trim(), email.trim(), phone?.trim())
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyAuthError(e)) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
