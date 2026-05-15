package id.tentuin.university.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyAuthError
import id.tentuin.university.data.repository.AccountRepository
import id.tentuin.university.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading:        Boolean = false,
    val error:            String? = null,
    val isSuccess:        Boolean = false,
    val needsAccountSetup: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo:    AuthRepository,
    private val accountRepo: AccountRepository,
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
            authRepo.login(email.trim(), password)
                .onSuccess {
                    val account = accountRepo.getMyAccount().getOrNull()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                            needsAccountSetup = (account == null),
                        )
                    }
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
            authRepo.register(email.trim(), password, fullName.trim(), phone?.trim())
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true, needsAccountSetup = true)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyAuthError(e)) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
