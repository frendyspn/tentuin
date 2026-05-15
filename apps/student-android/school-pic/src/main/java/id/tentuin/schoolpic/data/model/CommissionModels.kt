package id.tentuin.schoolpic.data.model

import com.google.gson.annotations.SerializedName

data class SchoolCommission(
    @SerializedName("id")         val id:        String,
    @SerializedName("school_id")  val schoolId:  String,
    @SerializedName("agent_id")   val agentId:   String?,
    @SerializedName("month")      val month:     Int,
    @SerializedName("year")       val year:      Int,
    @SerializedName("amount")     val amount:    Long,
    @SerializedName("status")     val status:    String,   // pending|paid|cancelled
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
)
