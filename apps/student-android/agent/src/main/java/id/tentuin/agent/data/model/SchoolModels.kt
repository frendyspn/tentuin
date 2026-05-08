package id.tentuin.agent.data.model

import com.google.gson.annotations.SerializedName

data class School(
    @SerializedName("id")              val id:            String,
    @SerializedName("name")            val name:          String,
    @SerializedName("npsn")            val npsn:          String?,
    @SerializedName("city")            val city:          String,
    @SerializedName("province")        val province:      String,
    @SerializedName("address")         val address:       String?,
    @SerializedName("email")           val email:         String?,
    @SerializedName("phone")           val phone:         String?,
    @SerializedName("logo_url")        val logoUrl:       String?,
    @SerializedName("total_students")  val totalStudents: Int = 0,
    @SerializedName("is_active")       val isActive:      Boolean = true,
)

data class SchoolTarget(
    @SerializedName("id")               val id:             String,
    @SerializedName("school_id")        val schoolId:       String,
    @SerializedName("year")             val year:           Int,
    @SerializedName("annual_target")    val annualTarget:   Int,
    @SerializedName("monthly_targets")  val monthlyTargets: List<Int>?,
)
