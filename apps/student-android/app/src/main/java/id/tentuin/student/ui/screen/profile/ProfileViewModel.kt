package id.tentuin.student.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.Profile
import id.tentuin.student.data.repository.AuthRepository
import id.tentuin.student.data.repository.BookmarkRepository
import id.tentuin.student.data.repository.ProfileRepository
import id.tentuin.student.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: Profile? = null,
    val isLoading: Boolean = true,
    val isGuest: Boolean = false,
    val testCount: Int = 0,
    val bookmarkCount: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository,
    private val testRepo: TestRepository,
    private val bookmarkRepo: BookmarkRepository,
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = sessionDataStore.userId.first()
            
            if (userId == null) {
                _uiState.update { it.copy(isGuest = true, isLoading = false) }
                return@launch
            }

            val profileResult = profileRepo.getProfile(userId)
            val historyResult = testRepo.getTestHistory(userId)
            val bookmarksResult = bookmarkRepo.getBookmarks(userId)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    profile = profileResult.getOrNull(),
                    testCount = historyResult.getOrNull()?.size ?: 0,
                    bookmarkCount = bookmarksResult.getOrNull()?.size ?: 0,
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
        }
    }
}
