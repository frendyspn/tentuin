package id.tentuin.schoolpic.data.repository

import id.tentuin.schoolpic.core.datastore.SessionDataStore
import id.tentuin.schoolpic.core.network.SchoolPicApi
import id.tentuin.schoolpic.data.model.SchoolCommission
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommissionRepository @Inject constructor(
    private val api: SchoolPicApi,
    private val session: SessionDataStore,
) {
    suspend fun getAllCommissions(): Result<List<SchoolCommission>> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        api.getCommissions(schoolId = "eq.$schoolId")
    }

    suspend fun getCommissionsByYear(year: Int): Result<List<SchoolCommission>> = runCatching {
        val schoolId = session.schoolId.first() ?: error("Belum terhubung ke sekolah.")
        api.getCommissionsByYear(schoolId = "eq.$schoolId", year = "eq.$year")
    }
}
