package play.pmu.ui.mathduel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import play.pmu.domain.game.AnswerDuel
import play.pmu.domain.game.MathQuestion
import play.pmu.domain.game.randomMathQuestion
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.Player
import javax.inject.Inject
import kotlin.random.Random

data class MathDuelUiState(
    val question: MathQuestion? = null,
    val duel: AnswerDuel = AnswerDuel(),
)

@HiltViewModel
class MathDuelViewModel @Inject constructor(
    private val random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MathDuelUiState())
    val uiState: StateFlow<MathDuelUiState> = _uiState.asStateFlow()

    fun startRound(operations: Set<MathOperation>) {
        if (_uiState.value.question != null) return
        _uiState.update {
            it.copy(question = randomMathQuestion(operations = operations, random = random))
        }
    }

    fun onAnswer(player: Player, answerIndex: Int) {
        val state = _uiState.value
        val question = state.question ?: return
        val answer = question.answers.getOrNull(answerIndex) ?: return

        _uiState.update {
            it.copy(duel = it.duel.answer(player, isCorrect = answer == question.correctAnswer))
        }
    }
}
