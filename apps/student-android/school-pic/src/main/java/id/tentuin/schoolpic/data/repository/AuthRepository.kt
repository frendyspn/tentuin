package id.tentuin.schoolpic.data.repository

import id.tentuin.schoolpic.core.datastore.SessionDataStore
import id.tentuin.schoolpic.core.network.SchoolPicApi
import id.tentuin.schoolpic.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: SchoolPicApi,
    private val session: SessionDataStore,
) {
    val accessToken = session.accessToken
    val userId      = session.userId
    val schoolId    = session.schoolId

    suspend fun login(email: String, password: String): Result<TokenResponse> = runCatching {
        val res = api.login(body = LoginRequest(email, password))
        val uid = res.user?.id ?: error("No user in response")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    suspend fun register(email: String, password: String, fullName: String): Result<TokenResponse> = runCatching {
        val res = api.register(RegisterRequest(email, password, PicMeta(fullName)))
        val uid = res.user?.id ?: error("Registrasi berhasil, silakan login.")
        session.saveSession(res.accessToken, res.refreshToken, uid)
        res
    }

    /** Setelah signup berhasil & token tersimpan, ikat user ke sekolah via claim_code. */
    suspend fun bindToSchool(claimCode: String): Result<BindResult> = runCatching {
        val res = api.bindSchoolPicToSchool(BindRequest(claimCode.trim().uppercase()))
        val first = res.firstOrNull() ?: error("Server tidak merespon.")
        if (!first.success) error(first.message)
        first.schoolId?.let { session.saveSchoolId(it) }
        first
    }

    /** Sinkronkan school_id ke datastore (dipanggil saat splash kalau session valid). */
    suspend fun syncSchoolIdFromProfile(): Result<String?> = runCatching {
        val uid = session.userId.first() ?: return@runCatching null
        val rows = api.getProfile(id = "eq.$uid", select = "id,school_id,role")
        val schoolId = rows.firstOrNull()?.schoolId
        if (schoolId != null) session.saveSchoolId(schoolId)
        schoolId
    }

    suspend fun logout() {
        val token = session.accessToken.first()
        if (token != null) runCatching { api.logout("Bearer $token") }
        session.clearSession()
    }
}
