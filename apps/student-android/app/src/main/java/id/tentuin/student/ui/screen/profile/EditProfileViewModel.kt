package id.tentuin.student.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.Profile
import id.tentuin.student.data.model.UpdateProfileRequest
import id.tentuin.student.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val profile:    Profile? = null,
    val isLoading:  Boolean  = true,
    val isSaving:   Boolean  = false,
    val isSaved:    Boolean  = false,
    val error:      String?  = null,

    // Form fields
    val fullName:   String = "",
    val schoolName: String = "",
    val schoolOptions: List<String> = emptyList(),
    val city:       String = "",
    val cityOptions: List<String> = emptyList(),
    val birthYear:  String = "",
    val grade:      Int?   = null,
    val nisn:       String = "",
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepo:      ProfileRepository,
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        // ... (existing code, unchanged)
        viewModelScope.launch {
            val userId = sessionDataStore.userId.first() ?: run {
                _uiState.update { it.copy(isLoading = false, error = "Tidak ada sesi aktif") }
                return@launch
            }

            profileRepo.getProfile(userId)
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            profile    = profile,
                            isLoading  = false,
                            fullName   = profile?.fullName   ?: "",
                            schoolName = profile?.schoolName ?: "",
                            city       = profile?.city       ?: "",
                            birthYear  = profile?.birthYear?.toString() ?: "",
                            grade      = profile?.grade,
                            nisn       = profile?.nisn       ?: "",
                        )
                    }
                }
                .onFailure { t ->
                    _uiState.update { it.copy(isLoading = false, error = t.message) }
                }
        }
    }

    fun onFullNameChange(v: String)   { _uiState.update { it.copy(fullName = v, isSaved = false) } }
    
    fun onSchoolNameChange(v: String) { 
        _uiState.update { it.copy(schoolName = v, isSaved = false) } 
        if (v.length >= 5) {
            viewModelScope.launch {
                val results = profileRepo.searchSchools("ilike.*$v*")
                _uiState.update { it.copy(schoolOptions = results.getOrDefault(emptyList()).map { s -> s.name }) }
            }
        }
    }

    fun onCityChange(v: String) { 
        _uiState.update { it.copy(city = v, isSaved = false) } 
        if (v.length >= 5) {
            viewModelScope.launch {
                val results = profileRepo.searchCities("ilike.*$v*")
                _uiState.update { it.copy(cityOptions = results.getOrDefault(emptyList()).map { c -> c.name }) }
            }
        }
    }

    fun onBirthYearChange(v: String)  {
        if (v.isEmpty() || v.all { it.isDigit() }) {
            _uiState.update { it.copy(birthYear = v, isSaved = false) }
        }
    }
    fun onGradeChange(v: Int)         { _uiState.update { it.copy(grade = v, isSaved = false) } }
    fun onNisnChange(v: String)       { _uiState.update { it.copy(nisn = v, isSaved = false) } }

    fun saveProfile() {
        // ... (existing code, unchanged)
        viewModelScope.launch {
            val userId = sessionDataStore.userId.first() ?: return@launch
            _uiState.update { it.copy(isSaving = true, error = null) }

            val request = UpdateProfileRequest(
                fullName   = _uiState.value.fullName.ifBlank { null },
                schoolName = _uiState.value.schoolName.ifBlank { null },
                city       = _uiState.value.city.ifBlank { null },
                birthYear  = _uiState.value.birthYear.toIntOrNull(),
                grade      = _uiState.value.grade,
                nisn       = _uiState.value.nisn.ifBlank { null },
            )

            profileRepo.updateProfile(userId, request)
                .onSuccess { _uiState.update { it.copy(isSaving = false, isSaved = true) } }
                .onFailure { t -> _uiState.update { it.copy(isSaving = false, error = t.message) } }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
