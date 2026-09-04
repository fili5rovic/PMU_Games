package play.pmu.ui.binary

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import play.pmu.domain.game.AnswerDuel
import play.pmu.domain.game.BinaryQuestion
import play.pmu.domain.game.randomBinaryQuestion
import play.pmu.domain.model.Player
import javax.inject.Inject
import kotlin.random.Random

data class BinaryDuelUiState(
    val question: BinaryQuestion,
    val duel: AnswerDuel = AnswerDuel(),
)

@HiltViewModel
class BinaryDuelViewModel @Inject constructor(
    random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BinaryDuelUiState(question = randomBinaryQuestion(random)))
    val uiState: StateFlow<BinaryDuelUiState> = _uiState.asStateFlow()

    fun onAnswer(player: Player, answerIndex: Int) {
        val state = _uiState.value
        val answer = state.question.answers.getOrNull(answerIndex) ?: return

        _uiState.update {
            it.copy(
                duel = it.duel.answer(player, isCorrect = answer == it.question.correctAnswer)
            )
        }
    }
}
