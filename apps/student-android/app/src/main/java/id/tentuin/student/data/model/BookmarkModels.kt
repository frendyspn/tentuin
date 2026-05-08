package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class UniversityBookmark(
    @SerializedName("id")            val id:           String,
    @SerializedName("user_id")       val userId:       String,
    @SerializedName("university_id") val universityId: String,
    @SerializedName("major_names")   val majorNames:   List<String> = emptyList(),
    @SerializedName("created_at")    val createdAt:    String?,
    @SerializedName("universities")  val university:   UniversityRow?,
)

data class CreateBookmarkRequest(
    @SerializedName("user_id")       val userId:       String,
    @SerializedName("university_id") val universityId: String,
    @SerializedName("major_names")   val majorNames:   List<String> = emptyList(),
)
