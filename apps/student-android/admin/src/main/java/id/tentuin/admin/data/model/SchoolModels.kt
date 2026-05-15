package id.tentuin.admin.data.model

import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")             val id:            String,
    @SerializedName("name")           val name:          String,
    @SerializedName("npsn")           val npsn:          String?,
    @SerializedName("city")           val city:          String,
    @SerializedName("province")       val province:      String,
    @SerializedName("address")        val address:       String?,
    @SerializedName("email")          val email:         String?,
    @SerializedName("phone")          val phone:         String?,
    @SerializedName("logo_url")       val logoUrl:       String?,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("is_active")      val isActive:      Boolean = true,
    @SerializedName("created_at")     val createdAt:     String?,
    @SerializedName("updated_at")     val updatedAt:     String?,
)

data class SchoolClaimEmbed(
    @SerializedName("id")          val id:          String,
    @SerializedName("agent_id")    val agentId:     String,
    @SerializedName("status")      val status:      String,
    @SerializedName("claim_code")  val claimCode:   String?,
    @SerializedName("claimed_at")  val claimedAt:   String?,
    @SerializedName("verified_at") val verifiedAt:  String?,
    @SerializedName("expires_at")  val expiresAt:   String?,
    @SerializedName("agent")       val agent:       AgentBrief?,
)

data class SchoolWithClaims(
    @SerializedName("id")             val id:            String,
    @SerializedName("name")           val name:          String,
    @SerializedName("npsn")           val npsn:          String?,
    @SerializedName("city")           val city:          String,
    @SerializedName("province")       val province:      String,
    @SerializedName("address")        val address:       String?,
    @SerializedName("email")          val email:         String?,
    @SerializedName("phone")          val phone:         String?,
    @SerializedName("logo_url")       val logoUrl:       String?,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("is_active")      val isActive:      Boolean = true,
    @SerializedName("claims")         val claims:        List<SchoolClaimEmbed>? = null,
) {
    val activeClaim: SchoolClaimEmbed? get() = claims?.firstOrNull { it.status == "active" }
    val pendingClaim: SchoolClaimEmbed? get() = claims?.firstOrNull { it.status == "pending" }
}
