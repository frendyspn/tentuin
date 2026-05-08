package id.tentuin.student.data.repository

import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.data.model.Question
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun getQuestions(): Result<List<Question>> = runCatching {
        api.getQuestions()
    }
}
