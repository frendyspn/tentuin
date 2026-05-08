package id.tentuin.student.data.repository

import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.CityRow
import id.tentuin.student.data.model.Profile
import id.tentuin.student.data.model.SchoolRow
import id.tentuin.student.data.model.UpdateProfileRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun getProfile(userId: String): Result<Profile?> = runCatching {
        api.getProfile(id = "eq.$userId").firstOrNull()
    }

    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): Result<Unit> = runCatching {
        val response = api.updateProfile(id = "eq.$userId", body = request)
        if (!response.isSuccessful) {
            throw Exception("Failed to update profile: ${response.code()} ${response.errorBody()?.string()}")
        }
    }

    suspend fun searchSchools(query: String): Result<List<SchoolRow>> = runCatching {
        api.searchSchools(name = query)
    }

    suspend fun searchCities(query: String): Result<List<CityRow>> = runCatching {
        api.searchCities(name = query)
    }
}
