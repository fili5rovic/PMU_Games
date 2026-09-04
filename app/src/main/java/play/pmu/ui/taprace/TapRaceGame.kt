package play.pmu.ui.taprace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.accentColor
import play.pmu.ui.theme.areaColor

@Composable
fun TapRaceGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: TapRaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val winner = uiState.winner

    LaunchedEffect(winner) {
        if (winner != null) {
            delay(WINNER_DELAY_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = winner,
                    detailRes = R.string.outcome_taps,
                    detailValue = maxOf(uiState.tapsOne, uiState.tapsTwo),
                )
            )
        }
    }

    TwoPlayerLayout(
        topContent = { TapRaceHalf(Player.TWO, uiState) },
        bottomContent = { TapRaceHalf(Player.ONE, uiState) },
        topModifier = tapHalfModifier(Player.TWO, viewModel::onTap),
        bottomModifier = tapHalfModifier(Player.ONE, viewModel::onTap),
    )
}

@Composable
private fun tapHalfModifier(player: Player, onTap: (Player) -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return Modifier
        .fillMaxSize()
        .background(player.areaColor())
        .testTag(PmuTestTags.playerArea(player.name))
        .clickable(
            interactionSource = interactionSource,
            indication = null,
        ) { onTap(player) }
}

@Composable
private fun TapRaceHalf(player: Player, uiState: TapRaceUiState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
        modifier = Modifier.padding(PmuSpacing.large),
    ) {
        Text(
            text = uiState.tapsOf(player).toString(),
            style = MaterialTheme.typography.displayLarge,
            color = player.accentColor,
        )
        Text(
            text = uiState.secondsLeft.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private const val WINNER_DELAY_MILLIS = 600L
