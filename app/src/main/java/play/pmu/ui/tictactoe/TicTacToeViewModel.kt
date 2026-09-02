package play.pmu.ui.tictactoe

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import play.pmu.domain.game.Mark
import play.pmu.domain.game.TicTacToeBoard
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import javax.inject.Inject

data class TicTacToeUiState(
    /** null dok runda ne dobije velicinu table i pocetnog igraca. */
    val board: TicTacToeBoard? = null,
    val currentPlayer: Player = Player.ONE,
    /** null dok partija traje. */
    val winner: Winner? = null,
)

/**
 * Iks-oks za dva igraca na jednoj tabli.
 *
 * Sva pravila su u [TicTacToeBoard] - klasi bez ijedne Android zavisnosti, koja
 * radi za tablu bilo koje velicine i testira se obicnim JUnit testom. Ovaj
 * ViewModel samo drzi trenutnu tablu i naizmenicno menja igraca.
 *
 * Tabla je zajednicka i lezi u sredini ekrana, kao na stolu, pa aplikacija ne
 * moze da zna CIJI je prst tapnuo polje. Zato se red poteza ne "brani" nego
 * SPROVODI: svaki potez upisuje znak igraca koji je trenutno na redu. Tapkanje
 * van svog reda moze samo da odigra potez ZA protivnika - isto kao da ste na
 * pravoj tabli posegli preko stola.
 */
@HiltViewModel
class TicTacToeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeUiState())
    val uiState: StateFlow<TicTacToeUiState> = _uiState.asStateFlow()

    /**
     * Postavlja rundu: velicinu table i igraca koji pocinje.
     *
     * [boardSizeOption] je ono sto su igraci izabrali u podesavanjima, pa se
     * "Slucajno" izvlaci TEK OVDE. Posto je svaka runda svoja destinacija sa
     * svojim ViewModel-om, tri pojavljivanja iks-oksa u istoj partiji dobiju
     * tri nezavisno izvucene velicine.
     *
     * Pocetnog igraca odredjuje raspored partije (prvo pojavljivanje slucajno,
     * svako sledece obrnuto), pa ga ova klasa samo prima.
     */
    fun startRound(boardSizeOption: BoardSizeOption, startingPlayer: Player) {
        if (_uiState.value.board != null) return
        _uiState.value = TicTacToeUiState(
            board = TicTacToeBoard(size = boardSizeOption.resolve()),
            currentPlayer = startingPlayer,
        )
    }

    fun onCellClick(index: Int) {
        val state = _uiState.value
        val currentBoard = state.board ?: return
        if (state.winner != null) return

        // place vraca null za nemoguc potez (zauzeto polje), pa se klik ignorise.
        val board = currentBoard.place(index, state.currentPlayer.mark) ?: return
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
