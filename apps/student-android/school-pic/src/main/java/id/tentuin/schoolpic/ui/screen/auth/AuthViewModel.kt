package id.tentuin.schoolpic.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.schoolpic.core.network.friendlyApiError
import id.tentuin.schoolpic.core.network.friendlyAuthError
import id.tentuin.schoolpic.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error:     String? = null,
    val isSuccess: Boolean = false,
    val info:      String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
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
                    runCatching { authRepository.syncSchoolIdFromProfile() }
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyAuthError(e)) }
                }
        }
    }

    fun register(fullName: String, email: String, password: String, claimCode: String) {
        viewModelScope.launch {
            if (fullName.isBlank() || email.isBlank() || password.isBlank() || claimCode.isBlank()) {
                _uiState.update { it.copy(error = "Semua field wajib diisi.") }
                return@launch
            }
            if (password.length < 6) {
                _uiState.update { it.copy(error = "Password minimal 6 karakter.") }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            val regResult = authRepository.register(email.trim(), password, fullName.trim())
            if (regResult.isFailure) {
                _uiState.update {
                    it.copy(isLoading = false, error = friendlyAuthError(regResult.exceptionOrNull()!!))
                }
                return@launch
            }

            authRepository.bindToSchool(claimCode.trim())
                .onSuccess { result ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, info = result.message) }
                }
                .onFailure { e ->
                    // Akun sudah dibuat tapi binding gagal — pesan jelas, user bisa login & coba bind ulang nanti.
                    _uiState.update { it.copy(isLoading = false, error = friendlyApiError(e, default = "Gagal menghubungkan kode registrasi.")) }
                }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
