package id.tentuin.university.data.model

import com.google.gson.annotations.SerializedName

// ── Prospect ringkas (data student dari profiles + test_results) ─────────
data class Prospect(
    @SerializedName("id")          val id:         String,
    @SerializedName("full_name")   val fullName:   String? = null,
    @SerializedName("school_name") val schoolName: String? = null,
    @SerializedName("city")        val city:       String? = null,
    @SerializedName("birth_year")  val birthYear:  Int? = null,
    @SerializedName("avatar_url")  val avatarUrl:  String? = null,
    @SerializedName("school_id")   val schoolId:   String? = null,
    @SerializedName("test_results") val testResults: List<TestResultBrief>? = null,
    @SerializedName("school")      val school:     SchoolBrief? = null,
)

data class SchoolBrief(
    @SerializedName("id")       val id:       String,
    @SerializedName("name")     val name:     String,
    @SerializedName("city")     val city:     String? = null,
    @SerializedName("province") val province: String? = null,
)

data class TestResultBrief(
    @SerializedName("riasec_code")        val riasecCode:        String? = null,
    @SerializedName("recommended_majors") val recommendedMajors: Any?    = null,
    @SerializedName("completed_at")       val completedAt:       String? = null,
)

// ── Followup state ───────────────────────────────────────────────────────
data class ProspectFollowup(
    @SerializedName("id")               val id:              String,
    @SerializedName("account_id")       val accountId:       String,
    @SerializedName("prospect_id")      val prospectId:      String,
    @SerializedName("assigned_to")      val assignedTo:      String? = null,
    @SerializedName("status")           val status:          String,
    @SerializedName("claimed_at")       val claimedAt:       String? = null,
    @SerializedName("last_activity_at") val lastActivityAt:  String? = null,
    @SerializedName("released_at")      val releasedAt:      String? = null,
    @SerializedName("released_reason")  val releasedReason:  String? = null,
    @SerializedName("notes")            val notes:           String? = null,
    @SerializedName("prospect")         val prospect:        Prospect?       = null,
    @SerializedName("assigned_profile") val assignedProfile: MemberProfile?  = null,
)

data class FollowupActivity(
    @SerializedName("id")            val id:            String,
    @SerializedName("followup_id")   val followupId:    String,
    @SerializedName("user_id")       val userId:        String?,
    @SerializedName("activity_type") val activityType:  String,
    @SerializedName("note")          val note:          String? = null,
    @SerializedName("created_at")    val createdAt:     String? = null,
    @SerializedName("user")          val user:          MemberProfile? = null,
)

// ── RPC payloads ─────────────────────────────────────────────────────────
data class UnlockProspectRequest(
    @SerializedName("p_account_id")  val accountId:  String,
    @SerializedName("p_prospect_id") val prospectId: String,
)

data class UnlockProspectResponse(
    @SerializedName("success")        val success:       Boolean,
    @SerializedName("message")        val message:       String,
    @SerializedName("followup_id")    val followupId:    String? = null,
    @SerializedName("quota_charged")  val quotaCharged:  Boolean = false,
    @SerializedName("new_balance")    val newBalance:    Int     = 0,
)

data class LogActivityRequest(
    @SerializedName("p_followup_id")   val followupId:   String,
    @SerializedName("p_activity_type") val activityType: String,
    @SerializedName("p_note")          val note:         String? = null,
)

data class LogActivityResponse(
    @SerializedName("success")     val success:    Boolean,
    @SerializedName("message")     val message:    String,
    @SerializedName("activity_id") val activityId: String? = null,
)

data class ChangeStatusRequest(
    @SerializedName("p_followup_id") val followupId: String,
    @SerializedName("p_new_status")  val newStatus:  String,
    @SerializedName("p_note")        val note:       String? = null,
)
