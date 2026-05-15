package id.tentuin.agent.ui.screen.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.network.friendlyApiError
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.data.repository.ClaimRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioUiState(
    val schoolClaims:     List<SchoolClaim>     = emptyList(),
    val universityClaims: List<UniversityClaim> = emptyList(),
    val isLoading:        Boolean               = true,
    val error:            String?               = null,
    val dialogCode:       String?               = null,
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val claimRepository: ClaimRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PortfolioUiState())
    val uiState: StateFlow<PortfolioUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val schools = claimRepository.getMySchoolClaims()
            val unis    = claimRepository.getMyUniversityClaims()

            val anyFailure = schools.exceptionOrNull() ?: unis.exceptionOrNull()
            _uiState.update {
                it.copy(
                    schoolClaims     = schools.getOrDefault(emptyList()),
                    universityClaims = unis.getOrDefault(emptyList()),
                    isLoading        = false,
                    error            = anyFailure?.let { e -> friendlyApiError(e) },
                )
            }
        }
    }

    fun showCodeDialog(code: String) = _uiState.update { it.copy(dialogCode = code) }
    fun dismissCodeDialog()           = _uiState.update { it.copy(dialogCode = null) }
    fun clearError()                  = _uiState.update { it.copy(error = null) }
}
