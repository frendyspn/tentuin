package id.tentuin.admin.data.repository

import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.RecordSubscribeRequest
import id.tentuin.admin.data.model.UniversitySubscribeLog
import id.tentuin.admin.data.model.UniversityWithClaims
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UniversityRepository @Inject constructor(
    private val api:       AdminApi,
    private val session:   SessionDataStore,
    private val auditRepo: AuditRepository,
) {
    suspend fun list(): Result<List<UniversityWithClaims>> = runCatching {
        api.listUniversities()
    }

    suspend fun getById(id: String): Result<UniversityWithClaims?> = runCatching {
        api.getUniversity(id = "eq.$id").firstOrNull()
    }

    suspend fun subscribeLogs(universityId: String): Result<List<UniversitySubscribeLog>> = runCatching {
        api.listSubscribeLogs(universityId = "eq.$universityId")
    }

    suspend fun recordSubscribe(
        universityId: String,
        agentId:      String?,
        amount:       Int,
        quota:        Int,
    ): Result<Unit> = runCatching {
        // Komisi agen: 10% dari amount kalau ada agen aktif
        val commission = if (agentId != null) (amount * 10) / 100 else 0
        api.recordSubscribe(
            RecordSubscribeRequest(
                universityId    = universityId,
                agentId         = agentId,
                amount          = amount,
                quotaPurchased  = quota,
                commissionAgent = commission,
            )
        )
        auditRepo.log(
            adminId      = session.userId.first() ?: "",
            action       = "university.subscribe.record",
            resourceType = "university",
            resourceId   = universityId,
            oldValues    = null,
            newValues    = mapOf(
                "amount"           to amount,
                "quota_purchased"  to quota,
                "commission_agent" to commission,
                "agent_id"         to agentId,
            ),
        )
    }
}
