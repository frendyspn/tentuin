package id.tentuin.university.data.repository

import id.tentuin.university.core.network.UniversityApi
import id.tentuin.university.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProspectRepository @Inject constructor(
    private val api: UniversityApi,
) {
    suspend fun listProspects(limit: Int = 50, offset: Int = 0): Result<List<Prospect>> = runCatching {
        api.getProspects(limit = limit, offset = offset)
    }

    suspend fun searchProspects(query: String): Result<List<Prospect>> = runCatching {
        api.searchProspects(name = "ilike.*$query*")
    }

    suspend fun listFollowups(accountId: String, statusFilter: String? = null): Result<List<ProspectFollowup>> = runCatching {
        api.getFollowups(
            accountId = "eq.$accountId",
            status    = statusFilter,
        )
    }

    suspend fun getFollowup(id: String): Result<ProspectFollowup?> = runCatching {
        api.getFollowup(id = "eq.$id").firstOrNull()
    }

    suspend fun listActivities(followupId: String): Result<List<FollowupActivity>> = runCatching {
        api.getActivities(followupId = "eq.$followupId")
    }

    suspend fun unlockProspect(accountId: String, prospectId: String): Result<UnlockProspectResponse> = runCatching {
        api.unlockProspect(UnlockProspectRequest(accountId, prospectId)).firstOrNull()
            ?: error("Empty response")
    }

    suspend fun logActivity(followupId: String, type: String, note: String?): Result<LogActivityResponse> = runCatching {
        api.logActivity(LogActivityRequest(followupId, type, note)).firstOrNull()
            ?: error("Empty response")
    }

    suspend fun changeStatus(followupId: String, newStatus: String, note: String? = null): Result<GenericResponse> = runCatching {
        api.changeStatus(ChangeStatusRequest(followupId, newStatus, note)).firstOrNull()
            ?: error("Empty response")
    }
}
