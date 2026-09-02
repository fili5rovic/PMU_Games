package play.pmu.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import play.pmu.R
import play.pmu.domain.model.GameType
import play.pmu.ui.components.GameCard
import play.pmu.ui.components.PmuTopAppBar

/**
 * Pocetni ekran - launcher igara.
 *
 * Nema ViewModel jer nema state-a ni logike: lista igara je [GameType.entries],
 * a klik samo poziva lambdu koju je dao NavHost. Pravljenje ViewModel-a ovde
 * bilo bi prazan sloj.
 */
@Composable
fun HomeScreen(
    onGameClick: (GameType) -> Unit,
    onStatisticsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            PmuTopAppBar(title = stringResource(R.string.app_name)) {
                IconButton(onClick = onStatisticsClick) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.ic_statistics),
                        contentDescription = stringResource(R.string.action_statistics),
                    )
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.action_settings),
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
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
            items(GameType.entries) { gameType ->
                GameCard(gameType = gameType, onClick = { onGameClick(gameType) })
            }
        }
    }
}
