package play.pmu.ui.reaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.theme.GoGreen
import play.pmu.ui.theme.WaitingRed

/**
 * Ekran igre je podeljen na dva dela:
 *  - ova funkcija drzi ViewModel i reaguje na zavrsetak partije (navigacija),
 *  - [ReactionContent] je bez state-a i prima samo vrednosti i lambde.
 * Zato se sadrzaj moze prikazati u @Preview-u bez Hilt-a i baze.
 */
@Composable
fun ReactionScreen(
    onNavigateBack: () -> Unit,
    onRoundFinished: (Long) -> Unit,
    viewModel: ReactionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val phase = uiState.phase

    // Navigacija je propratni efekat promene state-a, pa ide u LaunchedEffect,
    // a ne direktno u telo composable-a.
    LaunchedEffect(phase) {
        if (phase is ReactionPhase.Finished) onRoundFinished(phase.resultId)
    }

    ReactionContent(
        uiState = uiState,
        onTap = viewModel::onTap,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun ReactionContent(
    uiState: ReactionUiState,
    onTap: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isReady = uiState.phase is ReactionPhase.Ready
    // Prelaz crveno -> zeleno je animiran, sto igru cini prijatnijom bez dodatne logike.
    val background by animateColorAsState(
        targetValue = when (uiState.phase) {
            ReactionPhase.Ready -> GoGreen
            ReactionPhase.Waiting -> WaitingRed
            else -> MaterialTheme.colorScheme.surface
        },
        label = "reactionBackground",
    )
    val onBackground = when (uiState.phase) {
        ReactionPhase.Ready, ReactionPhase.Waiting -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_reaction_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(background)
                .clickable(onClick = onTap),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp),
            ) {
                ScoreBadge(
                    text = stringResource(
                        R.string.reaction_round,
                        (uiState.completedRounds + 1).coerceAtMost(uiState.totalRounds),
                        uiState.totalRounds,
                    ),
                )
                Text(
                    text = stringResource(
                        when (uiState.phase) {
                            ReactionPhase.Idle -> R.string.reaction_tap_to_start
                            ReactionPhase.Waiting -> R.string.reaction_wait
                            ReactionPhase.Ready -> R.string.reaction_tap_now
                            ReactionPhase.TooSoon -> R.string.reaction_too_soon
                            is ReactionPhase.Finished -> R.string.result_title
                        }
                    ),
                    style = if (isReady) {
                        MaterialTheme.typography.displaySmall
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    color = onBackground,
                    textAlign = TextAlign.Center,
                )
                uiState.lastTimeMs?.let { last ->
                    Text(
                        text = stringResource(R.string.reaction_last, last),
                        style = MaterialTheme.typography.bodyLarge,
                        color = onBackground,
                    )
                }
            }
        }
    }
}
