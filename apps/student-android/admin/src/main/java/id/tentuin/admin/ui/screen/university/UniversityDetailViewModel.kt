package id.tentuin.admin.ui.screen.university

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.UniversitySubscribeLog
import id.tentuin.admin.data.model.UniversityWithClaims
import id.tentuin.admin.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UniversityDetailUiState(
    val isLoading:     Boolean = true,
    val university:    UniversityWithClaims? = null,
    val subscribeLogs: List<UniversitySubscribeLog> = emptyList(),
    val error:         String? = null,
    val mutating:      Boolean = false,
    val toast:         String? = null,
)

@HiltViewModel
class UniversityDetailViewModel @Inject constructor(
    private val repo: UniversityRepository,
    savedState:        SavedStateHandle,
) : ViewModel() {
    val universityId: String = savedState["id"] ?: ""

    private val _uiState = MutableStateFlow(UniversityDetailUiState())
    val uiState: StateFlow<UniversityDetailUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (universityId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val u    = repo.getById(universityId).getOrNull()
            val logs = repo.subscribeLogs(universityId).getOrElse { emptyList() }
            _uiState.update { it.copy(isLoading = false, university = u, subscribeLogs = logs) }
        }
    }

    fun submitSubscribe(amount: Int, quota: Int, onDone: () -> Unit) {
        val agentId = _uiState.value.university?.activeClaim?.agentId
        viewModelScope.launch {
            _uiState.update { it.copy(mutating = true) }
            repo.recordSubscribe(universityId, agentId, amount, quota)
                .onSuccess {
                    _uiState.update { it.copy(mutating = false, toast = "Subscribe tercatat") }
                    refresh()
                    onDone()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(mutating = false, toast = e.message ?: "Gagal") }
                }
        }
    }

    fun clearToast() = _uiState.update { it.copy(toast = null) }
}
