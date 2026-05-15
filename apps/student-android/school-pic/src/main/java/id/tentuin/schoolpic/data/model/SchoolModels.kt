package id.tentuin.schoolpic.data.model

import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")             val id:            String,
    @SerializedName("name")           val name:          String,
    @SerializedName("npsn")           val npsn:          String?,
    @SerializedName("city")           val city:          String,
    @SerializedName("province")       val province:      String,
    @SerializedName("address")        val address:       String?,
    @SerializedName("email")          val email:         String?,
    @SerializedName("phone")          val phone:         String?,
    @SerializedName("logo_url")       val logoUrl:       String?,
    @SerializedName("total_students") val totalStudents: Int = 0,
    @SerializedName("is_active")      val isActive:      Boolean = true,
)

/** Body PATCH /schools — hanya kolom yg boleh di-update PIC. */
data class UpdateSchoolRequest(
    @SerializedName("name")     val name:    String?,
    @SerializedName("address")  val address: String?,
    @SerializedName("email")    val email:   String?,
    @SerializedName("phone")    val phone:   String?,
    @SerializedName("logo_url") val logoUrl: String?,
)
