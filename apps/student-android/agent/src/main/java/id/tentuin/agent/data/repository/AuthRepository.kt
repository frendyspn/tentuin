package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    val accessToken = session.accessToken
    val userId = session.userId

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val res = api.login(body = LoginRequest(email, password))
        val uid = res.user?.id ?: error("No user in response")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<TokenResponse> = runCatching {
        val res = api.register(RegisterRequest(email, password, AgentMeta(fullName, phone)))
        val uid = res.user?.id ?: error("Registrasi berhasil, silakan login.")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    suspend fun logout() {
        val token = session.accessToken.first()
        if (token != null) runCatching { api.logout("Bearer $token") }
        session.clearSession()
    }
}
