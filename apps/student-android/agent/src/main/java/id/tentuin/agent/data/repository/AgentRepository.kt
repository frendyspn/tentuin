package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.Agent
import id.tentuin.agent.data.model.CreateAgentRequest
import id.tentuin.agent.data.model.UpdateBankRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    suspend fun getOrCreateAgent(fullName: String, email: String, phone: String?): Result<Agent> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val existing = api.getAgent(id = "eq.$userId")
        if (existing.isNotEmpty()) return@runCatching existing.first()

        val code = generateReferralCode()
        val created = api.createAgent(
            CreateAgentRequest(
                id = userId,
                fullName = fullName,
                email = email,
                phone = phone,
                referralCode = code,
                lastActiveAt = System.currentTimeMillis().toString(),
            )
        )
        created.first()
    }

    suspend fun getCurrentAgent(): Result<Agent?> = runCatching {
        val userId = session.userId.first() ?: return@runCatching null
        api.getAgent(id = "eq.$userId").firstOrNull()
    }

    suspend fun updateLastActive(): Result<Unit> = runCatching {
        val userId = session.userId.first() ?: return@runCatching
        api.updateAgentLastActive(
            id = "eq.$userId",
            body = mapOf("last_active_at" to System.currentTimeMillis().toString())
        )
        Unit
    }

    suspend fun updateBank(bankName: String, accountNumber: String, accountName: String): Result<Unit> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.updateAgentBank(
            id = "eq.$userId",
            body = UpdateBankRequest(bankName, accountNumber, accountName)
        )
        Unit
    }

    private fun generateReferralCode(): String {
        val letters = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val prefix = (1..3).map { letters.random() }.joinToString("")
        val suffix = (1000..9999).random()
        return "$prefix$suffix"
    }
}
