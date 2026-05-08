package id.tentuin.student.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.Profile
import id.tentuin.student.data.model.TestResult
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.repository.ExploreRepository
import id.tentuin.student.data.repository.ProfileRepository
import id.tentuin.student.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val profile: Profile? = null,
    val lastTestResult: TestResult? = null,
    val featuredUniversities: List<UniversityRow> = emptyList(),
    val isLoading: Boolean = true,
    val isGuest: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val profileRepository: ProfileRepository,
    private val testRepository: TestRepository,
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun refresh() = loadData()

    private fun loadData() {
        viewModelScope.launch {
            val userId = sessionDataStore.userId.first()
            val isGuest = userId == null

            // Always load featured universities (public endpoint)
            val universities = exploreRepository.fetchUniversities()
                .getOrDefault(emptyList())
                .filter { it.isPartner }
                .take(5)

            if (isGuest) {
                _uiState.value = HomeUiState(
                    featuredUniversities = universities,
                    isLoading = false,
                    isGuest = true,
                )
                return@launch
            }

            val profile    = profileRepository.getProfile(userId!!).getOrNull()
            val lastResult = testRepository.getLatestResult(userId).getOrNull()

            _uiState.value = HomeUiState(
                profile              = profile,
                lastTestResult       = lastResult,
                featuredUniversities = universities,
                isLoading            = false,
                isGuest              = false,
            )
        }
    }
}
