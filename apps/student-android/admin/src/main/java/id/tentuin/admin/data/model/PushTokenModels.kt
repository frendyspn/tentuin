package id.tentuin.admin.data.model

import com.google.gson.annotations.SerializedName

data class UpsertPushTokenRequest(
    @SerializedName("user_id")      val userId:      String,
    @SerializedName("fcm_token")    val fcmToken:    String,
    @SerializedName("platform")     val platform:    String = "android",
    @SerializedName("device_label") val deviceLabel: String? = null,
)
