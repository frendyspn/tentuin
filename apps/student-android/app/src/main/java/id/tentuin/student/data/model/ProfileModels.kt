package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class Profile(
    @SerializedName("id")                      val id:                    String,
    @SerializedName("full_name")               val fullName:              String?,
    @SerializedName("school_name")             val schoolName:            String?,
    @SerializedName("city")                    val city:                  String?,
    @SerializedName("birth_year")              val birthYear:             Int?,
    @SerializedName("grade")                   val grade:                 Int?,
    @SerializedName("nisn")                    val nisn:                  String?,
    @SerializedName("avatar_url")              val avatarUrl:             String?,
    @SerializedName("has_completed_onboarding") val hasCompletedOnboarding: Boolean = false,
    @SerializedName("push_token")              val pushToken:             String?,
    @SerializedName("created_at")              val createdAt:             String?,
    @SerializedName("updated_at")              val updatedAt:             String?,
)

data class UpdateProfileRequest(
    @SerializedName("full_name")   val fullName:   String?,
    @SerializedName("school_name") val schoolName: String?,
    @SerializedName("city")        val city:       String?,
    @SerializedName("birth_year")  val birthYear:  Int?,
    @SerializedName("grade")       val grade:      Int?,
    @SerializedName("nisn")        val nisn:       String?,
    @SerializedName("updated_at")  val updatedAt:  String = java.time.Instant.now().toString(),
)
