package id.tentuin.student.ui.screen.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.TestResult
import id.tentuin.student.data.repository.TestRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TestHistoryUiState(
    val history:   List<TestResult> = emptyList(),
    val isLoading: Boolean          = true,
    val error:     String?          = null,
)

@HiltViewModel
class TestHistoryViewModel @Inject constructor(
    private val testRepository: TestRepository,
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestHistoryUiState())
    val uiState: StateFlow<TestHistoryUiState> = _uiState.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val userId = sessionDataStore.userId.first()
            if (userId != null) {
                testRepository.getHistory(userId)
                    .onSuccess { list ->
                        _uiState.update { it.copy(history = list, isLoading = false) }
                    }
                    .onFailure { t ->
                        _uiState.update { it.copy(isLoading = false, error = t.message ?: "Gagal memuat riwayat test") }
                    }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "User tidak ditemukan") }
            }
        }
    }
}
