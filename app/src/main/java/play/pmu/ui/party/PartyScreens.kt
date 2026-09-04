package play.pmu.ui.party

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.PartyScore
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.ResultActions
import play.pmu.ui.components.RoundResultView
import play.pmu.ui.components.WinnerReveal
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.components.winnerName
import play.pmu.ui.theme.PmuSpacing

@Composable
fun PartyRoundScreen(
    round: Int,
    uiState: PartyUiState,
    onRoundFinished: (RoundOutcome) -> Unit,
    onNextRound: () -> Unit,
    onPartyFinished: () -> Unit,
) {
    val partyRound = uiState.roundAt(round)
    val lastOutcome = uiState.lastOutcome

    if (partyRound == null) {
        LoadingView(message = stringResource(R.string.game_get_ready))
        return
    }

    if (!uiState.isRoundOver(round) || lastOutcome == null) {
        MiniGameRound(
            round = partyRound,
            gameSettings = uiState.gameSettings,
            onFinished = { outcome -> onRoundFinished(outcome) },
            roundLabel = stringResource(R.string.round_of, round + 1, uiState.totalRounds),
        )
        return
    }

    RoundResultView(outcome = lastOutcome) {
        PartyScore(scoreOne = uiState.scoreOne, scoreTwo = uiState.scoreTwo)
    }

    LaunchedEffect(round) {
        delay(ROUND_RESULT_MILLIS)
        if (round + 1 >= uiState.totalRounds) onPartyFinished() else onNextRound()
    }
}

@Composable
fun PartyResultScreen(
    uiState: PartyUiState,
    onSaveMatch: () -> Unit,
    onNewParty: () -> Unit,
    onHome: () -> Unit,
) {
    LaunchedEffect(Unit) { onSaveMatch() }

    val panel: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(PmuSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
        ) {
            Text(
                text = if (uiState.winner == Winner.DRAW) {
                    stringResource(R.string.party_draw)
                } else {
                    stringResource(R.string.party_winner, winnerName(uiState.winner))
                },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            PartyScore(scoreOne = uiState.scoreOne, scoreTwo = uiState.scoreTwo)
            ResultActions(
                primaryLabel = stringResource(R.string.party_new),
                onPrimary = onNewParty,
                homeLabel = stringResource(R.string.result_home),
                onHome = onHome,
            )
        }
    }

    WinnerReveal(
        winner = uiState.winner,
        modifier = Modifier.testTag(PmuTestTags.PARTY_RESULT),
    ) {
        TwoPlayerLayout(topContent = { panel() }, bottomContent = { panel() })
    }
}

private const val ROUND_RESULT_MILLIS = 1_800L
