package id.tentuin.university.data.repository

import id.tentuin.university.core.datastore.SessionDataStore
import id.tentuin.university.core.network.UniversityApi
import id.tentuin.university.data.model.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val api:     UniversityApi,
    private val session: SessionDataStore,
) {
    suspend fun getMyAccount(): Result<UniversityAccount?> = runCatching {
        val uid = session.userId.first() ?: error("Not logged in")
        val memberships = api.getMyMemberships(userId = "eq.$uid")
        memberships.firstOrNull()?.account?.also {
            session.saveAccountId(it.id)
        }
    }

    suspend fun createPersonal(displayName: String, universityId: String?): Result<CreateAccountResponse> = runCatching {
        val res = api.createPersonalAccount(CreatePersonalAccountRequest(displayName, universityId))
            .firstOrNull() ?: error("Empty response")
        if (res.success && res.accountId != null) session.saveAccountId(res.accountId)
        res
    }

    suspend fun createEnterprise(displayName: String, universityId: String?): Result<CreateAccountResponse> = runCatching {
        val res = api.createEnterpriseAccount(CreateEnterpriseAccountRequest(displayName, universityId))
            .firstOrNull() ?: error("Empty response")
        if (res.success && res.accountId != null) session.saveAccountId(res.accountId)
        res
    }

    suspend fun subscribe(accountId: String, planCode: String): Result<SubscribePlanResponse> = runCatching {
        api.subscribePlan(SubscribePlanRequest(accountId, planCode)).firstOrNull()
            ?: error("Empty response")
    }

    suspend fun getPlans(accountType: String): Result<List<SubscriptionPlan>> = runCatching {
        api.getPlansByType("eq.$accountType")
    }

    suspend fun getUniversities(): Result<List<UniversityBrief>> = runCatching {
        api.getUniversities()
    }

    suspend fun getSubscribeHistory(accountId: String): Result<List<SubscribeLog>> = runCatching {
        api.getSubscribeHistory("eq.$accountId")
    }
}
