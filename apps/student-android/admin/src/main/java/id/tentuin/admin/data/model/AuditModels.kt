package id.tentuin.admin.data.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AdminAuditLog(
    @SerializedName("id")            val id:           String,
    @SerializedName("admin_id")      val adminId:      String?,
    @SerializedName("action")        val action:       String,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("resource_id")   val resourceId:   String?,
    @SerializedName("old_values")    val oldValues:    JsonElement?,
    @SerializedName("new_values")    val newValues:    JsonElement?,
    @SerializedName("created_at")    val createdAt:    String?,
    @SerializedName("admin")         val admin:        AdminBrief?,
)

data class AdminBrief(
    @SerializedName("full_name") val fullName: String?,
)

data class CreateAuditLogRequest(
    @SerializedName("admin_id")      val adminId:      String,
    @SerializedName("action")        val action:       String,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("resource_id")   val resourceId:   String?,
    @SerializedName("old_values")    val oldValues:    Map<String, Any?>?,
    @SerializedName("new_values")    val newValues:    Map<String, Any?>?,
)
