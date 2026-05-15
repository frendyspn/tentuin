package id.tentuin.admin.data.model

import com.google.gson.annotations.SerializedName

data class UniversityClaimEmbed(
    @SerializedName("id")          val id:          String,
    @SerializedName("agent_id")    val agentId:     String,
    @SerializedName("status")      val status:      String,
    @SerializedName("claim_code")  val claimCode:   String?,
    @SerializedName("claimed_at")  val claimedAt:   String?,
    @SerializedName("verified_at") val verifiedAt:  String?,
    @SerializedName("expires_at")  val expiresAt:   String?,
    @SerializedName("agent")       val agent:       AgentBrief?,
)

data class UniversityWithClaims(
    @SerializedName("id")                    val id:                  String,
    @SerializedName("name")                  val name:                String,
    @SerializedName("city")                  val city:                String?,
    @SerializedName("province")              val province:            String?,
    @SerializedName("logo_url")              val logoUrl:             String?,
    @SerializedName("is_partner")            val isPartner:           Boolean = false,
    @SerializedName("partner_tier")          val partnerTier:         String?,
    @SerializedName("pic_name")              val picName:             String?,
    @SerializedName("pic_phone")             val picPhone:            String?,
    @SerializedName("quota_balance")         val quotaBalance:        Int = 0,
    @SerializedName("total_quota_purchased") val totalQuotaPurchased: Int = 0,
    @SerializedName("claims")                val claims:              List<UniversityClaimEmbed>? = null,
) {
    val activeClaim:  UniversityClaimEmbed? get() = claims?.firstOrNull { it.status == "active" }
    val pendingClaim: UniversityClaimEmbed? get() = claims?.firstOrNull { it.status == "pending" }
}

data class UniversitySubscribeLog(
    @SerializedName("id")                val id:               String,
    @SerializedName("university_id")     val universityId:     String,
    @SerializedName("agent_id")          val agentId:          String?,
    @SerializedName("amount")            val amount:           Int,
    @SerializedName("quota_purchased")   val quotaPurchased:   Int,
    @SerializedName("commission_agent")  val commissionAgent:  Int,
    @SerializedName("subscribed_at")     val subscribedAt:     String?,
)

data class RecordSubscribeRequest(
    @SerializedName("university_id")    val universityId:    String,
    @SerializedName("agent_id")         val agentId:         String?,
    @SerializedName("amount")           val amount:          Int,
    @SerializedName("quota_purchased")  val quotaPurchased:  Int,
    @SerializedName("commission_agent") val commissionAgent: Int,
)
