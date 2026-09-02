package play.pmu.ui.tictactoe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.game.Mark
import play.pmu.domain.game.TicTacToeBoard
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.PlayerBadge
import play.pmu.ui.components.SharedBoardLayout
import play.pmu.ui.theme.CorrectGreenLight
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.accentColor

/**
 * Iks-oks. Tabla je zajednicka i stoji u sredini, a svaki igrac na svom kraju
 * ekrana vidi (uspravno) cij je potez - zato se koristi [SharedBoardLayout], a
 * ne podeljeni ekran na pola.
 */
@Composable
fun TicTacToeGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: TicTacToeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            // Kratka pauza da igraci vide pobednicku liniju na tabli.
            delay(WINNER_DELAY_MILLIS)
            onFinished(RoundOutcome(winner = winner))
        }
    }

    SharedBoardLayout(
        topPanel = { TurnPanel(Player.TWO, uiState) },
        bottomPanel = { TurnPanel(Player.ONE, uiState) },
        centerContent = {
            Board(
                board = uiState.board,
                onCellClick = viewModel::onCellClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PmuSpacing.medium)
                    .aspectRatio(1f),
            )
        },
    )
}

/** Cij je potez - prikazuje se na oba kraja telefona, svakom igracu uspravno. */
@Composable
private fun TurnPanel(player: Player, uiState: TicTacToeUiState) {
    val isMyTurn = uiState.winner == null && uiState.currentPlayer == player

    Column(
        modifier = Modifier.fillMaxWidth().padding(PmuSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        PlayerBadge(player = player, isActive = isMyTurn)
        Text(
            text = stringResource(
                if (isMyTurn) R.string.tictactoe_your_turn else R.string.tictactoe_other_turn
            ),
            style = MaterialTheme.typography.titleSmall,
            color = if (isMyTurn) {
                player.accentColor
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun Board(
    board: TicTacToeBoard,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val winningLine = board.winningLine

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        // chunked deli listu od devet polja na tri reda po tri.
        board.cells.chunked(TicTacToeBoard.SIZE).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
            ) {
                row.forEachIndexed { columnIndex, mark ->
                    val index = rowIndex * TicTacToeBoard.SIZE + columnIndex
                    Cell(
                        mark = mark,
                        isWinning = winningLine?.contains(index) == true,
                        onClick = { onCellClick(index) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Cell(
    mark: Mark?,
    isWinning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isWinning) CorrectGreenLight else MaterialTheme.colorScheme.surfaceVariant,
        label = "cellColor",
    )

    Surface(
        onClick = onClick,
        // Zauzeto polje se ne moze ponovo tapnuti; ViewModel to i sam odbija,
        // ali ugaseno dugme je jasnije igracu.
        enabled = mark == null,
        color = containerColor,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Novi znak "uskace" - AnimatedContent animira prelaz sa praznog polja.
            AnimatedContent(
                targetState = mark,
                transitionSpec = {
                    (fadeIn(tween(120)) + scaleIn(initialScale = 0.5f)) togetherWith fadeOut(tween(90))
                },
                label = "cellMark",
            ) { value ->
                Text(
                    text = value?.name.orEmpty(),
                    style = MaterialTheme.typography.displayMedium,
                    color = when (value) {
                        Mark.X -> Player.ONE.accentColor
                        Mark.O -> Player.TWO.accentColor
                        null -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

private const val WINNER_DELAY_MILLIS = 900L
