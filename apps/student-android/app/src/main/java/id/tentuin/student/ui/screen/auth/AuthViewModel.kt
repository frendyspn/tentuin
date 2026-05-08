package id.tentuin.student.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val accessToken = authRepository.accessToken

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val emailError = validateEmail(email) ?: return setError(validateEmail(email) ?: return doLogin(email, password))
        setError(emailError)
    }

    private fun doLogin(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.login(email, password)
                .onSuccess { _uiState.value = AuthUiState(isSuccess = true) }
                .onFailure { _uiState.value = AuthUiState(error = friendlyError(it.message)) }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        val nameError     = if (fullName.isBlank()) "Nama lengkap wajib diisi" else null
        val emailError    = validateEmail(email)
        val passwordError = if (password.length < 8) "Password minimal 8 karakter" else null
        val firstError    = nameError ?: emailError ?: passwordError
        if (firstError != null) { setError(firstError); return }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.register(email, password, fullName)
                .onSuccess { _uiState.value = AuthUiState(isSuccess = true) }
                .onFailure { _uiState.value = AuthUiState(error = friendlyError(it.message)) }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun setError(msg: String) {
        _uiState.value = AuthUiState(error = msg)
    }

    private fun validateEmail(email: String): String? =
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Format email tidak valid" else null

    private fun friendlyError(msg: String?): String = when {
        msg == null                          -> "Terjadi kesalahan, coba lagi"
        msg.contains("Invalid login")        -> "Email atau password salah"
        msg.contains("already registered")  -> "Email sudah terdaftar"
        msg.contains("rate limit")           -> "Terlalu banyak percobaan, tunggu sebentar"
        else                                 -> "Terjadi kesalahan, coba lagi"
    }
}
