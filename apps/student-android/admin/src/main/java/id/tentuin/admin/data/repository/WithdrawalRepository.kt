package id.tentuin.admin.data.repository

import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.UpdateWithdrawalRequest
import id.tentuin.admin.data.model.WithdrawalWithAgent
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WithdrawalRepository @Inject constructor(
    private val api:      AdminApi,
    private val session:  SessionDataStore,
    private val auditRepo: AuditRepository,
) {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    suspend fun listPending(): Result<List<WithdrawalWithAgent>> = runCatching {
        api.listWithdrawals(status = "eq.requested")
    }

    suspend fun listAll(): Result<List<WithdrawalWithAgent>> = runCatching {
        api.listWithdrawals(status = null)
    }

    suspend fun getById(id: String): Result<WithdrawalWithAgent?> = runCatching {
        api.getWithdrawal(id = "eq.$id").firstOrNull()
    }

    suspend fun approve(id: String, oldStatus: String): Result<Unit> = runCatching {
        val now = isoFormat.format(Date())
        api.updateWithdrawal(
            id = "eq.$id",
            body = UpdateWithdrawalRequest(status = "approved", processedAt = now)
        )
        auditRepo.log(
            adminId      = session.userId.first() ?: "",
            action       = "withdrawal.approve",
            resourceType = "agent_withdrawal",
            resourceId   = id,
            oldValues    = mapOf("status" to oldStatus),
            newValues    = mapOf("status" to "approved", "processed_at" to now),
        )
    }

    suspend fun reject(id: String, notes: String, oldStatus: String): Result<Unit> = runCatching {
        val now = isoFormat.format(Date())
        api.updateWithdrawal(
            id = "eq.$id",
            body = UpdateWithdrawalRequest(status = "rejected", adminNotes = notes, processedAt = now)
        )
        auditRepo.log(
            adminId      = session.userId.first() ?: "",
            action       = "withdrawal.reject",
            resourceType = "agent_withdrawal",
            resourceId   = id,
            oldValues    = mapOf("status" to oldStatus),
            newValues    = mapOf("status" to "rejected", "admin_notes" to notes, "processed_at" to now),
        )
    }

    suspend fun markTransferred(id: String, oldStatus: String): Result<Unit> = runCatching {
        val now = isoFormat.format(Date())
        api.updateWithdrawal(
            id = "eq.$id",
            body = UpdateWithdrawalRequest(status = "transferred", processedAt = now)
        )
        auditRepo.log(
            adminId      = session.userId.first() ?: "",
            action       = "withdrawal.transfer",
            resourceType = "agent_withdrawal",
            resourceId   = id,
            oldValues    = mapOf("status" to oldStatus),
            newValues    = mapOf("status" to "transferred", "processed_at" to now),
        )
    }
}
