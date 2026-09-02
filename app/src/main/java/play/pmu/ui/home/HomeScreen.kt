package play.pmu.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import play.pmu.R
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MiniGame
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.GameCard
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.theme.PmuSpacing

/**
 * Pocetni ekran - launcher igara.
 *
 * Nema ViewModel jer nema state-a ni logike: liste igara su [MiniGame.entries] i
 * [GameType.entries], a klik samo poziva lambdu koju je dao NavHost. Pravljenje
 * ViewModel-a ovde bilo bi prazan sloj.
 *
 * Glavna akcija aplikacije je partija, pa je njena kartica prva i istaknuta.
 */
@Composable
fun HomeScreen(
    onPartyClick: () -> Unit,
    onMiniGameClick: (MiniGame) -> Unit,
    onGameClick: (GameType) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            PmuTopAppBar(title = stringResource(R.string.app_name)) {
                IconButton(
                    onClick = onStatisticsClick,
                    modifier = Modifier.testTag(PmuTestTags.STATISTICS),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_statistics),
                        contentDescription = stringResource(R.string.action_statistics),
                    )
                }
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag(PmuTestTags.SETTINGS),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.action_settings),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().testTag(PmuTestTags.HOME_LIST),
            contentPadding = PaddingValues(
                start = PmuSpacing.medium,
                end = PmuSpacing.medium,
                top = padding.calculateTopPadding() + PmuSpacing.small,
                bottom = padding.calculateBottomPadding() + PmuSpacing.medium,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                GameCard(
                    title = stringResource(R.string.home_party_title),
                    description = stringResource(R.string.home_party_desc),
                    iconRes = R.drawable.ic_party,
                    onClick = onPartyClick,
                    isPrimary = true,
                    modifier = Modifier.testTag(PmuTestTags.START_PARTY),
                )
            }

            item { SectionTitle(stringResource(R.string.home_section_duels)) }
            items(MiniGame.entries) { miniGame ->
                GameCard(
                    title = stringResource(miniGame.titleRes),
                    description = stringResource(miniGame.instructionRes),
                    iconRes = miniGame.iconRes,
                    onClick = { onMiniGameClick(miniGame) },
                    modifier = Modifier.testTag(PmuTestTags.miniGame(miniGame.name)),
                )
            }

            item { SectionTitle(stringResource(R.string.home_section_others)) }
            items(GameType.entries) { gameType ->
                GameCard(
                    title = stringResource(gameType.titleRes),
                    description = stringResource(gameType.descriptionRes),
                    iconRes = gameType.iconRes,
                    onClick = { onGameClick(gameType) },
                    modifier = Modifier.testTag(PmuTestTags.gameType(gameType.name)),
                )
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
        modifier = Modifier.padding(top = PmuSpacing.small),
    )
}
