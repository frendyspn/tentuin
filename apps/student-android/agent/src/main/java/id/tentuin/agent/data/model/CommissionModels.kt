package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class AgentCommission(
    @SerializedName("id")               val id:            String,
    @SerializedName("agent_id")         val agentId:       String,
    @SerializedName("month")            val month:         Int,
    @SerializedName("year")             val year:          Int,
    @SerializedName("stream_a_amount")  val streamAAmount: Int,
    @SerializedName("stream_b_amount")  val streamBAmount: Int,
    @SerializedName("total_amount")     val totalAmount:   Int,
    @SerializedName("status")           val status:        String,
    @SerializedName("notes")            val notes:         String?,
    @SerializedName("created_at")       val createdAt:     String?,
)

data class AgentWithdrawal(
    @SerializedName("id")            val id:           String,
    @SerializedName("agent_id")      val agentId:      String,
    @SerializedName("amount")        val amount:       Int,
    @SerializedName("status")        val status:       String,
    @SerializedName("requested_at")  val requestedAt:  String,
    @SerializedName("processed_at")  val processedAt:  String?,
    @SerializedName("admin_notes")   val adminNotes:   String?,
)

data class CreateWithdrawalRequest(
    @SerializedName("agent_id") val agentId: String,
    @SerializedName("amount")   val amount:  Int,
)

data class UniversitySubscribeLog(
    @SerializedName("id")               val id:              String,
    @SerializedName("university_id")    val universityId:    String,
    @SerializedName("amount")           val amount:          Int,
    @SerializedName("quota_purchased")  val quotaPurchased:  Int,
    @SerializedName("commission_agent") val commissionAgent: Int,
    @SerializedName("subscribed_at")    val subscribedAt:    String,
)
