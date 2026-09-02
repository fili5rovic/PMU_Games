package play.pmu.ui.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameStats
import play.pmu.ui.components.EmptyView
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.bestScoreText
import play.pmu.ui.components.scoreText
import java.text.DateFormat
import java.util.Date

@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.statistics_title),
                onNavigateBack = onNavigateBack,
            ) {
                if (uiState.history.isNotEmpty()) {
                    IconButton(onClick = viewModel::clearHistory) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.statistics_clear),
                        )
                    }
                }
            }
        },
    ) { padding ->
        if (uiState.history.isEmpty()) {
            EmptyView(
                message = stringResource(R.string.statistics_empty),
                modifier = Modifier.padding(padding),
            )
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // LazyRow: kartice po igri se skroluju vodoravno, pa ne zauzimaju ceo ekran.
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.perGame) { stats -> GameStatsCard(stats) }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.statistics_history),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(uiState.history, key = { it.id }) { result ->
                HistoryRow(result)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun GameStatsCard(stats: GameStats) {
    Card(modifier = Modifier.width(150.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(stats.gameType.titleRes),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${stringResource(R.string.statistics_played)}: ${stats.playCount}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.statistics_best) + ": " +
                    (stats.bestScore?.let { bestScoreText(stats.gameType, it) } ?: "-"),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun HistoryRow(result: GameResultEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = stringResource(result.gameType.titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                // DateFormat sa podrazumevanim lokalom, pa se datum prikazuje
                // u formatu jezika telefona.
                text = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(Date(result.playedAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = scoreText(result), style = MaterialTheme.typography.bodyLarge)
    }
}
