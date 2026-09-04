package play.pmu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import play.pmu.R
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.ui.PmuTestTags
import play.pmu.ui.theme.PmuSpacing

@Composable
fun RoundResultView(
    outcome: RoundOutcome,
    modifier: Modifier = Modifier,
    panelExtras: @Composable ColumnScope.() -> Unit = {},
) {
    val panel: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(PmuSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
        ) {
            Text(
                text = if (outcome.winner == Winner.DRAW) {
                    stringResource(R.string.round_draw)
                } else {
                    stringResource(R.string.round_winner, winnerName(outcome.winner))
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            // Detalj je opciona dopuna ("142 ms", "8 tapkanja"). Kada ga igra ne
            // posalje, red se prosto ne prikazuje - zato je detailRes nullable.
            val detailRes = outcome.detailRes
            if (detailRes != null) {
                Text(
                    text = if (outcome.detailValue != null) {
                        stringResource(detailRes, outcome.detailValue)
                    } else {
                        stringResource(detailRes)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }

            panelExtras()
        }
    }

    WinnerReveal(
        winner = outcome.winner,
        modifier = modifier.testTag(PmuTestTags.ROUND_RESULT),
    ) {
        TwoPlayerLayout(
            topContent = { panel() },
            bottomContent = { panel() },
        )
    }
}
