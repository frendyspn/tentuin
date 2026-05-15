package id.tentuin.schoolpic.ui.screen.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.schoolpic.core.network.friendlyApiError
import id.tentuin.schoolpic.data.model.Profile
import id.tentuin.schoolpic.data.repository.StudentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentUiState(
    val isLoading: Boolean = true,
    val query:     String = "",
    val items:     List<Profile> = emptyList(),
    val error:     String? = null,
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val repository: StudentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentUiState())
    val uiState: StateFlow<StudentUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init { load("") }

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            load(q)
        }
    }

    fun refresh() = load(_uiState.value.query)

    private fun load(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.searchStudents(query)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, items = list) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = friendlyApiError(e)) } }
        }
    }
}
