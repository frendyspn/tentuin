package id.tentuin.admin.core.network

import id.tentuin.admin.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {

    // ── Auth ─────────────────────────────────────────────────────────────
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest,
    ): TokenResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body body: RefreshRequest,
    ): TokenResponse

    @POST("auth/v1/logout")
    suspend fun logout(@Header("Authorization") token: String): ResponseBody

    // ── Profile (untuk role check) ───────────────────────────────────────
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id")     id:     String,    // format "eq.{uuid}"
        @Query("select") select: String = "id,full_name,role,avatar_url",
    ): List<AdminProfile>

    // ── Agents ───────────────────────────────────────────────────────────
    @GET("rest/v1/agents")
    suspend fun listAgents(
        @Query("order")  order:  String = "created_at.desc",
        @Query("select") select: String = "*",
        @Query("limit")  limit:  Int = 200,
    ): List<Agent>

    @GET("rest/v1/agents")
    suspend fun getAgent(
        @Query("id")     id:     String,
        @Query("select") select: String = "*",
    ): List<Agent>

    @PATCH("rest/v1/agents")
    @Headers("Prefer: return=minimal")
    suspend fun updateAgentStatus(
        @Query("id") id:   String,
        @Body        body: UpdateAgentStatusRequest,
    ): Response<ResponseBody>

    // ── Withdrawals ──────────────────────────────────────────────────────
    @GET("rest/v1/agent_withdrawals")
    suspend fun listWithdrawals(
        @Query("status") status: String? = null,    // optional "eq.requested"
        @Query("order")  order:  String = "requested_at.desc",
        @Query("select") select: String = "*,agent:agents(id,full_name,referral_code,bank_name,bank_account_number,bank_account_name)",
        @Query("limit")  limit:  Int = 200,
    ): List<WithdrawalWithAgent>

    @GET("rest/v1/agent_withdrawals")
    suspend fun getWithdrawal(
        @Query("id")     id:     String,
        @Query("select") select: String = "*,agent:agents(id,full_name,referral_code,bank_name,bank_account_number,bank_account_name)",
    ): List<WithdrawalWithAgent>

    @PATCH("rest/v1/agent_withdrawals")
    @Headers("Prefer: return=minimal")
    suspend fun updateWithdrawal(
        @Query("id") id:   String,
        @Body        body: UpdateWithdrawalRequest,
    ): Response<ResponseBody>

    // ── Audit Log ────────────────────────────────────────────────────────
    @POST("rest/v1/admin_audit_logs")
    @Headers("Prefer: return=minimal")
    suspend fun insertAuditLog(@Body body: CreateAuditLogRequest): Response<ResponseBody>

    @GET("rest/v1/admin_audit_logs")
    suspend fun listAuditLogs(
        @Query("order")  order:  String = "created_at.desc",
        @Query("select") select: String = "*,admin:profiles!admin_id(full_name)",
        @Query("limit")  limit:  Int = 100,
    ): List<AdminAuditLog>

    // ── Admin Push Tokens ────────────────────────────────────────────────
    @POST("rest/v1/admin_push_tokens")
    @Headers(
        "Prefer: resolution=merge-duplicates,return=minimal",
    )
    suspend fun upsertPushToken(@Body body: UpsertPushTokenRequest): Response<ResponseBody>

    @DELETE("rest/v1/admin_push_tokens")
    @Headers("Prefer: return=minimal")
    suspend fun deletePushToken(
        @Query("user_id")   userId:   String,
        @Query("fcm_token") fcmToken: String,
    ): Response<ResponseBody>

    // ── Schools ──────────────────────────────────────────────────────────
    @GET("rest/v1/schools")
    suspend fun listSchools(
        @Query("order")  order:  String = "name.asc",
        @Query("select") select: String = "*,claims:agent_school_claims(id,agent_id,status,claimed_at,verified_at,agent:agents(id,full_name,referral_code))",
        @Query("limit")  limit:  Int = 500,
    ): List<SchoolWithClaims>

    @GET("rest/v1/schools")
    suspend fun getSchool(
        @Query("id")     id:     String,
        @Query("select") select: String = "*,claims:agent_school_claims(id,agent_id,status,claim_code,claimed_at,verified_at,expires_at,agent:agents(id,full_name,referral_code,phone))",
    ): List<SchoolWithClaims>

    // ── Universities ─────────────────────────────────────────────────────
    @GET("rest/v1/universities")
    suspend fun listUniversities(
        @Query("order")  order:  String = "is_partner.desc,name.asc",
        @Query("select") select: String = "*,claims:agent_university_claims(id,agent_id,status,claimed_at,verified_at,agent:agents(id,full_name,referral_code))",
        @Query("limit")  limit:  Int = 500,
    ): List<UniversityWithClaims>

    @GET("rest/v1/universities")
    suspend fun getUniversity(
        @Query("id")     id:     String,
        @Query("select") select: String = "*,claims:agent_university_claims(id,agent_id,status,claim_code,claimed_at,verified_at,expires_at,agent:agents(id,full_name,referral_code,phone))",
    ): List<UniversityWithClaims>

    @POST("rest/v1/university_subscribe_logs")
    @Headers("Prefer: return=minimal")
    suspend fun recordSubscribe(@Body body: RecordSubscribeRequest): Response<ResponseBody>

    @GET("rest/v1/university_subscribe_logs")
    suspend fun listSubscribeLogs(
        @Query("university_id") universityId: String,
        @Query("order")         order:        String = "subscribed_at.desc",
        @Query("select")        select:       String = "*",
        @Query("limit")         limit:        Int = 50,
    ): List<UniversitySubscribeLog>

    // ── Commissions ──────────────────────────────────────────────────────
    @GET("rest/v1/agent_commissions")
    suspend fun listCommissions(
        @Query("month")  month:  String,        // "eq.{m}"
        @Query("year")   year:   String,        // "eq.{y}"
        @Query("order")  order:  String = "total_amount.desc",
        @Query("select") select: String = "*,agent:agents(id,full_name,referral_code)",
    ): List<AgentCommissionWithAgent>
}
