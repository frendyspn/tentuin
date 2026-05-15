package id.tentuin.schoolpic.ui.screen.school

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.schoolpic.core.network.friendlyApiError
import id.tentuin.schoolpic.data.model.School
import id.tentuin.schoolpic.data.model.UpdateSchoolRequest
import id.tentuin.schoolpic.data.repository.AuthRepository
import id.tentuin.schoolpic.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SchoolProfileUiState(
    val isLoading:  Boolean = true,
    val isSaving:   Boolean = false,
    val school:     School?  = null,
    val error:      String?  = null,
    val info:       String?  = null,
    val isLoggedOut: Boolean = false,
)

@HiltViewModel
class SchoolProfileViewModel @Inject constructor(
    private val schoolRepository: SchoolRepository,
    private val authRepository:   AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolProfileUiState())
    val uiState: StateFlow<SchoolProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            schoolRepository.getMySchool()
                .onSuccess { s -> _uiState.update { it.copy(isLoading = false, school = s) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = friendlyApiError(e)) } }
        }
    }

    fun save(name: String, address: String, email: String, phone: String, logoUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, info = null) }
            val req = UpdateSchoolRequest(
                name    = name.trim().ifBlank { null },
                address = address.trim().ifBlank { null },
                email   = email.trim().ifBlank { null },
                phone   = phone.trim().ifBlank { null },
                logoUrl = logoUrl.trim().ifBlank { null },
            )
            schoolRepository.updateMySchool(req)
                .onSuccess { s ->
                    _uiState.update { it.copy(isSaving = false, school = s, info = "Profil sekolah disimpan.") }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isSaving = false, error = friendlyApiError(e, default = "Gagal menyimpan.")) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, info = null) }
}
