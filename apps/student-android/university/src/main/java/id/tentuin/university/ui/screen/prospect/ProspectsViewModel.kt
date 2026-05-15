package id.tentuin.university.ui.screen.prospect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
import id.tentuin.university.data.model.Prospect
import id.tentuin.university.data.model.ProspectFollowup
import id.tentuin.university.data.model.UniversityAccount
import id.tentuin.university.data.repository.AccountRepository
import id.tentuin.university.data.repository.ProspectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ProspectTab { DISCOVER, MY_FOLLOWUPS }

data class ProspectsUiState(
    val tab:           ProspectTab = ProspectTab.DISCOVER,
    val account:       UniversityAccount? = null,
    val prospects:     List<Prospect> = emptyList(),
    val followups:     List<ProspectFollowup> = emptyList(),
    val loading:       Boolean = false,
    val unlockingId:   String? = null,
    val toast:         String? = null,
    val error:         String? = null,
    val query:         String = "",
)

@HiltViewModel
class ProspectsViewModel @Inject constructor(
    private val accountRepo:  AccountRepository,
    private val prospectRepo: ProspectRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(ProspectsUiState())
    val state: StateFlow<ProspectsUiState> = _s.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            val acc = accountRepo.getMyAccount().getOrNull()
            if (acc == null) {
                _s.update { it.copy(loading = false, error = "Account tidak ditemukan.") }
                return@launch
            }
            val prospects = prospectRepo.listProspects().getOrDefault(emptyList())
            val followups = prospectRepo.listFollowups(acc.id).getOrDefault(emptyList())
            _s.update { it.copy(loading = false, account = acc, prospects = prospects, followups = followups) }
        }
    }

    fun setTab(tab: ProspectTab) { _s.update { it.copy(tab = tab) } }

    fun search(q: String) {
        _s.update { it.copy(query = q) }
        viewModelScope.launch {
            if (q.isBlank()) {
                val list = prospectRepo.listProspects().getOrDefault(emptyList())
                _s.update { it.copy(prospects = list) }
            } else {
                val list = prospectRepo.searchProspects(q).getOrDefault(emptyList())
                _s.update { it.copy(prospects = list) }
            }
        }
    }

    fun unlock(prospect: Prospect, onUnlocked: (followupId: String) -> Unit = {}) {
        val acc = _s.value.account ?: return
        viewModelScope.launch {
            _s.update { it.copy(unlockingId = prospect.id, error = null) }
            prospectRepo.unlockProspect(acc.id, prospect.id)
                .onSuccess { r ->
                    if (r.success) {
                        _s.update { it.copy(unlockingId = null, toast = r.message) }
                        r.followupId?.let(onUnlocked)
                        load()
                    } else {
                        _s.update { it.copy(unlockingId = null, error = r.message) }
                    }
                }
                .onFailure { e ->
                    _s.update { it.copy(unlockingId = null, error = friendlyApiError(e)) }
                }
        }
    }

    fun clearMessages() = _s.update { it.copy(toast = null, error = null) }
}
