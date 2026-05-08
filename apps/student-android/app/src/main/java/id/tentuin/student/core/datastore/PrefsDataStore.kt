package id.tentuin.student.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.prefsDataStore by preferencesDataStore(name = "prefs")

class PrefsDataStore(
    private val context: Context,
) {
    private val store = context.prefsDataStore

    companion object {
        private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val onboardingDone: Flow<Boolean> = store.data.map { it[KEY_ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone() {
        store.edit { it[KEY_ONBOARDING_DONE] = true }
    }
}
