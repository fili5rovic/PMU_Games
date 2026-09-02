package play.pmu.ui.party

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.PartyScore
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.RoundResultView
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.components.winnerColor
import play.pmu.ui.components.winnerName
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing

/**
 * Ekrani partije. Sve tri funkcije su bez state-a: primaju [PartyUiState] i
 * lambde, pa ne znaju ni za ViewModel ni za navigaciju (state hoisting). To je
 * ista konvencija koju koristi i ostatak projekta.
 */

/** Pocetak partije: objasnjenje kako se telefon postavlja i dugme za start. */
@Composable
fun PartyStartScreen(
    uiState: PartyUiState,
    onStart: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.party_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(PmuSpacing.large),
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.large, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.party_setup_hint),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            if (uiState.isReady) {
                ScoreBadge(stringResource(R.string.party_rounds_info, uiState.totalRounds))
            }
            Button(
                onClick = onStart,
                // Dok se ne procita broj rundi iz podesavanja, partija ne moze da pocne.
                enabled = uiState.isReady,
                modifier = Modifier.fillMaxWidth().heightIn(min = GameTouchTargetSize),
            ) {
                Text(
                    text = stringResource(R.string.party_start),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

/**
 * Jedna runda partije.
 *
 * Da li je runda odigrana NE cuva se lokalno, nego se cita iz [PartyUiState]
 * (`isRoundOver`). Zato stanje ekrana ne moze da se raziđe sa stanjem partije, i
 * ne treba nikakav `remember`.
 */
@Composable
fun PartyRoundScreen(
    round: Int,
    uiState: PartyUiState,
    onRoundFinished: (RoundOutcome) -> Unit,
    onNextRound: () -> Unit,
    onPartyFinished: () -> Unit,
) {
    val game = uiState.gameAt(round)
    val lastOutcome = uiState.lastOutcome

    if (game == null) {
        LoadingView(message = stringResource(R.string.game_get_ready))
        return
    }

    if (!uiState.isRoundOver(round) || lastOutcome == null) {
        MiniGameRound(
            game = game,
            onFinished = { outcome -> onRoundFinished(outcome) },
            roundLabel = stringResource(R.string.round_of, round + 1, uiState.totalRounds),
        )
        return
    }

    // Runda je odigrana: kratko se prikaze ishod sa obe strane telefona, pa se
    // automatski prelazi na sledecu igru - igraci nista ne moraju da tapnu.
    RoundResultView(outcome = lastOutcome) {
        PartyScore(scoreOne = uiState.scoreOne, scoreTwo = uiState.scoreTwo)
    }

    LaunchedEffect(round) {
        delay(ROUND_RESULT_MILLIS)
        if (round + 1 >= uiState.totalRounds) onPartyFinished() else onNextRound()
    }
}

/** Konacan rezultat partije, citljiv sa oba kraja telefona. */
@Composable
fun PartyResultScreen(
    uiState: PartyUiState,
    onSaveMatch: () -> Unit,
    onNewParty: () -> Unit,
    onHome: () -> Unit,
) {
    // Upis u istoriju je propratni efekat prikaza ekrana, pa ide u LaunchedEffect.
    // Sam ViewModel dodatno pazi da se partija ne upise dva puta.
    LaunchedEffect(Unit) { onSaveMatch() }

    val panel: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(PmuSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
        ) {
            Text(
                text = stringResource(R.string.party_over),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (uiState.winner == Winner.DRAW) {
                    stringResource(R.string.party_draw)
                } else {
                    stringResource(R.string.party_winner, winnerName(uiState.winner))
                },
                style = MaterialTheme.typography.displaySmall,
                color = winnerColor(uiState.winner),
                textAlign = TextAlign.Center,
            )
            PartyScore(scoreOne = uiState.scoreOne, scoreTwo = uiState.scoreTwo)
            Column(verticalArrangement = Arrangement.spacedBy(PmuSpacing.small)) {
                Button(onClick = onNewParty) { Text(stringResource(R.string.party_new)) }
                OutlinedButton(onClick = onHome) { Text(stringResource(R.string.result_home)) }
            }
        }
    }

    TwoPlayerLayout(topContent = { panel() }, bottomContent = { panel() })
}

private const val ROUND_RESULT_MILLIS = 1_800L
