package id.tentuin.student.ui.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.repository.BookmarkRepository
import id.tentuin.student.data.repository.ExploreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UniversityDetailUiState(
    val university: UniversityRow? = null,
    val isLoading:  Boolean        = true,
    val isBookmarked: Boolean      = false,
    val error:      String?        = null,
)

@HiltViewModel
class UniversityDetailViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val bookmarkRepository: BookmarkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversityDetailUiState())
    val uiState: StateFlow<UniversityDetailUiState> = _uiState.asStateFlow()

    fun loadDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            exploreRepository.getUniversityDetail(id)
                .onSuccess { uni ->
                    _uiState.update { it.copy(university = uni, isLoading = false) }
                    checkBookmarkStatus(id)
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    private fun checkBookmarkStatus(universityId: String) {
        // Implementation for checking if bookmarked
    }

    fun toggleBookmark() {
        // Implementation for toggling bookmark
    }
}
