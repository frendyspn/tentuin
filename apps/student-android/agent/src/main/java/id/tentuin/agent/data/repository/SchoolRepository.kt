package id.tentuin.agent.data.repository

import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.School
import id.tentuin.agent.data.model.SchoolTarget
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolRepository @Inject constructor(
    private val api: AgentApi,
) {
    suspend fun list(limit: Int = 100, offset: Int = 0): Result<List<School>> = runCatching {
        api.getSchools(limit = limit, offset = offset)
    }

    suspend fun search(query: String): Result<List<School>> = runCatching {
        if (query.isBlank()) api.getSchools()
        else api.searchSchools(nameFilter = "ilike.*$query*")
    }

    suspend fun getTarget(schoolId: String, year: Int): Result<SchoolTarget?> = runCatching {
        api.getSchoolTarget(schoolId = "eq.$schoolId", year = "eq.$year").firstOrNull()
    }
}
