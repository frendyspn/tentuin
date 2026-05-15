package id.tentuin.schoolpic.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
)

data class RegisterRequest(
    @SerializedName("email")    val email:    String,
    @SerializedName("password") val password: String,
    @SerializedName("data")     val data:     PicMeta,
)

data class PicMeta(
    @SerializedName("full_name") val fullName: String,
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
