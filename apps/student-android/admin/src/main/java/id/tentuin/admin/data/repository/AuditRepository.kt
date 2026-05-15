package id.tentuin.admin.data.repository

import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.AdminAuditLog
import id.tentuin.admin.data.model.CreateAuditLogRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepository @Inject constructor(
    private val api: AdminApi,
) {
    suspend fun list(): Result<List<AdminAuditLog>> = runCatching {
        api.listAuditLogs()
    }

    suspend fun log(
        adminId:      String,
        action:       String,
        resourceType: String,
        resourceId:   String?,
        oldValues:    Map<String, Any?>?,
        newValues:    Map<String, Any?>?,
    ): Result<Unit> = runCatching {
        api.insertAuditLog(
            CreateAuditLogRequest(
                adminId      = adminId,
                action       = action,
                resourceType = resourceType,
                resourceId   = resourceId,
                oldValues    = oldValues,
                newValues    = newValues,
            )
        )
        Unit
    }
}
