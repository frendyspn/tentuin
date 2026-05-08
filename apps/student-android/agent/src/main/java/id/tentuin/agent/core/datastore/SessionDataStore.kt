package id.tentuin.agent.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "agent_session")

class SessionDataStore(private val context: Context) {
    private val store = context.sessionDataStore

    companion object {
        private val KEY_ACCESS_TOKEN  = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID       = stringPreferencesKey("user_id")
    }

    val accessToken:  Flow<String?> = store.data.map { it[KEY_ACCESS_TOKEN] }
    val refreshToken: Flow<String?> = store.data.map { it[KEY_REFRESH_TOKEN] }
    val userId:       Flow<String?> = store.data.map { it[KEY_USER_ID] }

    suspend fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
            prefs[KEY_USER_ID]       = userId
        }
    }

    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        store.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN]  = accessToken
            prefs[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearSession() { store.edit { it.clear() } }
}
