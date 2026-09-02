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

data class BinaryDuelUiState(
    val question: BinaryQuestion,
    val duel: AnswerDuel = AnswerDuel(),
)

/**
 * Binarno u decimalno: oba igraca vide isti binarni broj i po cetiri decimalna
 * odgovora. Prvi tacan odgovor osvaja rundu, a promasaj iskljucuje igraca do
 * kraja runde - isto pravilo kao u racunskom duelu, jer im je zajednicko u
 * [AnswerDuel].
 *
 * Igra nema podesavanja, pa se pitanje pravi odmah u `init`; kako je svaka runda
 * svoja destinacija sa svojim ViewModel-om, svaka runda dobija novo pitanje.
 */
@HiltViewModel
class BinaryDuelViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(BinaryDuelUiState(question = randomBinaryQuestion()))
    val uiState: StateFlow<BinaryDuelUiState> = _uiState.asStateFlow()

    /** [answerIndex] je mesto tapnutog odgovora u [BinaryQuestion.answers]. */
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
