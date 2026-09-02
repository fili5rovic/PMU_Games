package play.pmu.ui.reaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.ui.components.TwoPlayerLayout
import play.pmu.ui.theme.CorrectGreen
import play.pmu.ui.theme.GoGreen
import play.pmu.ui.theme.WaitingRed
import play.pmu.ui.theme.WrongRed

/**
 * Duel refleksa: telefon lezi izmedju igraca, svaki ima svoju polovinu ekrana.
 *
 * Igra ne zna nista o partiji - kada je runda resena, prijavi [RoundOutcome] i
 * time je njen posao zavrsen. O prikazu rezultata i o skoru brine onaj ko je
 * pokrenuo igru (partija ili pojedinacna igra).
 */
@Composable
fun ReactionGame(
    onFinished: (RoundOutcome) -> Unit,
    viewModel: ReactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val phase = uiState.phase

    // Kratak zastoj pre prijave: igraci vide da je tap registrovan (polovina
    // pobednika postane zelena) pre nego sto se pojavi ekran rezultata runde.
    LaunchedEffect(phase) {
        if (phase is ReactionPhase.Done) {
            delay(WINNER_FLASH_MILLIS)
            onFinished(
                RoundOutcome(
                    winner = phase.winner.asWinner,
                    detailRes = if (phase.isFalseStart) {
                        R.string.outcome_false_start
                    } else {
                        R.string.outcome_time_ms
                    },
                    detailValue = phase.timeMs,
                )
            )
        }
    }

    TwoPlayerLayout(
        topContent = { ReactionHalf(Player.TWO, phase) },
        bottomContent = { ReactionHalf(Player.ONE, phase) },
        topModifier = reactionHalfModifier(Player.TWO, phase, viewModel::onTap),
        bottomModifier = reactionHalfModifier(Player.ONE, phase, viewModel::onTap),
    )
}

/**
 * Podloga i dodir jedne polovine. Boja je jasan signal i bez citanja teksta:
 * crveno = cekaj, zeleno = tapni, a posle runde zeleno kod pobednika i crveno
 * kod poraženog.
 */
@Composable
private fun reactionHalfModifier(
    player: Player,
    phase: ReactionPhase,
    onTap: (Player) -> Unit,
): Modifier {
    val background by animateColorAsState(
        targetValue = when (phase) {
            ReactionPhase.Waiting -> WaitingRed
            ReactionPhase.Ready -> GoGreen
            is ReactionPhase.Done -> if (phase.winner == player) CorrectGreen else WrongRed
        },
        animationSpec = tween(durationMillis = COLOR_FADE_MILLIS),
        label = "reactionHalfBackground",
    )
    return Modifier
        .fillMaxSize()
        .background(background)
        .clickable { onTap(player) }
}

@Composable
private fun ReactionHalf(player: Player, phase: ReactionPhase) {
    val text = when (phase) {
        ReactionPhase.Waiting -> stringResource(R.string.reaction_wait)
        ReactionPhase.Ready -> stringResource(R.string.reaction_tap_now)
        is ReactionPhase.Done ->
            if (phase.winner == player) stringResource(player.titleRes) else ""
    }

    Text(
        text = text,
        style = if (phase == ReactionPhase.Ready) {
            MaterialTheme.typography.displayLarge
        } else {
            MaterialTheme.typography.headlineSmall
        },
        color = Color.White,
        textAlign = TextAlign.Center,
    )
}

private const val WINNER_FLASH_MILLIS = 700L
private const val COLOR_FADE_MILLIS = 120
