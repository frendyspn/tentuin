package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class SchoolRow(
    @SerializedName("id")   val id:   String,
    @SerializedName("name") val name: String,
    @SerializedName("city") val city: String?,
)

data class CityRow(
    @SerializedName("id")   val id:   String,
    @SerializedName("name") val name: String,
)
