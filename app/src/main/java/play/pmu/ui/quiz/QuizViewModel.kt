package play.pmu.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.QuestionSource
import play.pmu.data.repository.SettingsRepository
import play.pmu.data.repository.TriviaRepository
import play.pmu.domain.model.GameType
import play.pmu.domain.model.TriviaCategory
import play.pmu.domain.model.TriviaQuestion
import play.pmu.navigation.QuizGameRoute
import javax.inject.Inject

sealed interface QuizUiState {
    data object Loading : QuizUiState

    data object Error : QuizUiState

    data class Playing(
        val questions: List<TriviaQuestion>,
        val source: QuestionSource,
        val questionIndex: Int = 0,
        val correctCount: Int = 0,
        val selectedAnswer: String? = null,
    ) : QuizUiState {
        val currentQuestion: TriviaQuestion get() = questions[questionIndex]
        val totalQuestions: Int get() = questions.size
        val isLastQuestion: Boolean get() = questionIndex == questions.lastIndex
    }

    data class Finished(val resultId: Long) : QuizUiState
}

@HiltViewModel
class QuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val triviaRepository: TriviaRepository,
    private val settingsRepository: SettingsRepository,
    private val resultsRepository: GameResultsRepository,
) : ViewModel() {

    private val category: TriviaCategory =
        TriviaCategory.fromApiId(savedStateHandle.toRoute<QuizGameRoute>().categoryApiId)

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        _uiState.value = QuizUiState.Loading
        viewModelScope.launch {
            val count = settingsRepository.settings.first().questionCount
            val loaded = triviaRepository.loadQuestions(category, count)
            _uiState.value = if (loaded.questions.isEmpty()) {
                QuizUiState.Error
            } else {
                QuizUiState.Playing(questions = loaded.questions, source = loaded.source)
            }
        }
    }

    fun selectAnswer(answer: String) {
        val state = _uiState.value
        if (state !is QuizUiState.Playing || state.selectedAnswer != null) return
        _uiState.value = state.copy(selectedAnswer = answer)
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state !is QuizUiState.Playing) return
        val selected = state.selectedAnswer ?: return

        val correctCount =
            state.correctCount + if (selected == state.currentQuestion.correctAnswer) 1 else 0

        if (state.isLastQuestion) {
            saveResult(correctCount, state.totalQuestions)
        } else {
            _uiState.update {
                state.copy(
                    questionIndex = state.questionIndex + 1,
                    correctCount = correctCount,
                    selectedAnswer = null,
                )
            }
        }
    }

    private fun saveResult(correctCount: Int, total: Int) {
        viewModelScope.launch {
            val id = resultsRepository.save(
                GameResultEntity(
                    gameType = GameType.QUIZ,
                    score = correctCount,
                    total = total,
                    durationSeconds = 0,
                    playedAt = System.currentTimeMillis(),
                )
            )
            _uiState.value = QuizUiState.Finished(id)
        }
    }
}
