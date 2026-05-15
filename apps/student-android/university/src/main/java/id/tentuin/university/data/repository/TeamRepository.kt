package id.tentuin.university.data.repository

import id.tentuin.university.core.network.UniversityApi
import id.tentuin.university.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamRepository @Inject constructor(
    private val api: UniversityApi,
) {
    suspend fun listMembers(accountId: String): Result<List<AccountMember>> = runCatching {
        api.getAccountMembers(accountId = "eq.$accountId")
    }

    suspend fun findProfile(userId: String): Result<MemberProfile?> = runCatching {
        api.findProfileById(id = "eq.$userId").firstOrNull()
    }

    suspend fun addMember(enterpriseAccountId: String, userId: String): Result<AddEnterpriseMemberResponse> = runCatching {
        api.addEnterpriseMember(AddEnterpriseMemberRequest(enterpriseAccountId, userId)).firstOrNull()
            ?: error("Empty response")
    }

    suspend fun leaveTeam(accountId: String): Result<GenericResponse> = runCatching {
        api.leaveEnterpriseTeam(LeaveTeamRequest(accountId)).firstOrNull()
            ?: error("Empty response")
    }
}
