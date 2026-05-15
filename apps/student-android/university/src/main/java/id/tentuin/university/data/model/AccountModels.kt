package id.tentuin.university.data.model

import com.google.gson.annotations.SerializedName

data class UniversityAccount(
    @SerializedName("id")                     val id:                   String,
    @SerializedName("account_type")           val accountType:          String,   // 'personal' | 'enterprise'
    @SerializedName("owner_user_id")          val ownerUserId:          String,
    @SerializedName("university_id")          val universityId:         String?  = null,
    @SerializedName("display_name")           val displayName:          String,
    @SerializedName("quota_balance")          val quotaBalance:         Int      = 0,
    @SerializedName("total_quota_purchased")  val totalQuotaPurchased:  Int      = 0,
    @SerializedName("status")                 val status:               String   = "active",
    @SerializedName("created_at")             val createdAt:            String?  = null,
    @SerializedName("university")             val university:           UniversityBrief? = null,
)

data class UniversityBrief(
    @SerializedName("id")         val id:        String,
    @SerializedName("name")       val name:      String,
    @SerializedName("short_name") val shortName: String? = null,
    @SerializedName("city")       val city:      String? = null,
    @SerializedName("logo_url")   val logoUrl:   String? = null,
)

data class AccountMember(
    @SerializedName("id")         val id:         String,
    @SerializedName("account_id") val accountId:  String,
    @SerializedName("user_id")    val userId:     String,
    @SerializedName("role")       val role:       String,    // 'owner' | 'member'
    @SerializedName("invited_by") val invitedBy:  String?  = null,
    @SerializedName("joined_at")  val joinedAt:   String?  = null,
    @SerializedName("left_at")    val leftAt:     String?  = null,
    @SerializedName("profile")    val profile:    MemberProfile? = null,
)

data class MemberProfile(
    @SerializedName("id")        val id:        String,
    @SerializedName("full_name") val fullName:  String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
)

data class SubscriptionPlan(
    @SerializedName("code")         val code:         String,
    @SerializedName("name")         val name:         String,
    @SerializedName("account_type") val accountType:  String,
    @SerializedName("price")        val price:        Int,
    @SerializedName("quota")        val quota:        Int,
    @SerializedName("is_active")    val isActive:     Boolean = true,
)

data class SubscribeLog(
    @SerializedName("id")             val id:            String,
    @SerializedName("account_id")     val accountId:     String?,
    @SerializedName("plan_code")      val planCode:      String?,
    @SerializedName("amount")         val amount:        Int,
    @SerializedName("quota_purchased") val quotaPurchased: Int,
    @SerializedName("subscribed_at")  val subscribedAt:  String?,
)

// ── RPC payloads / responses ──────────────────────────────────────────────
data class CreatePersonalAccountRequest(
    @SerializedName("p_display_name")  val displayName:  String,
    @SerializedName("p_university_id") val universityId: String? = null,
)

data class CreateEnterpriseAccountRequest(
    @SerializedName("p_display_name")  val displayName:  String,
    @SerializedName("p_university_id") val universityId: String? = null,
)

data class CreateAccountResponse(
    @SerializedName("success")    val success:    Boolean,
    @SerializedName("message")    val message:    String,
    @SerializedName("account_id") val accountId:  String? = null,
)

data class SubscribePlanRequest(
    @SerializedName("p_account_id") val accountId: String,
    @SerializedName("p_plan_code")  val planCode:  String,
)

data class SubscribePlanResponse(
    @SerializedName("success")          val success:        Boolean,
    @SerializedName("message")          val message:        String,
    @SerializedName("subscribe_log_id") val subscribeLogId: String? = null,
    @SerializedName("new_balance")      val newBalance:     Int = 0,
)

data class AddEnterpriseMemberRequest(
    @SerializedName("p_enterprise_account_id") val enterpriseAccountId: String,
    @SerializedName("p_user_id")               val userId:              String,
)

data class AddEnterpriseMemberResponse(
    @SerializedName("success")             val success:            Boolean,
    @SerializedName("message")             val message:            String,
    @SerializedName("merged_quota")        val mergedQuota:        Int = 0,
    @SerializedName("enterprise_balance")  val enterpriseBalance:  Int = 0,
)

data class LeaveTeamRequest(
    @SerializedName("p_account_id") val accountId: String,
)

data class GenericResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)
