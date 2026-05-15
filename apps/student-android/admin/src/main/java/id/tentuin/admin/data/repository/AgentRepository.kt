package id.tentuin.admin.data.repository

import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.Agent
import id.tentuin.admin.data.model.UpdateAgentStatusRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepository @Inject constructor(
    private val api:       AdminApi,
    private val session:   SessionDataStore,
    private val auditRepo: AuditRepository,
) {
    suspend fun list(): Result<List<Agent>> = runCatching {
        api.listAgents()
    }

    suspend fun getById(id: String): Result<Agent?> = runCatching {
        api.getAgent(id = "eq.$id").firstOrNull()
    }

    suspend fun setStatus(id: String, newStatus: String, oldStatus: String, notes: String? = null): Result<Unit> = runCatching {
        api.updateAgentStatus(
            id = "eq.$id",
            body = UpdateAgentStatusRequest(status = newStatus, notes = notes),
        )
        auditRepo.log(
            adminId      = session.userId.first() ?: "",
            action       = "agent.status.$newStatus",
            resourceType = "agent",
            resourceId   = id,
            oldValues    = mapOf("status" to oldStatus),
            newValues    = mapOf("status" to newStatus, "notes" to notes),
        )
    }
}
