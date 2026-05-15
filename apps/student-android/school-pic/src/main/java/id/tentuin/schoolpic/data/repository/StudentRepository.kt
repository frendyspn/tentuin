package id.tentuin.schoolpic.data.repository

import id.tentuin.schoolpic.core.datastore.SessionDataStore
import id.tentuin.schoolpic.core.network.SchoolPicApi
import id.tentuin.schoolpic.data.model.Profile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val api: SchoolPicApi,
    private val session: SessionDataStore,
) {
    suspend fun getStudents(): Result<List<Profile>> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        api.getStudentsBySchool(schoolId = "eq.$schoolId")
    }

    suspend fun searchStudents(query: String): Result<List<Profile>> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        if (query.isBlank()) {
            api.getStudentsBySchool(schoolId = "eq.$schoolId")
        } else {
            val safe = query.trim().replace("%", "").replace("*", "")
            api.searchStudents(
                schoolId = "eq.$schoolId",
                nameFilter = "ilike.*$safe*",
            )
        }
    }
}
