package id.tentuin.admin.data.repository

import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.LoginRequest
import id.tentuin.admin.data.model.TokenResponse
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

class NotAdminException : Exception("Akun ini bukan admin")

@Singleton
class AuthRepository @Inject constructor(
    private val api:     AdminApi,
    private val session: SessionDataStore,
) {
    val accessToken = session.accessToken
    val userId      = session.userId
    val role        = session.role
    val fullName    = session.fullName

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val res = api.login(body = LoginRequest(email, password))
        val uid = res.user?.id ?: error("Tidak ada user pada response login")

        // Sementara simpan token agar request profile pakai JWT user (RLS).
        session.saveSession(
            accessToken  = res.accessToken,
            refreshToken = res.refreshToken,
            userId       = uid,
            role         = "",
            fullName     = null,
        )

        val profiles = runCatching { api.getProfile(id = "eq.$uid") }
            .getOrElse {
                session.clearSession()
                throw it
            }

        val profile = profiles.firstOrNull()
        if (profile == null || profile.role !in ADMIN_ROLES) {
            // Bukan admin → bersihkan session & lemparkan error.
            runCatching { api.logout("Bearer ${res.accessToken}") }
            session.clearSession()
            throw NotAdminException()
        }

        session.saveSession(
            accessToken  = res.accessToken,
            refreshToken = res.refreshToken,
            userId       = uid,
            role         = profile.role,
            fullName     = profile.fullName,
        )
        res
    }

    suspend fun logout() {
        val token = session.accessToken.first()
        if (token != null) runCatching { api.logout("Bearer $token") }
        session.clearSession()
    }

    companion object {
        val ADMIN_ROLES = setOf("admin", "super_admin")
    }
}
