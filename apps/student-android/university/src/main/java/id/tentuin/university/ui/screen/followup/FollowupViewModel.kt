package id.tentuin.university.ui.screen.followup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
import id.tentuin.university.data.model.FollowupActivity
import id.tentuin.university.data.model.ProspectFollowup
import id.tentuin.university.data.repository.ProspectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowupUiState(
    val followup:    ProspectFollowup? = null,
    val activities:  List<FollowupActivity> = emptyList(),
    val loading:     Boolean = false,
    val submitting:  Boolean = false,
    val toast:       String? = null,
    val error:       String? = null,
)

@HiltViewModel
class FollowupViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: ProspectRepository,
) : ViewModel() {
    private val followupId: String = savedStateHandle["id"] ?: error("Missing followup id")

    private val _s = MutableStateFlow(FollowupUiState())
    val state: StateFlow<FollowupUiState> = _s.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            val fu = repo.getFollowup(followupId).getOrNull()
            val acts = repo.listActivities(followupId).getOrDefault(emptyList())
            _s.update { it.copy(loading = false, followup = fu, activities = acts) }
        }
    }

    fun logActivity(type: String, note: String?) {
        viewModelScope.launch {
            _s.update { it.copy(submitting = true, error = null) }
            repo.logActivity(followupId, type, note)
                .onSuccess { r ->
                    if (r.success) {
                        _s.update { it.copy(submitting = false, toast = "Aktivitas dicatat") }
                        load()
                    } else {
                        _s.update { it.copy(submitting = false, error = r.message) }
                    }
                }
                .onFailure { e -> _s.update { it.copy(submitting = false, error = friendlyApiError(e)) } }
        }
    }

    fun changeStatus(newStatus: String, note: String? = null) {
        viewModelScope.launch {
            _s.update { it.copy(submitting = true, error = null) }
            repo.changeStatus(followupId, newStatus, note)
                .onSuccess { r ->
                    _s.update { it.copy(submitting = false, toast = r.message) }
                    load()
                }
                .onFailure { e -> _s.update { it.copy(submitting = false, error = friendlyApiError(e)) } }
        }
    }

    fun clearMessages() = _s.update { it.copy(toast = null, error = null) }
}
