package play.pmu.ui.memory

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.PlayerAreaLabel
import play.pmu.ui.components.SharedBoardLayout
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.accentColor
import play.pmu.ui.theme.areaColor

@Composable
fun MemoryGame(
    startingPlayer: Player,
    onFinished: (RoundOutcome) -> Unit,
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.startRound(startingPlayer) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = winner,
                    detailRes = R.string.outcome_pairs,
                    detailValue = maxOf(uiState.scoreOne, uiState.scoreTwo),
                )
            )
        }
    }

    val isOneActive = uiState.winner == null && uiState.currentPlayer == Player.ONE
    val isTwoActive = uiState.winner == null && uiState.currentPlayer == Player.TWO

    SharedBoardLayout(
        topPanel = { ScorePanel(Player.TWO, uiState, isTwoActive) },
        bottomPanel = { ScorePanel(Player.ONE, uiState, isOneActive) },
        topPanelModifier = playerAreaModifier(Player.TWO, isTwoActive),
        bottomPanelModifier = playerAreaModifier(Player.ONE, isOneActive),
        centerContent = {
            CardGrid(
                cards = uiState.cards,
                onCardClick = viewModel::onCardClick,
                modifier = Modifier.padding(PmuSpacing.medium),
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
private fun ScorePanel(player: Player, uiState: MemoryUiState, isActive: Boolean) {
    Column(
        modifier = Modifier.padding(PmuSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        PlayerAreaLabel(player = player, isActive = isActive)
        Text(
            text = uiState.scoreOf(player).toString(),
            style = MaterialTheme.typography.displaySmall,
            color = player.accentColor,
        )
    }
}

@Composable
private fun CardGrid(
    cards: List<MemoryCard>,
    onCardClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        cards.chunked(GRID_COLUMNS).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
            ) {
                row.forEachIndexed { columnIndex, card ->
                    MemoryCardItem(
                        card = card,
                        onClick = { onCardClick(card.id) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag(
                                PmuTestTags.memoryCard(rowIndex * GRID_COLUMNS + columnIndex)
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryCardItem(
    card: MemoryCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFaceUp = card.isRevealed || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = FLIP_DURATION_MILLIS),
        label = "cardFlip",
    )
    val showFront = rotation > 90f
    val hiddenDescription = stringResource(R.string.cd_card_hidden)

    Card(
        onClick = onClick,
        enabled = !card.isMatched,
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = CAMERA_DISTANCE
            }
            .semantics { if (!showFront) contentDescription = hiddenDescription },
        colors = CardDefaults.cardColors(
            containerColor = if (showFront) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (showFront) card.symbol else "?",
                style = MaterialTheme.typography.headlineSmall,
                // Prednja strana se dodatno okrece da simbol ne bude u zrcalu.
                modifier = Modifier.graphicsLayer { rotationY = if (showFront) 180f else 0f },
            )
        }
    }
}

private const val GRID_COLUMNS = 4
private const val FLIP_DURATION_MILLIS = 350
private const val CAMERA_DISTANCE = 12f
private const val WINNER_DELAY_MILLIS = 900L
private const val TURN_FADE_MILLIS = 260
