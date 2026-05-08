package id.tentuin.student.ui.screen.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.PrefsDataStore
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.ForceUpdateResult
import id.tentuin.student.data.repository.AppConfigRepository
import id.tentuin.student.ui.navigation.Route
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    data class Navigate(val route: String) : SplashState()
    data class ForceUpdate(val storeUrl: String) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val prefsDataStore: PrefsDataStore,
    private val appConfigRepository: AppConfigRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val forceUpdateResult = appConfigRepository.checkForceUpdate()
            if (forceUpdateResult is ForceUpdateResult.UpdateRequired) {
                _state.value = SplashState.ForceUpdate(forceUpdateResult.storeUrl)
                return@launch
            }

            val accessToken   = sessionDataStore.accessToken.first()
            val onboardingDone = prefsDataStore.onboardingDone.first()

            val destination = when {
                !onboardingDone -> Route.Onboarding.route
                accessToken != null -> Route.Home.route
                else -> Route.Login.route
            }
            _state.value = SplashState.Navigate(destination)
        }
    }
}
