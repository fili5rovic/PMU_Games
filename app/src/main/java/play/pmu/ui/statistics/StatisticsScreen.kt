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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.data.local.GameResultEntity
import play.pmu.data.local.MatchEntity
import play.pmu.data.local.MiniGameStats
import play.pmu.data.local.RoundResultEntity
import play.pmu.data.repository.GameStats
import play.pmu.domain.model.Winner
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.EmptyView
import play.pmu.ui.components.PartyScore
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.scoreText
import play.pmu.ui.components.winnerColor
import play.pmu.ui.components.winnerName
import play.pmu.ui.theme.PmuSpacing
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
                if (!uiState.isEmpty) {
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
        if (uiState.isEmpty) {
            EmptyView(
                message = stringResource(R.string.statistics_empty),
                modifier = Modifier
                    .padding(padding)
                    .testTag(PmuTestTags.STATISTICS_SCREEN),
            )
            return@Scaffold
        }

        // JEDAN LazyColumn prikazuje vise razlicitih tabela, pa kljucevi moraju da
        // budu jedinstveni u celoj listi - a ne samo unutar svoje sekcije.
        // Svaka tabela ima svoj autoincrement, tako da partija i rezultat
        // pantomime lako dobiju isti id (oba pocinju od 1). Bez prefiksa
        // Compose tada baca "Key 1 was already used" i ekran pukne.
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag(PmuTestTags.STATISTICS_SCREEN),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + PmuSpacing.small,
                bottom = padding.calculateBottomPadding() + PmuSpacing.medium,
            ),
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
        ) {
            // Istorija partija je glavni deo aplikacije, pa stoji prva.
            if (uiState.matches.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.party_history)) }
                items(uiState.matches, key = { "match-${it.id}" }) { match ->
                    MatchRow(match)
                }
            }

            if (uiState.miniGames.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.statistics_mini_games)) }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = PmuSpacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
                    ) {
                        items(uiState.miniGames, key = { it.game.name }) { stats ->
                            MiniGameStatsCard(stats)
                        }
                    }
                }
            }

            if (uiState.rounds.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.statistics_recent_rounds)) }
                items(uiState.rounds, key = { "round-${it.id}" }) { round ->
                    RoundRow(round)
                }
            }

            if (uiState.history.isNotEmpty()) {
                // LazyRow: kartice po igri se skroluju vodoravno, pa ne zauzimaju ceo ekran.
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = PmuSpacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
                    ) {
                        items(uiState.perGame) { stats -> GameStatsCard(stats) }
                    }
                }
                item { SectionTitle(stringResource(R.string.statistics_history)) }
                items(uiState.history, key = { "result-${it.id}" }) { result ->
                    HistoryRow(result)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = PmuSpacing.medium, vertical = PmuSpacing.small),
    )
}

/** Jedna odigrana partija: datum, rezultat i pobednik. */
@Composable
private fun MatchRow(match: MatchEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PmuSpacing.medium, vertical = PmuSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = if (match.winner == Winner.DRAW) {
                    stringResource(R.string.party_draw)
                } else {
                    stringResource(R.string.party_winner, winnerName(match.winner))
                },
                style = MaterialTheme.typography.bodyLarge,
                color = winnerColor(match.winner),
            )
            Text(
                text = formatDateTime(match.playedAt) + " - " +
                    stringResource(R.string.party_history_rounds, match.gamesPlayed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(R.string.party_score, match.scoreOne, match.scoreTwo),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/**
 * Sazetak jedne mini igre: koliko je rundi odigrano i kako su podeljene pobede.
 *
 * Prikazuje sve sto je u bazi, pa nova mini igra ovde osvane sama - nema
 * `when` po tipu igre koji bi morao da se dopunjava.
 */
@Composable
private fun MiniGameStatsCard(stats: MiniGameStats) {
    Card(modifier = Modifier.width(160.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(stats.game.titleRes),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "${stringResource(R.string.statistics_played)}: ${stats.played}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PartyScore(scoreOne = stats.winsOne, scoreTwo = stats.winsTwo)
        }
    }
}

/** Jedna odigrana runda mini igre: naziv igre, pobednik i vreme. */
@Composable
private fun RoundRow(round: RoundResultEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PmuSpacing.medium, vertical = PmuSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(round.game.titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = formatDateTime(round.playedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = if (round.winner == Winner.DRAW) {
                stringResource(R.string.round_draw)
            } else {
                winnerName(round.winner)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = winnerColor(round.winner),
        )
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
                // Rekord je u obe igre obican broj (pojmovi, odnosno tacni odgovori).
                text = stringResource(R.string.statistics_best) + ": " +
                    (stats.bestScore?.toString() ?: "-"),
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
            .padding(horizontal = PmuSpacing.medium, vertical = PmuSpacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = stringResource(result.gameType.titleRes),
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = formatDateTime(result.playedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = scoreText(result), style = MaterialTheme.typography.bodyLarge)
    }
}

/**
 * DateFormat sa podrazumevanim lokalom, pa se datum prikazuje u formatu jezika
 * telefona.
 */
private fun formatDateTime(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
