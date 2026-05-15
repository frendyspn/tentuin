package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class Agent(
    @SerializedName("id")                   val id:                 String,
    @SerializedName("full_name")            val fullName:           String,
    @SerializedName("email")                val email:              String,
    @SerializedName("phone")                val phone:              String?,
    @SerializedName("referral_code")        val referralCode:       String,
    @SerializedName("status")               val status:             String,
    @SerializedName("is_owner")             val isOwner:            Boolean = false,
    @SerializedName("last_active_at")       val lastActiveAt:       String?,
    @SerializedName("bank_name")            val bankName:           String?,
    @SerializedName("bank_account_number")  val bankAccountNumber:  String?,
    @SerializedName("bank_account_name")    val bankAccountName:    String?,
    @SerializedName("created_at")           val createdAt:          String?,
)

data class CreateAgentRequest(
    @SerializedName("id")             val id:           String,
    @SerializedName("full_name")      val fullName:     String,
    @SerializedName("email")          val email:        String,
    @SerializedName("phone")          val phone:        String?,
    @SerializedName("referral_code")  val referralCode: String,
    // last_active_at omitted → server pakai DEFAULT now()
)

data class UpdateBankRequest(
    @SerializedName("bank_name")            val bankName:          String,
    @SerializedName("bank_account_number")  val bankAccountNumber: String,
    @SerializedName("bank_account_name")    val bankAccountName:   String,
    // updated_at omitted → trigger handle_updated_at akan refresh otomatis
)
