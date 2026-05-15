package id.tentuin.admin.ui.screen.school

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

data class SchoolListUiState(
    val isLoading: Boolean = true,
    val schools:   List<SchoolWithClaims> = emptyList(),
    val query:     String = "",
    val error:     String? = null,
)

@HiltViewModel
class SchoolListViewModel @Inject constructor(
    private val repo: SchoolRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchoolListUiState())
    val uiState: StateFlow<SchoolListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repo.list()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, schools = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun setQuery(q: String) = _uiState.update { it.copy(query = q) }

    fun filtered(): List<SchoolWithClaims> {
        val q = _uiState.value.query.trim().lowercase()
        if (q.isEmpty()) return _uiState.value.schools
        return _uiState.value.schools.filter {
            it.name.lowercase().contains(q) ||
            it.city.lowercase().contains(q) ||
            (it.npsn?.lowercase()?.contains(q) == true)
        }
    }
}
