package id.tentuin.university.core.network

import id.tentuin.university.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface UniversityApi {

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

    // ── Universities (master data) ────────────────────────────────────────
    @GET("rest/v1/universities")
    suspend fun getUniversities(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "name.asc",
        @Query("select")    select:   String = "id,name,short_name,city,logo_url",
        @Query("limit")     limit:    Int = 200,
    ): List<UniversityBrief>

    // ── Subscription plans ────────────────────────────────────────────────
    @GET("rest/v1/university_subscription_plans")
    suspend fun getPlans(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "price.asc",
        @Query("select")    select:   String = "*",
    ): List<SubscriptionPlan>

    @GET("rest/v1/university_subscription_plans")
    suspend fun getPlansByType(
        @Query("account_type") accountType: String, // eq.personal or eq.enterprise
        @Query("is_active")    isActive:    String = "eq.true",
        @Query("order")        order:       String = "price.asc",
        @Query("select")       select:      String = "*",
    ): List<SubscriptionPlan>

    // ── University Accounts ───────────────────────────────────────────────
    /** Account aktif untuk user yang sedang login (lewat RLS). */
    @GET("rest/v1/university_account_members")
    suspend fun getMyMemberships(
        @Query("user_id") userId: String,                  // eq.{uuid}
        @Query("left_at") leftAt: String = "is.null",
        @Query("select")  select: String =
            "id,role,joined_at,account:university_accounts(id,account_type,owner_user_id,university_id,display_name,quota_balance,total_quota_purchased,status,created_at,university:universities(id,name,short_name,city,logo_url))",
    ): List<MemberWithAccount>

    @GET("rest/v1/university_accounts")
    suspend fun getAccount(
        @Query("id")     id:     String,
        @Query("select") select: String =
            "*,university:universities(id,name,short_name,city,logo_url)",
    ): List<UniversityAccount>

    /** Member list untuk enterprise account (owner-only via RLS). */
    @GET("rest/v1/university_account_members")
    suspend fun getAccountMembers(
        @Query("account_id") accountId: String,
        @Query("left_at")    leftAt:    String = "is.null",
        @Query("select")     select:    String =
            "id,role,joined_at,user_id,profile:profiles!user_id(id,full_name,avatar_url)",
        @Query("order")      order:     String = "role.desc,joined_at.asc",
    ): List<AccountMember>

    // ── Subscribe logs (history) ─────────────────────────────────────────
    @GET("rest/v1/university_subscribe_logs")
    suspend fun getSubscribeHistory(
        @Query("account_id") accountId: String,
        @Query("order")      order:     String = "subscribed_at.desc",
        @Query("select")     select:    String = "id,account_id,plan_code,amount,quota_purchased,subscribed_at",
        @Query("limit")      limit:     Int    = 50,
    ): List<SubscribeLog>

    // ── Prospects (siswa yang sudah complete RIASEC) ─────────────────────
    /** Daftar siswa untuk discovery (basic info, belum unlock). */
    @GET("rest/v1/profiles")
    suspend fun getProspects(
        @Query("role")             role:           String = "eq.student",
        @Query("has_completed_onboarding") completed: String = "eq.true",
        @Query("order")            order:          String = "created_at.desc",
        @Query("select")           select:         String =
            "id,full_name,school_name,city,birth_year,avatar_url,school_id,school:schools(id,name,city,province),test_results(riasec_code,completed_at)",
        @Query("limit")            limit:          Int = 50,
        @Query("offset")           offset:         Int = 0,
    ): List<Prospect>

    @GET("rest/v1/profiles")
    suspend fun searchProspects(
        @Query("role")           role:    String = "eq.student",
        @Query("full_name")      name:    String,   // ilike.*xxx*
        @Query("order")          order:   String = "full_name.asc",
        @Query("select")         select:  String =
            "id,full_name,school_name,city,birth_year,avatar_url,school_id,school:schools(id,name,city,province),test_results(riasec_code,completed_at)",
        @Query("limit")          limit:   Int = 50,
    ): List<Prospect>

    // ── Prospect Followups ───────────────────────────────────────────────
    @GET("rest/v1/prospect_followups")
    suspend fun getFollowups(
        @Query("account_id") accountId: String,
        @Query("status")     status:    String? = null,    // e.g. "in.(claimed,contacted,qualified)"
        @Query("order")      order:     String = "last_activity_at.desc",
        @Query("select")     select:    String =
            "*,prospect:profiles!prospect_id(id,full_name,school_name,city,avatar_url,school:schools(id,name,city)),assigned_profile:profiles!assigned_to(id,full_name,avatar_url)",
        @Query("limit")      limit:     Int = 50,
    ): List<ProspectFollowup>

    @GET("rest/v1/prospect_followups")
    suspend fun getFollowup(
        @Query("id")     id:     String,
        @Query("select") select: String =
            "*,prospect:profiles!prospect_id(id,full_name,school_name,city,birth_year,avatar_url,school:schools(id,name,city,province),test_results(riasec_code,recommended_majors,completed_at)),assigned_profile:profiles!assigned_to(id,full_name,avatar_url)",
    ): List<ProspectFollowup>

    @GET("rest/v1/prospect_followup_activities")
    suspend fun getActivities(
        @Query("followup_id") followupId: String,
        @Query("order")       order:      String = "created_at.desc",
        @Query("select")      select:     String =
            "id,followup_id,user_id,activity_type,note,created_at,user:profiles!user_id(id,full_name,avatar_url)",
    ): List<FollowupActivity>

    // ── RPC: account creation ────────────────────────────────────────────
    @POST("rest/v1/rpc/create_personal_account")
    suspend fun createPersonalAccount(@Body body: CreatePersonalAccountRequest): List<CreateAccountResponse>

    @POST("rest/v1/rpc/create_enterprise_account")
    suspend fun createEnterpriseAccount(@Body body: CreateEnterpriseAccountRequest): List<CreateAccountResponse>

    // ── RPC: subscribe ───────────────────────────────────────────────────
    @POST("rest/v1/rpc/subscribe_plan")
    suspend fun subscribePlan(@Body body: SubscribePlanRequest): List<SubscribePlanResponse>

    // ── RPC: team management ─────────────────────────────────────────────
    @POST("rest/v1/rpc/add_enterprise_member")
    suspend fun addEnterpriseMember(@Body body: AddEnterpriseMemberRequest): List<AddEnterpriseMemberResponse>

    @POST("rest/v1/rpc/leave_enterprise_team")
    suspend fun leaveEnterpriseTeam(@Body body: LeaveTeamRequest): List<GenericResponse>

    // ── RPC: prospect operations ─────────────────────────────────────────
    @POST("rest/v1/rpc/unlock_prospect")
    suspend fun unlockProspect(@Body body: UnlockProspectRequest): List<UnlockProspectResponse>

    @POST("rest/v1/rpc/log_followup_activity")
    suspend fun logActivity(@Body body: LogActivityRequest): List<LogActivityResponse>

    @POST("rest/v1/rpc/change_followup_status")
    suspend fun changeStatus(@Body body: ChangeStatusRequest): List<GenericResponse>

    // ── Profile lookup (for add member by email) ─────────────────────────
    @GET("rest/v1/profiles")
    suspend fun findProfileById(
        @Query("id")     id:     String,
        @Query("select") select: String = "id,full_name,avatar_url",
    ): List<MemberProfile>
}

// helper data class for response join shape
data class MemberWithAccount(
    @com.google.gson.annotations.SerializedName("id")        val id:        String,
    @com.google.gson.annotations.SerializedName("role")      val role:      String,
    @com.google.gson.annotations.SerializedName("joined_at") val joinedAt:  String? = null,
    @com.google.gson.annotations.SerializedName("account")   val account:   UniversityAccount,
)
