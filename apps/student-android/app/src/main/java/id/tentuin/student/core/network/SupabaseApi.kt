package id.tentuin.student.core.network

import id.tentuin.student.data.model.AppConfig
import id.tentuin.student.data.model.CityRow
import id.tentuin.student.data.model.CreateBookmarkRequest
import id.tentuin.student.data.model.LoginRequest
import id.tentuin.student.data.model.MajorRow
import id.tentuin.student.data.model.Profile
import id.tentuin.student.data.model.Question
import id.tentuin.student.data.model.RefreshRequest
import id.tentuin.student.data.model.SaveTestResultRequest
import id.tentuin.student.data.model.SchoolRow
import id.tentuin.student.data.model.SignupRequest
import id.tentuin.student.data.model.TestResult
import id.tentuin.student.data.model.TokenResponse
import id.tentuin.student.data.model.UniversityBookmark
import id.tentuin.student.data.model.UniversityRow
import id.tentuin.student.data.model.UpdateProfileRequest
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseApi {

    // ── Auth ─────────────────────────────────────────────────────────────

    @POST("auth/v1/token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body body: LoginRequest,
    ): TokenResponse

    @POST("auth/v1/signup")
    suspend fun register(
        @Body body: SignupRequest,
    ): TokenResponse

    @POST("auth/v1/token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body body: RefreshRequest,
    ): TokenResponse

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
    ): ResponseBody

    // ── Profiles ──────────────────────────────────────────────────────────

    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id")     id:     String,
        @Query("select") select: String = "*",
    ): List<Profile>

    @PATCH("rest/v1/profiles")
    @Headers("Prefer: return=minimal")
    suspend fun updateProfile(
        @Query("id")  id:   String,
        @Body   body: UpdateProfileRequest,
    ): retrofit2.Response<ResponseBody>

    @GET("rest/v1/schools")
    suspend fun searchSchools(
        @Query("name") name: String, // format: ilike.*query*
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 10,
    ): List<SchoolRow>

    @GET("rest/v1/cities")
    suspend fun searchCities(
        @Query("name") name: String, // format: ilike.*query*
        @Query("select") select: String = "*",
        @Query("limit") limit: Int = 10,
    ): List<CityRow>

    // ── App Config (Force Update) ─────────────────────────────────────────

    @GET("rest/v1/app_config")
    suspend fun getAppConfig(
        @Query("platform") platform: String = "eq.android",
        @Query("select")   select:   String = "*",
    ): List<AppConfig>

    // ── Questions ─────────────────────────────────────────────────────────

    @GET("rest/v1/questions")
    suspend fun getQuestions(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "order_number",
        @Query("select")    select:   String = "*",
    ): List<Question>

    // ── Test Results ──────────────────────────────────────────────────────

    @POST("rest/v1/test_results")
    @Headers("Prefer: return=representation")
    suspend fun saveTestResult(
        @Body body: SaveTestResultRequest,
    ): List<TestResult>

    @GET("rest/v1/test_results")
    suspend fun getTestHistory(
        @Query("user_id") userId: String? = null,
        @Query("order")   order:  String? = null,
        @Query("select")  select: String = "*",
    ): List<TestResult>

    // ── Universities ──────────────────────────────────────────────────────

    @GET("rest/v1/universities")
    suspend fun getUniversities(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "is_partner.desc,name.asc",
        @Query("select")    select:   String = "*",
        @Query("limit")     limit:    Int    = 100,
        @Query("offset")    offset:   Int    = 0,
    ): List<UniversityRow>

    @GET("rest/v1/universities")
    suspend fun searchUniversities(
        @Query("or")        or:       String,
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "is_partner.desc,name.asc",
        @Query("select")    select:   String = "*",
    ): List<UniversityRow>

    // ── Majors ────────────────────────────────────────────────────────────

    @GET("rest/v1/majors")
    suspend fun getMajors(
        @Query("is_active") isActive: String = "eq.true",
        @Query("order")     order:    String = "name.asc",
        @Query("select")    select:   String = "id,name,faculty,riasec_codes,university_id,is_active",
        @Query("limit")     limit:    Int    = 200,
    ): List<MajorRow>

    @GET("rest/v1/majors")
    suspend fun getMajorsByRiasec(
        @Query("riasec_codes") riasecCodes: String,
        @Query("is_active")    isActive:    String = "eq.true",
        @Query("order")        order:       String = "name.asc",
        @Query("select")       select:      String = "id,name,faculty,riasec_codes,university_id,is_active",
        @Query("limit")        limit:       Int    = 20,
    ): List<MajorRow>

    @GET("rest/v1/universities")
    suspend fun getUniversityDetail(
        @Query("id")     id:     String,
        @Query("select") select: String = "*,majors(*)",
    ): List<UniversityRow>

    // ── Bookmarks ─────────────────────────────────────────────────────────

    @GET("rest/v1/university_bookmarks")
    suspend fun getBookmarks(
        @Query("user_id") userId: String,
        @Query("select")  select: String = "*,universities(*)",
    ): List<UniversityBookmark>

    @POST("rest/v1/university_bookmarks")
    @Headers("Prefer: return=representation")
    suspend fun createBookmark(
        @Body body: CreateBookmarkRequest,
    ): List<UniversityBookmark>

    @DELETE("rest/v1/university_bookmarks")
    suspend fun deleteBookmark(
        @Query("user_id")       userId:      String,
        @Query("university_id") universityId: String,
    ): ResponseBody
}
