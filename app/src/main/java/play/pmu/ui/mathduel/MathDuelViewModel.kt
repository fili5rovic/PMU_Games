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
    /** null dok runda ne dobije podesavanja (vidi [MathDuelViewModel.startRound]). */
    val question: MathQuestion? = null,
    val duel: AnswerDuel = AnswerDuel(),
)

/**
 * Racunski duel: oba igraca vide ISTO pitanje, svaki na svojoj polovini ekrana i
 * u svom smeru. Prvi tacan odgovor osvaja rundu.
 *
 * Pitanje se pravi u [startRound], a ne u `init`, jer zavisi od podesavanja
 * (koje su operacije ukljucene) koja ekran prosledjuje. Posto je svaka runda
 * svoja destinacija sa svojim ViewModel-om, poziv se desava tacno jednom po
 * rundi - a straza na `question != null` pokriva i ponovni ulazak u kompoziciju.
 *
 * Pravila duela su u [AnswerDuel], zajednicka sa igrom binarno-u-decimalno.
 */
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

    /** [answerIndex] je mesto tapnutog odgovora u [MathQuestion.answers]. */
    fun onAnswer(player: Player, answerIndex: Int) {
        val state = _uiState.value
        val question = state.question ?: return
        val answer = question.answers.getOrNull(answerIndex) ?: return

        _uiState.update {
            it.copy(duel = it.duel.answer(player, isCorrect = answer == question.correctAnswer))
        }
    }
}
