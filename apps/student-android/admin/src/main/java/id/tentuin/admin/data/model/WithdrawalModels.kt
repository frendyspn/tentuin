package id.tentuin.admin.data.model

import com.google.gson.annotations.SerializedName

data class AgentWithdrawal(
    @SerializedName("id")           val id:           String,
    @SerializedName("agent_id")     val agentId:      String,
    @SerializedName("amount")       val amount:       Int,
    @SerializedName("status")       val status:       String,    // requested|approved|rejected|transferred
    @SerializedName("requested_at") val requestedAt:  String?,
    @SerializedName("processed_at") val processedAt:  String?,
    @SerializedName("admin_notes")  val adminNotes:   String?,
)

data class WithdrawalWithAgent(
    @SerializedName("id")           val id:           String,
    @SerializedName("agent_id")     val agentId:      String,
    @SerializedName("amount")       val amount:       Int,
    @SerializedName("status")       val status:       String,
    @SerializedName("requested_at") val requestedAt:  String?,
    @SerializedName("processed_at") val processedAt:  String?,
    @SerializedName("admin_notes")  val adminNotes:   String?,
    @SerializedName("agent")        val agent:        AgentBrief?,
)

data class UpdateWithdrawalRequest(
    @SerializedName("status")       val status:       String,
    @SerializedName("admin_notes")  val adminNotes:   String? = null,
    @SerializedName("processed_at") val processedAt:  String? = null,
)
