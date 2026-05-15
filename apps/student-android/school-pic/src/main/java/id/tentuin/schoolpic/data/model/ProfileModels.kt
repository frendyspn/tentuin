package id.tentuin.schoolpic.data.model

import com.google.gson.annotations.SerializedName

/** profiles row — subset yg dipakai aplikasi PIC. */
data class Profile(
    @SerializedName("id")          val id:        String,
    @SerializedName("full_name")   val fullName:  String?,
    @SerializedName("school_name") val schoolName: String?,
    @SerializedName("school_id")   val schoolId:  String?,
    @SerializedName("city")        val city:      String?,
    @SerializedName("birth_year")  val birthYear: Int?,
    @SerializedName("avatar_url")  val avatarUrl: String?,
    @SerializedName("role")        val role:      String?,
)
