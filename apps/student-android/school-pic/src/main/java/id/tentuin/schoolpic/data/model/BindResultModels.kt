package id.tentuin.schoolpic.data.model

import com.google.gson.annotations.SerializedName

/** Body untuk RPC bind_school_pic_to_school. */
data class BindRequest(
    @SerializedName("p_claim_code") val claimCode: String,
)

/** Hasil RPC: returns table (success bool, message text, school_id uuid). */
data class BindResult(
    @SerializedName("success")   val success:  Boolean,
    @SerializedName("message")   val message:  String,
    @SerializedName("school_id") val schoolId: String?,
)
