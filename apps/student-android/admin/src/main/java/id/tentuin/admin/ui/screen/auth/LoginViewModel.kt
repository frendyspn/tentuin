package id.tentuin.admin.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.core.notification.PushTokenRegistrar
import id.tentuin.admin.data.repository.AuthRepository
import id.tentuin.admin.data.repository.NotAdminException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error:     String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository:      AuthRepository,
    private val pushTokenRegistrar:  PushTokenRegistrar,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onSuccess {
                    runCatching { pushTokenRegistrar.ensureRegistered() }
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    val msg = when (e) {
                        is NotAdminException -> e.message ?: "Akun ini bukan admin"
                        else -> e.message ?: "Login gagal"
                    }
                    _uiState.update { it.copy(isLoading = false, error = msg) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
