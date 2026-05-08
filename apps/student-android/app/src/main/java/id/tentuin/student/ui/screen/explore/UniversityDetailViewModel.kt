package id.tentuin.student.ui.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.repository.BookmarkRepository
import id.tentuin.student.data.repository.ExploreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UniversityDetailUiState(
    val university:   UniversityRow? = null,
    val isLoading:    Boolean        = true,
    val isBookmarked: Boolean        = false,
    val isGuest:      Boolean        = false,
    val error:        String?        = null,
)

@HiltViewModel
class UniversityDetailViewModel @Inject constructor(
    private val exploreRepository:  ExploreRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val sessionDataStore:   SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UniversityDetailUiState())
    val uiState: StateFlow<UniversityDetailUiState> = _uiState.asStateFlow()

    private var currentUniversityId: String = ""

    fun loadDetail(id: String) {
        currentUniversityId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userId = sessionDataStore.userId.first()
            _uiState.update { it.copy(isGuest = userId == null) }

            exploreRepository.getUniversityDetail(id)
                .onSuccess { uni ->
                    _uiState.update { it.copy(university = uni, isLoading = false) }
                    if (userId != null) checkBookmarkStatus(userId, id)
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    private fun checkBookmarkStatus(userId: String, universityId: String) {
        viewModelScope.launch {
            bookmarkRepository.getBookmarks(userId)
                .onSuccess { bookmarks ->
                    val isBookmarked = bookmarks.any { it.universityId == universityId }
                    _uiState.update { it.copy(isBookmarked = isBookmarked) }
                }
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            val userId = sessionDataStore.userId.first() ?: return@launch
            val currentlyBookmarked = _uiState.value.isBookmarked

            if (currentlyBookmarked) {
                bookmarkRepository.deleteBookmark(userId, currentUniversityId)
                    .onSuccess { _uiState.update { it.copy(isBookmarked = false) } }
            } else {
                bookmarkRepository.createBookmark(userId, currentUniversityId)
                    .onSuccess { _uiState.update { it.copy(isBookmarked = true) } }
            }
        }
    }
}
