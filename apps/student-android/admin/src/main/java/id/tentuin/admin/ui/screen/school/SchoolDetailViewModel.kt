package id.tentuin.admin.ui.screen.school

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.SchoolWithClaims
import id.tentuin.admin.data.repository.SchoolRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SchoolDetailUiState(
    val isLoading: Boolean = true,
    val school:    SchoolWithClaims? = null,
    val error:     String? = null,
)

@HiltViewModel
class SchoolDetailViewModel @Inject constructor(
    private val repo: SchoolRepository,
    savedState:        SavedStateHandle,
) : ViewModel() {
    val schoolId: String = savedState["id"] ?: ""

    private val _uiState = MutableStateFlow(SchoolDetailUiState())
    val uiState: StateFlow<SchoolDetailUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        if (schoolId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.getById(schoolId)
                .onSuccess { s -> _uiState.update { it.copy(isLoading = false, school = s) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }
}
