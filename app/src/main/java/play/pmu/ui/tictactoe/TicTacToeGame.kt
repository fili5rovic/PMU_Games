package play.pmu.ui.tictactoe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.domain.game.Mark
import play.pmu.domain.game.TicTacToeBoard
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.PlayerAreaLabel
import play.pmu.ui.components.SharedBoardLayout
import play.pmu.ui.theme.CorrectGreenLight
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.accentColor
import play.pmu.ui.theme.areaColor

@Composable
fun TicTacToeGame(
    boardSizeOption: BoardSizeOption,
    startingPlayer: Player,
    onFinished: (RoundOutcome) -> Unit,
    viewModel: TicTacToeViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.startRound(boardSizeOption, startingPlayer) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val board = uiState.board ?: return
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(RoundOutcome(winner = winner))
        }
    }

    val isOneActive = winner == null && uiState.currentPlayer == Player.ONE
    val isTwoActive = winner == null && uiState.currentPlayer == Player.TWO

    SharedBoardLayout(
        topPanel = { PlayerAreaLabel(player = Player.TWO, isActive = isTwoActive) },
        bottomPanel = { PlayerAreaLabel(player = Player.ONE, isActive = isOneActive) },
        topPanelModifier = playerAreaModifier(Player.TWO, isTwoActive),
        bottomPanelModifier = playerAreaModifier(Player.ONE, isOneActive),
        centerContent = {
            Board(
                board = board,
                onCellClick = viewModel::onCellClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(PmuSpacing.medium)
                    .aspectRatio(1f),
            )
        },
    )
}

@Composable
private fun playerAreaModifier(player: Player, isActive: Boolean): Modifier {
    val background by animateColorAsState(
        targetValue = player.areaColor(isActive),
        animationSpec = tween(durationMillis = TURN_FADE_MILLIS),
        label = "playerArea",
    )
    return Modifier
        .fillMaxSize()
        .background(background)
        .testTag(PmuTestTags.playerArea(player.name))
}

@Composable
private fun Board(
    board: TicTacToeBoard,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val winningLine = board.winningLine
    val markStyle = markStyleFor(board.size)
    val spacing = if (board.size >= LARGE_BOARD_SIZE) SMALL_CELL_SPACING else PmuSpacing.small

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        board.cells.chunked(board.size).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                row.forEachIndexed { columnIndex, mark ->
                    val index = rowIndex * board.size + columnIndex
                    Cell(
                        mark = mark,
                        markStyle = markStyle,
                        isWinning = winningLine?.contains(index) == true,
                        onClick = { onCellClick(index) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag(PmuTestTags.ticTacToeCell(rowIndex, columnIndex)),
                    )
                }
            }
        }
    }
}

@Composable
private fun markStyleFor(size: Int): TextStyle = when (size) {
    3 -> MaterialTheme.typography.displayMedium
    4 -> MaterialTheme.typography.displaySmall
    else -> MaterialTheme.typography.headlineSmall
}

@Composable
private fun Cell(
    mark: Mark?,
    markStyle: TextStyle,
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
        enabled = mark == null,
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = mark,
                transitionSpec = {
                    (fadeIn(tween(120)) + scaleIn(initialScale = 0.5f)) togetherWith fadeOut(tween(90))
                },
                label = "cellMark",
            ) { value ->
                Text(
                    text = value?.name.orEmpty(),
                    style = markStyle,
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
private const val TURN_FADE_MILLIS = 260
private const val LARGE_BOARD_SIZE = 5
private val SMALL_CELL_SPACING = 4.dp
