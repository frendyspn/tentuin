package id.tentuin.university.ui.screen.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
import id.tentuin.university.data.model.AccountMember
import id.tentuin.university.data.model.UniversityAccount
import id.tentuin.university.data.repository.AccountRepository
import id.tentuin.university.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeamUiState(
    val account: UniversityAccount? = null,
    val members: List<AccountMember> = emptyList(),
    val loading: Boolean = false,
    val submitting: Boolean = false,
    val toast:   String? = null,
    val error:   String? = null,
)

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
    private val teamRepo:    TeamRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(TeamUiState())
    val state: StateFlow<TeamUiState> = _s.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            val acc = accountRepo.getMyAccount().getOrNull()
            if (acc == null) {
                _s.update { it.copy(loading = false, error = "Account tidak ditemukan.") }
                return@launch
            }
            val members = if (acc.accountType == "enterprise")
                teamRepo.listMembers(acc.id).getOrDefault(emptyList())
            else emptyList()
            _s.update { it.copy(loading = false, account = acc, members = members) }
        }
    }

    fun addMember(userId: String) {
        val acc = _s.value.account ?: return
        if (acc.accountType != "enterprise") return
        viewModelScope.launch {
            _s.update { it.copy(submitting = true, error = null) }
            teamRepo.addMember(acc.id, userId.trim())
                .onSuccess { r ->
                    if (r.success) {
                        _s.update { it.copy(submitting = false, toast = r.message) }
                        load()
                    } else {
                        _s.update { it.copy(submitting = false, error = r.message) }
                    }
                }
                .onFailure { e -> _s.update { it.copy(submitting = false, error = friendlyApiError(e)) } }
        }
    }

    fun leave() {
        val acc = _s.value.account ?: return
        viewModelScope.launch {
            _s.update { it.copy(submitting = true, error = null) }
            teamRepo.leaveTeam(acc.id)
                .onSuccess { r ->
                    _s.update { it.copy(submitting = false, toast = r.message) }
                    if (r.success) load()
                }
                .onFailure { e -> _s.update { it.copy(submitting = false, error = friendlyApiError(e)) } }
        }
    }

    fun clearMessages() = _s.update { it.copy(toast = null, error = null) }
}
