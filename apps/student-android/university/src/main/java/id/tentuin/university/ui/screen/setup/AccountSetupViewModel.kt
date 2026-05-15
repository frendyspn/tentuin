package id.tentuin.university.ui.screen.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
import id.tentuin.university.data.model.UniversityBrief
import id.tentuin.university.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val universities: List<UniversityBrief> = emptyList(),
    val loadingList: Boolean = false,
    val submitting:  Boolean = false,
    val error:       String? = null,
    val success:     Boolean = false,
)

@HiltViewModel
class AccountSetupViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SetupUiState())
    val state: StateFlow<SetupUiState> = _state.asStateFlow()

    init { loadUniversities() }

    private fun loadUniversities() {
        viewModelScope.launch {
            _state.update { it.copy(loadingList = true) }
            accountRepo.getUniversities()
                .onSuccess { list -> _state.update { it.copy(universities = list, loadingList = false) } }
                .onFailure { e -> _state.update { it.copy(loadingList = false, error = friendlyApiError(e)) } }
        }
    }

    fun submit(accountType: String, displayName: String, universityId: String?) {
        viewModelScope.launch {
            if (displayName.isBlank()) {
                _state.update { it.copy(error = "Nama account wajib diisi.") }
                return@launch
            }
            _state.update { it.copy(submitting = true, error = null) }
            val res = if (accountType == "enterprise")
                accountRepo.createEnterprise(displayName.trim(), universityId)
            else
                accountRepo.createPersonal(displayName.trim(), universityId)

            res.onSuccess { r ->
                if (r.success) _state.update { it.copy(submitting = false, success = true) }
                else _state.update { it.copy(submitting = false, error = r.message) }
            }.onFailure { e ->
                _state.update { it.copy(submitting = false, error = friendlyApiError(e)) }
            }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }
}
