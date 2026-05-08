package id.tentuin.student.data.model

import com.google.gson.annotations.SerializedName

data class UniversityRow(
    @SerializedName("id")           val id:          String,
    @SerializedName("name")         val name:        String,
    @SerializedName("short_name")   val shortName:   String,
    @SerializedName("city")         val city:        String,
    @SerializedName("province")     val province:    String,
    @SerializedName("type")         val type:        String,        // "negeri" | "swasta"
    @SerializedName("logo_url")     val logoUrl:     String?,
    @SerializedName("website")      val website:     String?,
    @SerializedName("is_partner")   val isPartner:   Boolean = false,
    @SerializedName("partner_tier") val partnerTier: String?,       // "basic" | "premium" | null
    @SerializedName("is_active")    val isActive:    Boolean = true,
    @SerializedName("description")  val description: String? = null,
    @SerializedName("majors")       val majors:      List<MajorRow> = emptyList(),
)

data class MajorRow(
    @SerializedName("id")            val id:           String,
    @SerializedName("university_id") val universityId: String,
    @SerializedName("name")          val name:         String,
    @SerializedName("faculty")       val faculty:      String?,
    @SerializedName("riasec_codes")  val riasecCodes:  List<String> = emptyList(),
    @SerializedName("is_active")     val isActive:     Boolean = true,
)

enum class ExploreTab { UNIVERSITIES, MAJORS }
