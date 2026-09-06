package play.pmu.ui.stepbystep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.StepByStepRepository
import play.pmu.domain.model.GameType
import play.pmu.domain.model.StepByStepPuzzle
import javax.inject.Inject
import kotlin.random.Random

data class StepByStepUiState(
    val puzzle: StepByStepPuzzle? = null,
    val currentStepIndex: Int = 0,
    val revealedClues: List<String> = emptyList(),
    val inputGuess: String = "",
    val isWrongGuess: Boolean = false,
    val isFinished: Boolean = false,
    val isGuessedCorrectly: Boolean = false,
    val score: Int = 0,
    val finishedResultId: Long? = null,
    val isLoading: Boolean = true,
) {
    val currentStepNumber: Int get() = currentStepIndex + 1
    val totalSteps: Int get() = 7
    val currentPotentialPoints: Int
        get() = if (currentStepIndex in StepByStepViewModel.POINTS_BY_STEP.indices) {
            StepByStepViewModel.POINTS_BY_STEP[currentStepIndex]
        } else 0
}

@HiltViewModel
class StepByStepViewModel @Inject constructor(
    private val stepByStepRepository: StepByStepRepository,
    private val resultsRepository: GameResultsRepository,
    private val random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepByStepUiState())
    val uiState: StateFlow<StepByStepUiState> = _uiState.asStateFlow()

    init {
        loadPuzzle()
    }

    private fun loadPuzzle() {
        val puzzle = stepByStepRepository.getRandomPuzzle(random)
        if (puzzle != null) {
            _uiState.update {
                it.copy(
                    puzzle = puzzle,
                    currentStepIndex = 0,
                    revealedClues = listOf(puzzle.clues.firstOrNull().orEmpty()),
                    inputGuess = "",
                    isWrongGuess = false,
                    isFinished = false,
                    isGuessedCorrectly = false,
                    score = 0,
                    finishedResultId = null,
                    isLoading = false,
                )
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onGuessChanged(guess: String) {
        _uiState.update {
            it.copy(inputGuess = guess, isWrongGuess = false)
        }
    }

    fun submitGuess() {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        if (state.isFinished) return

        val guess = state.inputGuess.trim()
        if (guess.isEmpty()) return

        if (isCorrectGuess(guess, puzzle.solution)) {
            val awardedScore = POINTS_BY_STEP.getOrElse(state.currentStepIndex) { 0 }
            _uiState.update {
                it.copy(
                    isFinished = true,
                    isGuessedCorrectly = true,
                    score = awardedScore,
                    revealedClues = puzzle.clues,
                    isWrongGuess = false,
                )
            }
            saveResult(awardedScore = awardedScore, guessed = true)
        } else {
            _uiState.update { it.copy(isWrongGuess = true) }
        }
    }

    fun nextClue() {
        val state = _uiState.value
        val puzzle = state.puzzle ?: return
        if (state.isFinished) return

        if (state.currentStepIndex < 6) {
            val nextIndex = state.currentStepIndex + 1
            _uiState.update {
                it.copy(
                    currentStepIndex = nextIndex,
                    revealedClues = puzzle.clues.take(nextIndex + 1),
                    inputGuess = "",
                    isWrongGuess = false,
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isFinished = true,
                    isGuessedCorrectly = false,
                    score = 0,
                    revealedClues = puzzle.clues,
                    isWrongGuess = false,
                )
            }
            saveResult(awardedScore = 0, guessed = false)
        }
    }

    private fun saveResult(awardedScore: Int, guessed: Boolean) {
        val puzzle = _uiState.value.puzzle ?: return
        viewModelScope.launch {
            val id = resultsRepository.save(
                GameResultEntity(
                    gameType = GameType.STEP_BY_STEP,
                    score = awardedScore,
                    total = MAX_POSSIBLE_SCORE,
                    durationSeconds = 0,
                    playedAt = System.currentTimeMillis(),
                    correctItems = if (guessed) listOf(puzzle.solution) else emptyList(),
                    skippedItems = if (!guessed) listOf(puzzle.solution) else emptyList(),
                )
            )
            _uiState.update { it.copy(finishedResultId = id) }
        }
    }

    fun isCorrectGuess(guess: String, target: String): Boolean {
        val cleanGuess = normalize(guess)
        val cleanTarget = normalize(target)
        if (cleanGuess.isEmpty() || cleanTarget.isEmpty()) return false
        return cleanGuess == cleanTarget
    }

    private fun normalize(text: String): String {
        return text.trim()
            .lowercase()
            .replace("č", "c")
            .replace("ć", "c")
            .replace("š", "s")
            .replace("ž", "z")
            .replace("đ", "dj")
            .replace("-", " ")
            .replace(Regex("[^a-z0-9 ]"), "")
            .replace(Regex("\\s+"), " ")
    }

    companion object {
        val POINTS_BY_STEP = listOf(30, 25, 20, 15, 10, 5, 0)
        const val MAX_POSSIBLE_SCORE = 30
    }
}
