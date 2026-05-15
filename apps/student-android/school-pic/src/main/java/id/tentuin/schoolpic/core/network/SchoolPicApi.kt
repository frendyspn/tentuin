package id.tentuin.schoolpic.core.network

import id.tentuin.schoolpic.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SchoolPicApi {

    // ── Auth ──────────────────────────────────────────────────────────────
    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest,
    ): TokenResponse

    @POST("auth/v1/signup")
    suspend fun register(@Body body: RegisterRequest): TokenResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body body: RefreshRequest,
    ): TokenResponse

    @POST("auth/v1/logout")
    suspend fun logout(@Header("Authorization") token: String): ResponseBody

    // ── Profile ───────────────────────────────────────────────────────────
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id")     id:     String,    // format: "eq.{uuid}"
        @Query("select") select: String = "*",
    ): List<Profile>

    /** Daftar siswa di sekolah PIC ini. */
    @GET("rest/v1/profiles")
    suspend fun getStudentsBySchool(
        @Query("school_id") schoolId: String,    // format: "eq.{uuid}"
        @Query("role")      role:     String = "eq.student",
        @Query("order")     order:    String = "full_name.asc",
        @Query("select")    select:   String = "id,full_name,school_name,city,birth_year,avatar_url",
        @Query("limit")     limit:    Int = 200,
        @Query("offset")    offset:   Int = 0,
    ): List<Profile>

    @GET("rest/v1/profiles")
    suspend fun searchStudents(
        @Query("school_id") schoolId:   String,
        @Query("role")      role:       String = "eq.student",
        @Query("full_name") nameFilter: String,   // format: "ilike.*query*"
        @Query("order")     order:      String = "full_name.asc",
        @Query("select")    select:     String = "id,full_name,school_name,city,birth_year,avatar_url",
        @Query("limit")     limit:      Int = 200,
    ): List<Profile>

    // ── School ────────────────────────────────────────────────────────────
    @GET("rest/v1/schools")
    suspend fun getSchool(
        @Query("id")     id:     String,
        @Query("select") select: String = "*",
    ): List<School>

    @PATCH("rest/v1/schools")
    @Headers("Prefer: return=representation")
    suspend fun updateSchool(
        @Query("id") id:   String,
        @Body        body: UpdateSchoolRequest,
    ): List<School>

    // ── Commissions ───────────────────────────────────────────────────────
    @GET("rest/v1/school_commissions")
    suspend fun getCommissions(
        @Query("school_id") schoolId: String,
        @Query("order")     order:    String = "year.desc,month.desc",
        @Query("select")    select:   String = "*",
        @Query("limit")     limit:    Int = 100,
    ): List<SchoolCommission>

    @GET("rest/v1/school_commissions")
    suspend fun getCommissionsByYear(
        @Query("school_id") schoolId: String,
        @Query("year")      year:     String,
        @Query("order")     order:    String = "month.asc",
        @Query("select")    select:   String = "*",
    ): List<SchoolCommission>

    // ── RPC ───────────────────────────────────────────────────────────────
    @POST("rest/v1/rpc/bind_school_pic_to_school")
    suspend fun bindSchoolPicToSchool(@Body body: BindRequest): List<BindResult>
}
