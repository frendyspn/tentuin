package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class Question(
    @SerializedName("id")           val id:          String,
    @SerializedName("order_number") val orderNumber: Int,
    @SerializedName("text")         val text:        String,
    @SerializedName("category")     val category:    String, // "realistic"|"investigative"|"artistic"|"social"|"enterprising"|"conventional"
    @SerializedName("is_active")    val isActive:    Boolean = true,
)
