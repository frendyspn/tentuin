package id.tentuin.agent.ui.screen.claim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.friendlyApiError
import id.tentuin.agent.data.model.School
import id.tentuin.agent.data.model.SchoolClaim
import id.tentuin.agent.data.model.UniversityBrief
import id.tentuin.agent.data.model.UniversityClaim
import id.tentuin.agent.data.repository.ClaimRepository
import id.tentuin.agent.data.repository.SchoolRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClaimUiState(
    val schools:                List<School>                = emptyList(),
    val universities:           List<UniversityBrief>       = emptyList(),
    val schoolClaimsBySchoolId:     Map<String, SchoolClaim>     = emptyMap(),
    val universityClaimsByUniId:    Map<String, UniversityClaim> = emptyMap(),
    val myUserId:               String?                     = null,
    val searchQuery:            String                      = "",
    val isLoading:              Boolean                     = false,
    val claimingId:             String?                     = null,
    val error:                  String?                     = null,
    val successMessage:         String?                     = null,
    val dialogCode:             String?                     = null,
)

@HiltViewModel
class ClaimViewModel @Inject constructor(
    private val schoolRepository: SchoolRepository,
    private val claimRepository:  ClaimRepository,
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClaimUiState())
    val uiState: StateFlow<ClaimUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val uid = sessionDataStore.userId.first()
            _uiState.update { it.copy(myUserId = uid) }
        }
    }

    // ── Schools ──────────────────────────────────────────────────────────────

    fun loadSchools() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val schoolsResult = schoolRepository.search(_uiState.value.searchQuery)
            val claimsResult  = claimRepository.getAllActiveSchoolClaims()

            schoolsResult.fold(
                onSuccess = { schools ->
                    val claims = claimsResult.getOrDefault(emptyList())
                    val claimsMap = claims.associateBy { it.schoolId }
                    _uiState.update {
                        it.copy(
                            schools                 = schools,
                            schoolClaimsBySchoolId  = claimsMap,
                            isLoading               = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = friendlyApiError(e, "Gagal memuat sekolah."),
                        )
                    }
                }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            _uiState.update { it.copy(isLoading = true, error = null) }
            schoolRepository.search(query)
                .onSuccess { schools ->
                    _uiState.update { it.copy(schools = schools, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = friendlyApiError(e, "Gagal mencari sekolah."),
                        )
                    }
                }
        }
    }

    fun claimSchool(schoolId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(claimingId = schoolId, error = null) }
            claimRepository.claimSchool(schoolId)
                .onSuccess { claim ->
                    _uiState.update {
                        it.copy(
                            claimingId             = null,
                            schoolClaimsBySchoolId = it.schoolClaimsBySchoolId + (schoolId to claim),
                            dialogCode             = claim.claimCode,
                            successMessage         = "Klaim berhasil. Bagikan kode ke PIC sekolah.",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            claimingId = null,
                            error      = e.message ?: friendlyApiError(e, "Gagal mengklaim sekolah."),
                        )
                    }
                }
        }
    }

    // ── Universities ─────────────────────────────────────────────────────────

    fun loadUniversities() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val unisResult   = claimRepository.getAvailableUniversities()
            val claimsResult = claimRepository.getAllActiveUniversityClaims()

            unisResult.fold(
                onSuccess = { unis ->
                    val claims = claimsResult.getOrDefault(emptyList())
                    val claimsMap = claims.associateBy { it.universityId }
                    _uiState.update {
                        it.copy(
                            universities             = unis,
                            universityClaimsByUniId  = claimsMap,
                            isLoading                = false,
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = friendlyApiError(e, "Gagal memuat kampus."),
                        )
                    }
                }
            )
        }
    }

    fun claimUniversity(universityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(claimingId = universityId, error = null) }
            claimRepository.claimUniversity(universityId)
                .onSuccess { claim ->
                    _uiState.update {
                        it.copy(
                            claimingId               = null,
                            universityClaimsByUniId  = it.universityClaimsByUniId + (universityId to claim),
                            dialogCode               = claim.claimCode,
                            successMessage           = "Klaim berhasil. Bagikan kode ke PIC kampus.",
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            claimingId = null,
                            error      = e.message ?: friendlyApiError(e, "Gagal mengklaim kampus."),
                        )
                    }
                }
        }
    }

    // ── Misc ─────────────────────────────────────────────────────────────────

    fun showCodeDialog(code: String) = _uiState.update { it.copy(dialogCode = code) }
    fun dismissCodeDialog()           = _uiState.update { it.copy(dialogCode = null) }
    fun clearMessage()                = _uiState.update { it.copy(error = null, successMessage = null) }
}
