package id.tentuin.university.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
    @SerializedName("data")     val data:     UserMeta,
)

data class UserMeta(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone")     val phone:    String? = null,
)

data class RefreshRequest(
    @SerializedName("refresh_token") val refreshToken: String,
)

data class TokenResponse(
    @SerializedName("access_token")  val accessToken:  String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("user")          val user:         AuthUser?,
    @SerializedName("expires_in")    val expiresIn:    Long = 3600,
)

data class AuthUser(
    @SerializedName("id")    val id:    String,
    @SerializedName("email") val email: String,
)

data class AuthUserDetail(
    @SerializedName("id")            val id:           String,
    @SerializedName("email")         val email:        String,
    @SerializedName("user_metadata") val userMetadata: UserMeta? = null,
)
