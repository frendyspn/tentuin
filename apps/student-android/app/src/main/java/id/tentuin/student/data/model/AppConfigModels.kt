package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class AppConfig(
    @SerializedName("platform")    val platform:   String,
    @SerializedName("min_version") val minVersion: String,
    @SerializedName("store_url")   val storeUrl:   String,
)

sealed class ForceUpdateResult {
    object UpToDate : ForceUpdateResult()
    data class UpdateRequired(val storeUrl: String) : ForceUpdateResult()
}
