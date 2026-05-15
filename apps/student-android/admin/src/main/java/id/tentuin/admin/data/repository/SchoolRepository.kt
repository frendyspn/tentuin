package id.tentuin.admin.data.repository

import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.SchoolWithClaims
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolRepository @Inject constructor(
    private val api: AdminApi,
) {
    suspend fun list(): Result<List<SchoolWithClaims>> = runCatching {
        api.listSchools()
    }

    suspend fun getById(id: String): Result<SchoolWithClaims?> = runCatching {
        api.getSchool(id = "eq.$id").firstOrNull()
    }
}
