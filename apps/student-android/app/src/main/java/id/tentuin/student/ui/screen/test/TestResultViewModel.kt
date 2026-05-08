package id.tentuin.student.ui.screen.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.data.model.MajorRow
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.repository.ExploreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestResultUiState(
    val riasecCode: String = "",
    val majors: List<MajorRow> = emptyList(), // Unique majors to display
    val allRecommendedMajors: List<MajorRow> = emptyList(), // All major rows (with university_id)
    val universities: List<UniversityRow> = emptyList(),
    val filteredUniversities: List<UniversityRow> = emptyList(),
    val selectedMajorNames: List<String> = emptyList(),
    val loadingRecommendations: Boolean = true,
)

@HiltViewModel
class TestResultViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TestResultUiState())
    val uiState: StateFlow<TestResultUiState> = _uiState.asStateFlow()

    fun loadRecommendations(riasecCode: String) {
        if (_uiState.value.riasecCode == riasecCode) return
        _uiState.update { it.copy(riasecCode = riasecCode, loadingRecommendations = true) }
        
        viewModelScope.launch {
            val dominantCode = riasecCode.firstOrNull()?.toString() ?: ""
            val allMajorsRes = exploreRepository.fetchMajors()
            val allUnisRes = exploreRepository.fetchUniversities()
            
            val allMajors = allMajorsRes.getOrNull() ?: emptyList()
            val allUnis = allUnisRes.getOrNull() ?: emptyList()
            
            val recommendedMajors = allMajors.filter { it.riasecCodes.contains(dominantCode) }
            val uniqueMajors = recommendedMajors.distinctBy { it.name.trim().lowercase() }
            
            _uiState.update { 
                it.copy(
                    allRecommendedMajors = recommendedMajors,
                    majors = uniqueMajors,
                    universities = allUnis,
                    loadingRecommendations = false
                ) 
            }
            filterUniversities()
        }
    }

    fun toggleMajor(majorName: String) {
        _uiState.update { state ->
            val currentSelected = state.selectedMajorNames.toMutableList()
            if (currentSelected.contains(majorName)) {
                currentSelected.remove(majorName)
            } else {
                if (currentSelected.size < 3) {
                    currentSelected.add(majorName)
                }
            }
            state.copy(selectedMajorNames = currentSelected)
        }
        filterUniversities()
    }

    private fun filterUniversities() {
        _uiState.update { state ->
            val selected = state.selectedMajorNames
            val matchingUniversities = if (selected.isEmpty()) {
                val universityIdsWithRecommended = state.allRecommendedMajors.map { it.universityId }.toSet()
                state.universities.filter { it.id in universityIdsWithRecommended }
            } else {
                val validUniversityIds = state.allRecommendedMajors
                    .filter { major -> selected.any { it.equals(major.name, ignoreCase = true) } }
                    .map { it.universityId }
                    .toSet()
                state.universities.filter { it.id in validUniversityIds }
            }
            state.copy(filteredUniversities = matchingUniversities)
        }
    }
}
