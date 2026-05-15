package id.tentuin.schoolpic.data.repository

import id.tentuin.schoolpic.core.datastore.SessionDataStore
import id.tentuin.schoolpic.core.network.SchoolPicApi
import id.tentuin.schoolpic.data.model.School
import id.tentuin.schoolpic.data.model.UpdateSchoolRequest
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchoolRepository @Inject constructor(
    private val api: SchoolPicApi,
    private val session: SessionDataStore,
) {
    suspend fun getMySchool(): Result<School> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        val rows = api.getSchool(id = "eq.$schoolId")
        rows.firstOrNull() ?: error("Data sekolah tidak ditemukan.")
    }

    suspend fun updateMySchool(req: UpdateSchoolRequest): Result<School> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        val rows = api.updateSchool(id = "eq.$schoolId", body = req)
        rows.firstOrNull() ?: error("Update gagal: tidak ada data terkembali.")
    }
}
