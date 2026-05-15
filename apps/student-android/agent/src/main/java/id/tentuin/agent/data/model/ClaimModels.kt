package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class SchoolClaim(
    @SerializedName("id")          val id:         String,
    @SerializedName("agent_id")    val agentId:    String,
    @SerializedName("school_id")   val schoolId:   String,
    @SerializedName("is_active")   val isActive:   Boolean = false,
    @SerializedName("claim_code")  val claimCode:  String? = null,
    @SerializedName("status")      val status:     String  = "pending",
    @SerializedName("verified_at") val verifiedAt: String? = null,
    @SerializedName("expires_at")  val expiresAt:  String? = null,
    @SerializedName("claimed_at")  val claimedAt:  String,
    @SerializedName("school")      val school:     School? = null,
) {
    val isMine: Boolean get() = false // computed in VM with myUserId
    val isPending: Boolean get() = status == "pending"
    val isActiveClaim: Boolean get() = status == "active"
}

data class UniversityClaim(
    @SerializedName("id")              val id:           String,
    @SerializedName("agent_id")        val agentId:      String,
    @SerializedName("university_id")   val universityId: String,
    @SerializedName("is_active")       val isActive:     Boolean = false,
    @SerializedName("claim_code")      val claimCode:    String? = null,
    @SerializedName("status")          val status:       String  = "pending",
    @SerializedName("verified_at")     val verifiedAt:   String? = null,
    @SerializedName("expires_at")      val expiresAt:    String? = null,
    @SerializedName("claimed_at")      val claimedAt:    String,
    @SerializedName("university")      val university:   UniversityBrief? = null,
)

data class UniversityBrief(
    @SerializedName("id")              val id:            String,
    @SerializedName("name")            val name:          String,
    @SerializedName("short_name")      val shortName:     String,
    @SerializedName("city")            val city:          String,
    @SerializedName("logo_url")        val logoUrl:       String?,
    @SerializedName("quota_balance")   val quotaBalance:  Int = 0,
    @SerializedName("is_partner")      val isPartner:     Boolean = false,
    @SerializedName("partner_tier")    val partnerTier:   String?,
)

data class CreateSchoolClaimRequest(
    @SerializedName("agent_id")  val agentId:  String,
    @SerializedName("school_id") val schoolId: String,
)

data class CreateUniversityClaimRequest(
    @SerializedName("agent_id")      val agentId:      String,
    @SerializedName("university_id") val universityId: String,
)
