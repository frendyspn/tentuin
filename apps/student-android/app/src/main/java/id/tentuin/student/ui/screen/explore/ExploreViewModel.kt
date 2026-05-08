package id.tentuin.student.ui.screen.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.data.model.ExploreTab
import id.tentuin.student.data.model.MajorRow
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.repository.ExploreRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNIVERSITY_PAGE_SIZE = 12
private const val MAJOR_PAGE_SIZE = 12

data class ExploreUiState(
    val activeTab: ExploreTab = ExploreTab.UNIVERSITIES,
    val searchQuery: String = "",
    val typeFilter: String = "Semua",
    val riasecCode: String? = null,
    val universities: List<UniversityRow> = emptyList(),
    val majors: List<MajorRow> = emptyList(),
    val searchResults: List<UniversityRow>? = null,
    val loadingUniversities: Boolean = true,
    val loadingMajors: Boolean = true,
    val searching: Boolean = false,
    val visibleUniversityCount: Int = UNIVERSITY_PAGE_SIZE,
    val visibleMajorCount: Int = MAJOR_PAGE_SIZE,
    val visibleUniversities: List<UniversityRow> = emptyList(),
    val visibleMajors: List<MajorRow> = emptyList(),
    val filteredUniversitiesCount: Int = 0,
    val filteredMajorsCount: Int = 0,
    val hasMoreVisibleItems: Boolean = false,
    val isLoadingVisible: Boolean = true,
    val isEmptyVisible: Boolean = false,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        refreshUniversities()
        refreshMajors()
    }

    fun onTabChanged(tab: ExploreTab) {
        _uiState.update { current ->
            current.copy(
                activeTab = tab,
                searchQuery = "",
                typeFilter = "Semua",
                riasecCode = null,
                searchResults = null,
                searching = false,
                visibleUniversityCount = UNIVERSITY_PAGE_SIZE,
                visibleMajorCount = MAJOR_PAGE_SIZE,
            )
        }
        searchJob?.cancel()
        recomputeVisibleLists()
    }

    fun onSearchChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val currentTab = _uiState.value.activeTab
        searchJob?.cancel()

        if (currentTab == ExploreTab.UNIVERSITIES) {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                _uiState.update { it.copy(searchResults = null, searching = false, visibleUniversityCount = UNIVERSITY_PAGE_SIZE) }
                recomputeVisibleLists()
                return
            }

            searchJob = viewModelScope.launch {
                delay(400)
                _uiState.update { it.copy(searching = true) }
                repository.searchUniversities(trimmed)
                    .onSuccess { results ->
                        _uiState.update {
                            it.copy(
                                searchResults = results,
                                searching = false,
                                visibleUniversityCount = UNIVERSITY_PAGE_SIZE,
                            )
                        }
                        recomputeVisibleLists()
                    }
                    .onFailure {
                        _uiState.update { state ->
                            state.copy(searching = false, searchResults = state.searchResults ?: state.universities)
                        }
                        recomputeVisibleLists()
                    }
            }
        } else {
            recomputeVisibleLists()
        }
    }

    fun onUniversityFilterChanged(filter: String) {
        _uiState.update {
            it.copy(
                typeFilter = filter,
                visibleUniversityCount = UNIVERSITY_PAGE_SIZE,
            )
        }
        recomputeVisibleLists()
    }

    fun onRiasecFilterChanged(code: String?) {
        _uiState.update {
            it.copy(
                riasecCode = code,
                visibleMajorCount = MAJOR_PAGE_SIZE,
            )
        }
        recomputeVisibleLists()
    }

    fun loadMoreVisibleItems() {
        val current = _uiState.value
        when (current.activeTab) {
            ExploreTab.UNIVERSITIES -> {
                if (current.visibleUniversityCount < current.filteredUniversitiesCount && !current.searching) {
                    _uiState.update { it.copy(visibleUniversityCount = (it.visibleUniversityCount + UNIVERSITY_PAGE_SIZE).coerceAtMost(it.filteredUniversitiesCount)) }
                    recomputeVisibleLists()
                }
            }
            ExploreTab.MAJORS -> {
                if (current.visibleMajorCount < current.filteredMajorsCount) {
                    _uiState.update { it.copy(visibleMajorCount = (it.visibleMajorCount + MAJOR_PAGE_SIZE).coerceAtMost(it.filteredMajorsCount)) }
                    recomputeVisibleLists()
                }
            }
        }
    }

    private fun refreshUniversities() {
        viewModelScope.launch {
            repository.fetchUniversities()
                .onSuccess { universities ->
                    _uiState.update { it.copy(universities = universities, loadingUniversities = false) }
                    recomputeVisibleLists()
                }
                .onFailure {
                    _uiState.update { it.copy(loadingUniversities = false) }
                    recomputeVisibleLists()
                }
        }
    }

    private fun refreshMajors() {
        viewModelScope.launch {
            repository.fetchMajors()
                .onSuccess { majors ->
                    _uiState.update { it.copy(majors = majors, loadingMajors = false) }
                    recomputeVisibleLists()
                }
                .onFailure {
                    _uiState.update { it.copy(loadingMajors = false) }
                    recomputeVisibleLists()
                }
        }
    }

    private fun recomputeVisibleLists() {
        val current = _uiState.value
        val universitySource = current.searchResults ?: current.universities
        val filteredUniversities = universitySource.filter { university ->
            current.typeFilter == "Semua" || university.type.equals(current.typeFilter, ignoreCase = true)
        }
        val uniqueMajors = current.majors
            .asSequence()
            .distinctBy { it.name.trim().lowercase() }
            .toList()
        val filteredMajors = uniqueMajors.filter { major ->
            val matchSearch = current.searchQuery.isBlank() || major.name.contains(current.searchQuery, ignoreCase = true)
            val matchRiasec = current.riasecCode == null || major.riasecCodes.contains(current.riasecCode)
            matchSearch && matchRiasec
        }

        val visibleUniversities = filteredUniversities.take(current.visibleUniversityCount)
        val visibleMajors = filteredMajors.take(current.visibleMajorCount)

        _uiState.update {
            it.copy(
                visibleUniversities = visibleUniversities,
                visibleMajors = visibleMajors,
                filteredUniversitiesCount = filteredUniversities.size,
                filteredMajorsCount = filteredMajors.size,
                hasMoreVisibleItems = when (it.activeTab) {
                    ExploreTab.UNIVERSITIES -> it.visibleUniversityCount < filteredUniversities.size
                    ExploreTab.MAJORS -> it.visibleMajorCount < filteredMajors.size
                },
                isLoadingVisible = when (it.activeTab) {
                    ExploreTab.UNIVERSITIES -> it.loadingUniversities
                    ExploreTab.MAJORS -> it.loadingMajors
                },
                isEmptyVisible = when (it.activeTab) {
                    ExploreTab.UNIVERSITIES -> !it.loadingUniversities && visibleUniversities.isEmpty()
                    ExploreTab.MAJORS -> !it.loadingMajors && visibleMajors.isEmpty()
                },
            )
        }
    }
}
