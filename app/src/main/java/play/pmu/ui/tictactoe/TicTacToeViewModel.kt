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
import kotlin.random.Random

data class TicTacToeUiState(
    val board: TicTacToeBoard? = null,
    val currentPlayer: Player = Player.ONE,
    val winner: Winner? = null,
)

@HiltViewModel
class TicTacToeViewModel @Inject constructor(
    private val random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeUiState())
    val uiState: StateFlow<TicTacToeUiState> = _uiState.asStateFlow()

    fun startRound(boardSizeOption: BoardSizeOption, startingPlayer: Player) {
        if (_uiState.value.board != null) return
        _uiState.value = TicTacToeUiState(
            board = TicTacToeBoard(size = boardSizeOption.resolve(random)),
            currentPlayer = startingPlayer,
        )
    }

    fun onCellClick(index: Int) {
        val state = _uiState.value
        val currentBoard = state.board ?: return
        if (state.winner != null) return

        val board = currentBoard.place(index, state.currentPlayer.mark) ?: return
        val hasWon = board.winner != null

        _uiState.update {
            it.copy(
                board = board,
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

private val Player.mark: Mark
    get() = if (this == Player.ONE) Mark.X else Mark.O
