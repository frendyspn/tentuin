package id.tentuin.student.data.repository

import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.MajorRow
import id.tentuin.student.data.model.UniversityRow
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun fetchUniversities(): Result<List<UniversityRow>> = runCatching {
        api.getUniversities(limit = 200)
    }

    suspend fun searchUniversities(query: String): Result<List<UniversityRow>> = runCatching {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val orFilter = "name.ilike.*$encoded*,short_name.ilike.*$encoded*,city.ilike.*$encoded*"
        api.searchUniversities(or = "($orFilter)")
    }

    suspend fun fetchMajors(): Result<List<MajorRow>> = runCatching {
        api.getMajors(limit = 500)
    }

    suspend fun getMajorsByRiasec(dominantCode: String): Result<List<MajorRow>> = runCatching {
        api.getMajorsByRiasec(riasecCodes = "cs.{$dominantCode}")
    }

    suspend fun getUniversityDetail(id: String): Result<UniversityRow?> = runCatching {
        api.getUniversityDetail(id = "eq.$id").firstOrNull()
    }
}
