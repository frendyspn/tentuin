package id.tentuin.student.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.UniversityBookmark
import id.tentuin.student.data.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookmarksUiState(
    val bookmarks: List<UniversityBookmark> = emptyList(),
    val isLoading: Boolean = true,
    val error:     String? = null,
)

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository,
    private val sessionDataStore:   SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarksUiState())
    val uiState: StateFlow<BookmarksUiState> = _uiState.asStateFlow()

    init {
        loadBookmarks()
    }

    fun loadBookmarks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = sessionDataStore.userId.first() ?: run {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }

            bookmarkRepository.getBookmarks(userId)
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, bookmarks = list) } }
                .onFailure { t  -> _uiState.update { it.copy(isLoading = false, error = t.message) } }
        }
    }

    fun removeBookmark(universityId: String) {
        viewModelScope.launch {
            val userId = sessionDataStore.userId.first() ?: return@launch
            bookmarkRepository.deleteBookmark(userId, universityId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(bookmarks = state.bookmarks.filter { it.universityId != universityId })
                    }
                }
        }
    }
}
