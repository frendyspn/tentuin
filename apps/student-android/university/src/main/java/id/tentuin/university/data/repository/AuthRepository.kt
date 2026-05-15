package id.tentuin.university.data.repository

import id.tentuin.university.core.datastore.SessionDataStore
import id.tentuin.university.core.network.UniversityApi
import id.tentuin.university.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api:     UniversityApi,
    private val session: SessionDataStore,
) {
    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val res = api.login(body = LoginRequest(email, password))
        session.saveSession(res.accessToken, res.refreshToken, res.user?.id ?: "")
        res
    }

    suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<TokenResponse> = runCatching {
        val res = api.register(RegisterRequest(email, password, UserMeta(fullName, phone)))
        if (res.accessToken.isNotEmpty()) {
            session.saveSession(res.accessToken, res.refreshToken, res.user?.id ?: "")
        }
        res
    }

    suspend fun logout(): Result<Unit> = runCatching {
        val token = session.accessToken.first()
        if (!token.isNullOrEmpty()) {
            runCatching { api.logout("Bearer $token") }
        }
        session.clearSession()
    }

    suspend fun getCurrentUser(): Result<AuthUserDetail> = runCatching { api.getAuthUser() }

    suspend fun isLoggedIn(): Boolean = session.accessToken.first() != null
}
