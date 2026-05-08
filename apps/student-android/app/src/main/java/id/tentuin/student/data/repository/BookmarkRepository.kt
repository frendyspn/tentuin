package id.tentuin.student.data.repository

import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.CreateBookmarkRequest
import id.tentuin.student.data.model.UniversityBookmark
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun getBookmarks(userId: String): Result<List<UniversityBookmark>> = runCatching {
        api.getBookmarks(userId = "eq.$userId")
    }

    suspend fun createBookmark(userId: String, universityId: String): Result<UniversityBookmark> = runCatching {
        api.createBookmark(CreateBookmarkRequest(userId, universityId)).first()
    }

    suspend fun deleteBookmark(userId: String, universityId: String): Result<Unit> = runCatching {
        api.deleteBookmark(userId = "eq.$userId", universityId = "eq.$universityId")
        Unit
    }
}
