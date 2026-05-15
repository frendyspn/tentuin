package id.tentuin.university.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
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

data class DashboardUiState(
    val loading:         Boolean = false,
    val account:         UniversityAccount? = null,
    val activeFollowups: List<ProspectFollowup> = emptyList(),
    val error:           String? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val accountRepo:  AccountRepository,
    private val prospectRepo: ProspectRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _s.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            accountRepo.getMyAccount().onSuccess { acc ->
                if (acc == null) {
                    _s.update { it.copy(loading = false, error = "Account belum dibuat.") }
                    return@onSuccess
                }
                val fu = prospectRepo.listFollowups(
                    accountId = acc.id,
                    statusFilter = "in.(claimed,contacted,qualified)",
                ).getOrDefault(emptyList())
                _s.update { it.copy(loading = false, account = acc, activeFollowups = fu) }
            }.onFailure { e ->
                _s.update { it.copy(loading = false, error = friendlyApiError(e)) }
            }
        }
    }
}
