package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class RiasecScores(
    val realistic:     Int = 0,
    val investigative: Int = 0,
    val artistic:      Int = 0,
    val social:        Int = 0,
    val enterprising:  Int = 0,
    val conventional:  Int = 0,
)

data class TestResult(
    @SerializedName("id")          val id:         String,
    @SerializedName("user_id")     val userId:     String,
    @SerializedName("riasec_code") val riasecCode: String,
    @SerializedName("scores")      val scores:     Map<String, Double>,
    @SerializedName("completed_at") val completedAt: String?,
)

data class SaveTestResultRequest(
    @SerializedName("user_id")     val userId:    String,
    @SerializedName("scores")      val scores:    Map<String, Int>,
    @SerializedName("riasec_code") val riasecCode: String,
)
