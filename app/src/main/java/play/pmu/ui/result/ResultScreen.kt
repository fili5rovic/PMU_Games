package play.pmu.ui.result

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType
import play.pmu.ui.components.ErrorView
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ResultActions
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.components.scoreText
import play.pmu.ui.theme.CorrectGreenLight
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.WrongRedLight

@Composable
fun ResultScreen(
    onPlayAgain: (GameType) -> Unit,
    onHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.result_title),
                onNavigateBack = onHome,
            )
        },
    ) { padding ->
        val result = uiState.result
        when {
            uiState.isLoading -> LoadingView(
                message = stringResource(R.string.quiz_loading),
                modifier = Modifier.padding(padding),
            )

            result == null -> ErrorView(
                message = stringResource(R.string.result_missing),
                modifier = Modifier.padding(padding),
            )

            else -> ResultContent(
                result = result,
                isPersonalBest = uiState.isPersonalBest,
                contentPadding = padding,
                onPlayAgain = { onPlayAgain(result.gameType) },
                onHome = onHome,
            )
        }
    }
}

@Composable
private fun ResultContent(
    result: GameResultEntity,
    isPersonalBest: Boolean,
    contentPadding: PaddingValues,
    onPlayAgain: () -> Unit,
    onHome: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                text = stringResource(result.gameType.titleRes),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Text(
                text = scoreText(result),
                style = MaterialTheme.typography.displaySmall,
            )
        }
        item {
            AnimatedVisibility(
                visible = isPersonalBest,
                enter = scaleIn(animationSpec = tween(durationMillis = 400)),
            ) {
                ScoreBadge(stringResource(R.string.result_new_best))
            }
        }

        if (result.correctItems.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.result_guessed), result.correctItems.size) }
            items(result.correctItems) { word ->
                WordRow(word = word, color = CorrectGreenLight)
            }
        }
        if (result.skippedItems.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.result_skipped), result.skippedItems.size) }
            items(result.skippedItems) { word ->
                WordRow(word = word, color = WrongRedLight)
            }
        }

        item {
            ResultActions(
                primaryLabel = stringResource(R.string.result_play_again),
                onPrimary = onPlayAgain,
                homeLabel = stringResource(R.string.result_home),
                onHome = onHome,
                modifier = Modifier.padding(top = PmuSpacing.medium),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(text = count.toString(), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun WordRow(word: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text = word,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        modifier = Modifier.fillMaxWidth(),
    )
}
