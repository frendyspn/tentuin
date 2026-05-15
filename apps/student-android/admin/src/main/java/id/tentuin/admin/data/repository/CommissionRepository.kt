package id.tentuin.admin.data.repository

import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.AgentCommissionWithAgent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommissionRepository @Inject constructor(
    private val api: AdminApi,
) {
    suspend fun listFor(month: Int, year: Int): Result<List<AgentCommissionWithAgent>> = runCatching {
        api.listCommissions(month = "eq.$month", year = "eq.$year")
    }
}
