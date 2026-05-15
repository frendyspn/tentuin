package id.tentuin.university.ui.screen.subscribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.core.network.friendlyApiError
import id.tentuin.university.data.model.SubscribeLog
import id.tentuin.university.data.model.SubscriptionPlan
import id.tentuin.university.data.model.UniversityAccount
import id.tentuin.university.data.repository.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscribeUiState(
    val account:    UniversityAccount? = null,
    val plans:      List<SubscriptionPlan> = emptyList(),
    val history:    List<SubscribeLog> = emptyList(),
    val loading:    Boolean = false,
    val submitting: Boolean = false,
    val toast:      String? = null,
    val error:      String? = null,
)

@HiltViewModel
class SubscribeViewModel @Inject constructor(
    private val accountRepo: AccountRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(SubscribeUiState())
    val state: StateFlow<SubscribeUiState> = _s.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true, error = null) }
            val acc = accountRepo.getMyAccount().getOrNull()
            if (acc == null) {
                _s.update { it.copy(loading = false, error = "Account tidak ditemukan.") }
                return@launch
            }
            val plans = accountRepo.getPlans(acc.accountType).getOrDefault(emptyList())
            val hist  = accountRepo.getSubscribeHistory(acc.id).getOrDefault(emptyList())
            _s.update { it.copy(loading = false, account = acc, plans = plans, history = hist) }
        }
    }

    fun subscribe(planCode: String) {
        viewModelScope.launch {
            val acc = _s.value.account ?: return@launch
            _s.update { it.copy(submitting = true, error = null) }
            accountRepo.subscribe(acc.id, planCode)
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

    fun clearMessages() = _s.update { it.copy(toast = null, error = null) }
}
