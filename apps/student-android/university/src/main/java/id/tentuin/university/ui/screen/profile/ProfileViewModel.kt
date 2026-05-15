package id.tentuin.university.ui.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.university.data.model.AuthUserDetail
import id.tentuin.university.data.model.UniversityAccount
import id.tentuin.university.data.repository.AccountRepository
import id.tentuin.university.data.repository.AuthRepository
import id.tentuin.university.data.repository.TeamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user:        AuthUserDetail? = null,
    val account:     UniversityAccount? = null,
    val loading:     Boolean = false,
    val loggedOut:   Boolean = false,
    val error:       String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepo:    AuthRepository,
    private val accountRepo: AccountRepository,
    private val teamRepo:    TeamRepository,
) : ViewModel() {
    private val _s = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _s.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _s.update { it.copy(loading = true) }
            val user = authRepo.getCurrentUser().getOrNull()
            val acc  = accountRepo.getMyAccount().getOrNull()
            _s.update { it.copy(loading = false, user = user, account = acc) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _s.update { it.copy(loggedOut = true) }
        }
    }

    fun leaveTeam() {
        val acc = _s.value.account ?: return
        if (acc.accountType != "enterprise") return
        viewModelScope.launch {
            teamRepo.leaveTeam(acc.id)
            load()
        }
    }
}
