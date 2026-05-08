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
    suspend fun getSchoolClaims(): Result<List<SchoolClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getSchoolClaims(agentId = "eq.$userId")
    }

    suspend fun claimSchool(schoolId: String): Result<SchoolClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.checkSchoolClaim(schoolId = "eq.$schoolId")
        if (existing.isNotEmpty()) error("Sekolah ini sudah diklaim.")
        val result = api.createSchoolClaim(CreateSchoolClaimRequest(userId, schoolId))
        result.first()
    }

    suspend fun getUniversityClaims(): Result<List<UniversityClaim>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getUniversityClaims(agentId = "eq.$userId")
    }

    suspend fun claimUniversity(universityId: String): Result<UniversityClaim> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.checkUniversityClaim(universityId = "eq.$universityId")
        if (existing.isNotEmpty()) error("Universitas ini sudah diklaim.")
        val result = api.createUniversityClaim(CreateUniversityClaimRequest(userId, universityId))
        result.first()
    }

    suspend fun getAvailableSchools(search: String? = null): Result<List<School>> = runCatching {
        if (!search.isNullOrBlank()) {
            api.searchSchools(nameFilter = "ilike.*$search*")
        } else {
            api.getSchools()
        }
    }

    suspend fun getAvailableUniversities(): Result<List<UniversityBrief>> = runCatching {
        api.getUniversities()
    }
}
