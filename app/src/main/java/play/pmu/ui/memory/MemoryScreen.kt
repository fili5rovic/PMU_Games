package play.pmu.ui.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge

@Composable
fun MemoryScreen(
    onNavigateBack: () -> Unit,
    onGameFinished: (Long) -> Unit,
    viewModel: MemoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.finishedResultId) {
        uiState.finishedResultId?.let(onGameFinished)
    }

    MemoryContent(
        uiState = uiState,
        onCardClick = viewModel::onCardClick,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun MemoryContent(
    uiState: MemoryUiState,
    onCardClick: (Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_memory_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ScoreBadge(stringResource(R.string.memory_moves, uiState.moves))
                ScoreBadge(
                    stringResource(
                        R.string.memory_pairs,
                        uiState.matchedPairs,
                        uiState.totalPairs,
                    )
                )
            }

            // LazyVerticalGrid sa fiksne 4 kolone daje mrezu 4x4.
            LazyVerticalGrid(
                columns = GridCells.Fixed(GRID_COLUMNS),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(uiState.cards, key = { it.id }) { card ->
                    MemoryCardItem(card = card, onClick = { onCardClick(card.id) })
                }
            }
        }
    }
}

/**
 * Kartica se "obrce" animacijom rotacije oko Y ose. Simbol se prikazuje samo
 * kada je kartica presla pola obrtaja, pa se sadrzaj ne vidi kroz "zadnju stranu".
 */
@Composable
private fun MemoryCardItem(
    card: MemoryCard,
    onClick: () -> Unit,
) {
    val isFaceUp = card.isRevealed || card.isMatched
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(durationMillis = FLIP_DURATION_MILLIS),
        label = "cardFlip",
    )
    val showFront = rotation > 90f

    Card(
        onClick = onClick,
        enabled = !card.isMatched,
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = CAMERA_DISTANCE
            },
        colors = CardDefaults.cardColors(
            containerColor = if (showFront) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
        ),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (showFront) card.symbol else "?",
                style = MaterialTheme.typography.headlineMedium,
                // Prednja strana se dodatno okrece da tekst ne bude u zrcalu.
                modifier = Modifier.graphicsLayer { rotationY = if (showFront) 180f else 0f },
            )
        }
    }
}

private const val GRID_COLUMNS = 4
private const val FLIP_DURATION_MILLIS = 350
private const val CAMERA_DISTANCE = 12f
