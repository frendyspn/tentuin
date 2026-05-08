package id.tentuin.student.data.repository

import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.LoginRequest
import id.tentuin.student.data.model.RefreshRequest
import id.tentuin.student.data.model.SignupRequest
import id.tentuin.student.data.model.TokenResponse
import id.tentuin.student.data.model.UserMeta
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: SupabaseApi,
    private val sessionDataStore: SessionDataStore,
) {
    val accessToken = sessionDataStore.accessToken

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val response = api.login(body = LoginRequest(email, password))
        val userId = response.user?.id ?: error("No user in response")
        sessionDataStore.saveSession(response.accessToken, response.refreshToken, userId)
        response
    }

    suspend fun register(email: String, password: String, fullName: String): Result<TokenResponse> = runCatching {
        val response = api.register(SignupRequest(email, password, UserMeta(fullName)))
        val userId = response.user?.id ?: error("No user in response")
        sessionDataStore.saveSession(response.accessToken, response.refreshToken, userId)
        response
    }

    suspend fun logout() {
        val token = sessionDataStore.accessToken.first()
        if (token != null) {
            runCatching { api.logout("Bearer $token") }
        }
        sessionDataStore.clearSession()
    }

    suspend fun refreshSession(): Result<TokenResponse> = runCatching {
        val refreshToken = sessionDataStore.refreshToken.first() ?: error("No refresh token")
        val response = api.refreshToken(body = RefreshRequest(refreshToken))
        sessionDataStore.updateAccessToken(response.accessToken, response.refreshToken)
        response
    }
}
