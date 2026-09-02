package play.pmu.ui.mathduel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import play.pmu.domain.game.MathQuestion
import play.pmu.domain.game.randomMathQuestion
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import javax.inject.Inject

data class MathDuelUiState(
    val question: MathQuestion,
    /** Igraci koji su promasili, pa vise ne mogu da odgovaraju u ovoj rundi. */
    val lockedOut: Set<Player> = emptySet(),
    val winner: Winner? = null,
) {
    fun canAnswer(player: Player): Boolean = winner == null && player !in lockedOut
}

/**
 * Racunski duel: oba igraca vide ISTO pitanje, svaki na svojoj polovini ekrana i
 * u svom smeru. Prvi tacan odgovor osvaja rundu, a pogresan odgovor iskljucuje
 * igraca do kraja runde - pa nagadjanje nije isplativo.
 *
 * Samo generisanje pitanja je cista funkcija [randomMathQuestion] van
 * ViewModel-a, pa se moze testirati bez Android okruzenja.
 */
@HiltViewModel
class MathDuelViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MathDuelUiState(question = randomMathQuestion()))
    val uiState: StateFlow<MathDuelUiState> = _uiState.asStateFlow()

    /**
     * Kao i u duelu refleksa, dva "istovremena" odgovora se izvrsavaju jedan za
     * drugim na glavnoj niti: prvi tacan postavi pobednika, a drugi odmah ispada
     * iz provere `canAnswer`. Zato ne postoji nacin da runda ima dva pobednika.
     */
    fun onAnswer(player: Player, answer: Int) {
        val state = _uiState.value
        if (!state.canAnswer(player)) return

        if (answer == state.question.correctAnswer) {
            _uiState.update { it.copy(winner = player.asWinner) }
            return
        }

        val lockedOut = state.lockedOut + player
        _uiState.update {
            it.copy(
                lockedOut = lockedOut,
                // Ako su oba igraca promasila, runda je neresena.
                winner = if (lockedOut.size == Player.entries.size) Winner.DRAW else null,
            )
        }
    }
}
