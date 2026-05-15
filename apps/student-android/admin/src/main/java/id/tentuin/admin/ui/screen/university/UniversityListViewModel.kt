package id.tentuin.admin.ui.screen.university

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.admin.data.model.UniversityWithClaims
import id.tentuin.admin.data.repository.UniversityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UniversityListUiState(
    val isLoading:    Boolean = true,
    val universities: List<UniversityWithClaims> = emptyList(),
    val query:        String = "",
    val error:        String? = null,
)

@HiltViewModel
class UniversityListViewModel @Inject constructor(
    private val repo: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversityListUiState())
    val uiState: StateFlow<UniversityListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.list()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, universities = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun setQuery(q: String) = _uiState.update { it.copy(query = q) }

    fun filtered(): List<UniversityWithClaims> {
        val q = _uiState.value.query.trim().lowercase()
        if (q.isEmpty()) return _uiState.value.universities
        return _uiState.value.universities.filter {
            it.name.lowercase().contains(q) ||
            (it.city?.lowercase()?.contains(q) == true)
        }
    }
}
