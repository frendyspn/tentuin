package id.tentuin.agent.data.repository

import id.tentuin.agent.core.datastore.SessionDataStore
import id.tentuin.agent.core.network.AgentApi
import id.tentuin.agent.data.model.*
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommissionRepository @Inject constructor(
    private val api: AgentApi,
    private val session: SessionDataStore,
) {
    suspend fun getCommissions(year: Int = LocalDate.now().year): Result<List<AgentCommission>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getCommissions(agentId = "eq.$userId", year = "eq.$year")
    }

    suspend fun getSubscribeLogs(): Result<List<UniversitySubscribeLog>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getSubscribeLogs(agentId = "eq.$userId")
    }

    suspend fun getWithdrawals(): Result<List<AgentWithdrawal>> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        api.getWithdrawals(agentId = "eq.$userId")
    }

    suspend fun requestWithdrawal(amount: Int): Result<AgentWithdrawal> = runCatching {
        val userId = session.userId.first() ?: error("Not logged in")
        val result = api.createWithdrawal(CreateWithdrawalRequest(userId, amount))
        result.first()
    }
}
