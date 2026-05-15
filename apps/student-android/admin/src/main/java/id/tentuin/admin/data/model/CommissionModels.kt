package id.tentuin.admin.data.model

import com.google.gson.annotations.SerializedName

data class AgentCommissionWithAgent(
    @SerializedName("id")              val id:             String,
    @SerializedName("agent_id")        val agentId:        String,
    @SerializedName("month")           val month:          Int,
    @SerializedName("year")            val year:           Int,
    @SerializedName("stream_a_amount") val streamAAmount:  Int = 0,
    @SerializedName("stream_b_amount") val streamBAmount:  Int = 0,
    @SerializedName("total_amount")    val totalAmount:    Int = 0,
    @SerializedName("status")          val status:         String,
    @SerializedName("notes")           val notes:          String?,
    @SerializedName("agent")           val agent:          AgentBrief?,
)
