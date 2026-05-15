package id.tentuin.agent.core.network

import id.tentuin.agent.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AgentApi {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest,
    ): TokenResponse

    @POST("auth/v1/signup")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body body: RefreshRequest,
    ): TokenResponse

    @POST("auth/v1/logout")
    suspend fun logout(@Header("Authorization") token: String): ResponseBody

    @GET("auth/v1/user")
    suspend fun getAuthUser(): AuthUserDetail

    // ── Agents ────────────────────────────────────────────────────────────
    @GET("rest/v1/agents")
    suspend fun getAgent(
        @Query("id")     id:     String,    // format: "eq.{uuid}"
        @Query("select") select: String = "*",
    ): List<Agent>

    @POST("rest/v1/agents")
    @Headers("Prefer: return=representation")
    suspend fun createAgent(@Body body: CreateAgentRequest): List<Agent>

    @PATCH("rest/v1/agents")
    @Headers("Prefer: return=minimal")
    suspend fun updateAgentLastActive(
        @Query("id")  id:   String,
        @Body         body: Map<String, String>,
    ): Response<ResponseBody>

    @PATCH("rest/v1/agents")
    @Headers("Prefer: return=minimal")
    suspend fun updateAgentBank(
        @Query("id")  id:   String,
        @Body         body: UpdateBankRequest,
    ): Response<ResponseBody>

    // ── Schools ───────────────────────────────────────────────────────────
    @GET("rest/v1/schools")
    suspend fun getSchools(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "name.asc",
        @Query("select")    select:   String = "*",
        @Query("limit")     limit:    Int = 100,
        @Query("offset")    offset:   Int = 0,
    ): List<School>

    @GET("rest/v1/schools")
    suspend fun searchSchools(
        @Query("name")      nameFilter: String,   // format: "ilike.*query*"
        @Query("is_active") isActive:   String = "eq.true",
        @Query("order")     order:      String = "name.asc",
        @Query("select")    select:     String = "*",
    ): List<School>

    @GET("rest/v1/school_targets")
    suspend fun getSchoolTarget(
        @Query("school_id") schoolId: String,
        @Query("year")      year:     String,
        @Query("select")    select:   String = "*",
    ): List<SchoolTarget>

    // ── Agent School Claims ───────────────────────────────────────────────
    @GET("rest/v1/agent_school_claims")
    suspend fun getSchoolClaims(
        @Query("agent_id")  agentId:  String,
        @Query("status")    status:   String = "in.(pending,active)",
        @Query("select")    select:   String = "*,school:schools(id,name,city,province,total_students,logo_url,npsn,address)",
        @Query("order")     order:    String = "claimed_at.desc",
    ): List<SchoolClaim>

    /** Semua klaim sekolah yang sedang pending/active (lintas agen) — dipakai di Klaim screen
     *  agar agen bisa lihat sekolah mana yang sudah dipegang agen lain. */
    @GET("rest/v1/agent_school_claims")
    suspend fun getAllActiveSchoolClaims(
        @Query("status") status: String = "in.(pending,active)",
        @Query("select") select: String = "id,agent_id,school_id,status,claim_code,verified_at,expires_at,claimed_at,is_active",
        @Query("limit")  limit:  Int    = 1000,
    ): List<SchoolClaim>

    @GET("rest/v1/agent_school_claims")
    suspend fun checkSchoolClaim(
        @Query("school_id") schoolId: String,
        @Query("status")    status:   String = "in.(pending,active)",
        @Query("select")    select:   String = "id",
    ): List<SchoolClaim>

    @POST("rest/v1/agent_school_claims")
    @Headers("Prefer: return=representation")
    suspend fun createSchoolClaim(@Body body: CreateSchoolClaimRequest): List<SchoolClaim>

    // ── Agent University Claims ───────────────────────────────────────────
    @GET("rest/v1/universities")
    suspend fun getUniversities(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "is_partner.desc,name.asc",
        @Query("select")    select:   String = "id,name,short_name,city,logo_url,is_partner,partner_tier,quota_balance",
        @Query("limit")     limit:    Int = 100,
    ): List<UniversityBrief>

    @GET("rest/v1/agent_university_claims")
    suspend fun getUniversityClaims(
        @Query("agent_id")  agentId:  String,
        @Query("status")    status:   String = "in.(pending,active)",
        @Query("select")    select:   String = "*,university:universities(id,name,short_name,city,logo_url,quota_balance,is_partner,partner_tier)",
        @Query("order")     order:    String = "claimed_at.desc",
    ): List<UniversityClaim>

    /** Semua klaim kampus pending/active (lintas agen). */
    @GET("rest/v1/agent_university_claims")
    suspend fun getAllActiveUniversityClaims(
        @Query("status") status: String = "in.(pending,active)",
        @Query("select") select: String = "id,agent_id,university_id,status,claim_code,verified_at,expires_at,claimed_at,is_active",
        @Query("limit")  limit:  Int    = 1000,
    ): List<UniversityClaim>

    @GET("rest/v1/agent_university_claims")
    suspend fun checkUniversityClaim(
        @Query("university_id") universityId: String,
        @Query("status")        status:       String = "in.(pending,active)",
        @Query("select")        select:       String = "id",
    ): List<UniversityClaim>

    @POST("rest/v1/agent_university_claims")
    @Headers("Prefer: return=representation")
    suspend fun createUniversityClaim(@Body body: CreateUniversityClaimRequest): List<UniversityClaim>

    // ── Commissions ───────────────────────────────────────────────────────
    @GET("rest/v1/agent_commissions")
    suspend fun getCommissions(
        @Query("agent_id") agentId: String,
        @Query("year")     year:    String,
        @Query("order")    order:   String = "month.asc",
        @Query("select")   select:  String = "*",
    ): List<AgentCommission>

    @GET("rest/v1/university_subscribe_logs")
    suspend fun getSubscribeLogs(
        @Query("agent_id") agentId: String,
        @Query("order")    order:   String = "subscribed_at.desc",
        @Query("select")   select:  String = "*",
        @Query("limit")    limit:   Int = 50,
    ): List<UniversitySubscribeLog>

    // ── Withdrawals ───────────────────────────────────────────────────────
    @GET("rest/v1/agent_withdrawals")
    suspend fun getWithdrawals(
        @Query("agent_id") agentId: String,
        @Query("order")    order:   String = "requested_at.desc",
        @Query("select")   select:  String = "*",
    ): List<AgentWithdrawal>

    @POST("rest/v1/agent_withdrawals")
    @Headers("Prefer: return=representation")
    suspend fun createWithdrawal(@Body body: CreateWithdrawalRequest): List<AgentWithdrawal>
}
