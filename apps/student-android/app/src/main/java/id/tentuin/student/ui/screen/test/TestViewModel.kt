package id.tentuin.student.ui.screen.test

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.core.util.computeRiasecCode
import id.tentuin.student.core.util.computeScores
import id.tentuin.student.data.model.Question
import id.tentuin.student.data.model.RiasecScores
import id.tentuin.student.data.model.TestResult
import id.tentuin.student.data.repository.QuestionRepository
import id.tentuin.student.data.repository.TestRepository
import id.tentuin.student.ui.component.share.ShareCardGenerator
import id.tentuin.student.ui.component.share.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class TestPhase { LOADING, IN_PROGRESS, SUBMITTING, DONE, ERROR }

data class TestUiState(
    val questions:             List<Question>    = emptyList(),
    val answers:               Map<String, Int>  = emptyMap(),
    val currentIndex:          Int               = 0,
    val phase:                 TestPhase         = TestPhase.LOADING,
    val error:                 String?           = null,
    val finalScores:           RiasecScores?     = null,
    val finalCode:             String            = "",
    val savedResult:           TestResult?       = null,
    val isGuest:               Boolean           = false,
    val isGeneratingShareCard: Boolean           = false,
)

@HiltViewModel
class TestViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val testRepository: TestRepository,
    private val sessionDataStore: SessionDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TestUiState())
    val uiState: StateFlow<TestUiState> = _uiState.asStateFlow()

    init { loadQuestions() }

    fun loadQuestions() {
        viewModelScope.launch {
            val userId  = sessionDataStore.userId.first()
            _uiState.update { it.copy(phase = TestPhase.LOADING, isGuest = userId == null) }
            questionRepository.getQuestions()
                .onSuccess { questions ->
                    _uiState.update { it.copy(questions = questions, phase = TestPhase.IN_PROGRESS) }
                }
                .onFailure {
                    _uiState.update { it.copy(phase = TestPhase.ERROR, error = "Gagal memuat soal. Periksa koneksi internet.") }
                }
        }
    }

    fun canStartTest(): Boolean {
        return !_uiState.value.isGuest
    }

    fun answer(questionId: String, value: Int) {
        _uiState.update { state ->
            val newAnswers = state.answers + (questionId to value)
            val newIndex   = (state.currentIndex + 1).coerceAtMost(state.questions.size - 1)
            val isLast     = state.currentIndex == state.questions.size - 1
            if (isLast) {
                submitTest(newAnswers, state.questions)
            }
            state.copy(answers = newAnswers, currentIndex = if (!isLast) newIndex else state.currentIndex)
        }
    }

    fun previousQuestion() {
        _uiState.update { it.copy(currentIndex = (it.currentIndex - 1).coerceAtLeast(0)) }
    }

    fun resetTest() {
        _uiState.update { TestUiState(questions = it.questions, phase = TestPhase.IN_PROGRESS, isGuest = it.isGuest) }
    }

    /**
     * Generate share card dari hasil RIASEC dan buka Android share sheet.
     * Dijalankan di Dispatchers.Default agar tidak memblokir UI thread.
     */
    fun shareResult(context: Context) {
        val scores = _uiState.value.finalScores ?: return
        val code   = _uiState.value.finalCode
        if (code.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingShareCard = true) }
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    ShareCardGenerator.generate(context, code, scores)
                }
                ShareHelper.shareImage(
                    context = context,
                    bitmap  = bitmap,
                    text    = "Tipe kepribadianku $code! Cari tahu tipe kepribadianmu juga di tentuin.id/tes 🎯"
                )
            } finally {
                _uiState.update { it.copy(isGeneratingShareCard = false) }
            }
        }
    }

    private fun submitTest(answers: Map<String, Int>, questions: List<Question>) {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = TestPhase.SUBMITTING) }
            val scores     = computeScores(questions, answers)
            val riasecCode = computeRiasecCode(scores)
            val userId     = sessionDataStore.userId.first()

            var savedResult: TestResult? = null
            if (userId != null) {
                savedResult = testRepository.saveResult(userId, scores, riasecCode).getOrNull()
            }
            _uiState.update {
                it.copy(
                    phase       = TestPhase.DONE,
                    finalScores = scores,
                    finalCode   = riasecCode,
                    savedResult = savedResult,
                )
            }
        }
    }
}
