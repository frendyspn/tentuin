package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaimRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    // ── Schools ──────────────────────────────────────────────────────────────

    suspend fun getMySchoolClaims(): Result<List<SchoolClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getSchoolClaims(agentId = "eq.$userId")
    }

    /** Semua klaim sekolah pending/active (lintas agen) — untuk tahu sekolah mana yang sudah dipegang. */
    suspend fun getAllActiveSchoolClaims(): Result<List<SchoolClaim>> = runCatching {
        api.getAllActiveSchoolClaims()
    }

    suspend fun claimSchool(schoolId: String): Result<SchoolClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.checkSchoolClaim(schoolId = "eq.$schoolId")
        if (existing.isNotEmpty()) error("Sekolah ini sudah diklaim agen lain.")
        val result = api.createSchoolClaim(CreateSchoolClaimRequest(userId, schoolId))
        result.first()
    }

    // ── Universities ─────────────────────────────────────────────────────────

    suspend fun getMyUniversityClaims(): Result<List<UniversityClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getUniversityClaims(agentId = "eq.$userId")
    }

    suspend fun getAllActiveUniversityClaims(): Result<List<UniversityClaim>> = runCatching {
        api.getAllActiveUniversityClaims()
    }

    suspend fun claimUniversity(universityId: String): Result<UniversityClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.checkUniversityClaim(universityId = "eq.$universityId")
        if (existing.isNotEmpty()) error("Kampus ini sudah diklaim agen lain.")
        val result = api.createUniversityClaim(CreateUniversityClaimRequest(userId, universityId))
        result.first()
    }

    suspend fun getAvailableUniversities(): Result<List<UniversityBrief>> = runCatching {
        api.getUniversities()
    }

    // ── Backward compatibility (used by DashboardViewModel) ──────────────────

    @Deprecated("Use getMySchoolClaims()", ReplaceWith("getMySchoolClaims()"))
    suspend fun getSchoolClaims(): Result<List<SchoolClaim>> = getMySchoolClaims()

    @Deprecated("Use getMyUniversityClaims()", ReplaceWith("getMyUniversityClaims()"))
    suspend fun getUniversityClaims(): Result<List<UniversityClaim>> = getMyUniversityClaims()
}
