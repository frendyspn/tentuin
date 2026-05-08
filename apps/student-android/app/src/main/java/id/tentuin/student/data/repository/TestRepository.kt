package id.tentuin.student.data.repository

import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.RiasecScores
import id.tentuin.student.data.model.SaveTestResultRequest
import id.tentuin.student.data.model.TestResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun saveResult(
        userId: String,
        scores: RiasecScores,
        riasecCode: String,
    ): Result<TestResult> = runCatching {
        val scoresMap = mapOf(
            "realistic"     to scores.realistic,
            "investigative" to scores.investigative,
            "artistic"      to scores.artistic,
            "social"        to scores.social,
            "enterprising"  to scores.enterprising,
            "conventional"  to scores.conventional,
        )
        api.saveTestResult(SaveTestResultRequest(userId, scoresMap, riasecCode))
            .firstOrNull() ?: error("No result returned")
    }

    suspend fun getHistory(userId: String): Result<List<TestResult>> = runCatching {
        api.getTestHistory(
            userId = "eq.$userId",
            order = "completed_at.desc"
        )
    }

    suspend fun getLatestResult(userId: String): Result<TestResult?> = runCatching {
        api.getTestHistory(
            userId = "eq.$userId",
            order = "completed_at.desc"
        ).firstOrNull()
    }
}
