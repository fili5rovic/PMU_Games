package play.pmu.ui.tictactoe

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import play.pmu.domain.game.Mark
import play.pmu.domain.game.TicTacToeBoard
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import javax.inject.Inject

data class TicTacToeUiState(
    val board: TicTacToeBoard = TicTacToeBoard(),
    val currentPlayer: Player = Player.ONE,
    /** null dok partija traje. */
    val winner: Winner? = null,
)

/**
 * Iks-oks za dva igraca na jednoj tabli.
 *
 * Sva pravila su u [TicTacToeBoard] - klasi bez ijedne Android zavisnosti, koja
 * se testira obicnim JUnit testom. Ovaj ViewModel samo drzi trenutnu tablu i
 * naizmenicno menja igraca, pa je i sam kratak i lak za citanje.
 *
 * Tabla je zajednicka i lezi u sredini ekrana, kao na stolu, pa aplikacija ne
 * moze da zna CIJI je prst tapnuo polje. Zato se red poteza ne "brani" nego
 * ENFORSIRA: svaki potez upisuje znak igraca koji je trenutno na redu. Tapkanje
 * van svog reda moze samo da odigra potez ZA protivnika - isto kao da ste na
 * pravoj tabli posegli preko stola.
 */
@HiltViewModel
class TicTacToeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeUiState())
    val uiState: StateFlow<TicTacToeUiState> = _uiState.asStateFlow()

    fun onCellClick(index: Int) {
        val state = _uiState.value
        if (state.winner != null) return

        // place vraca null za nemoguc potez (zauzeto polje), pa se klik ignorise.
        val board = state.board.place(index, state.currentPlayer.mark) ?: return
        val hasWon = board.winner != null

        _uiState.update {
            it.copy(
                board = board,
                // Igrac se menja samo ako partija nastavlja.
                currentPlayer = if (hasWon || board.isDraw) {
                    it.currentPlayer
                } else {
                    it.currentPlayer.opponent
                },
                winner = when {
                    hasWon -> state.currentPlayer.asWinner
                    board.isDraw -> Winner.DRAW
                    else -> null
                },
            )
        }
    }
}

/** Igrac 1 igra iksevima, igrac 2 kruzicima. */
private val Player.mark: Mark
    get() = if (this == Player.ONE) Mark.X else Mark.O
