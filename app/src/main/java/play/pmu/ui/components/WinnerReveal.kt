package play.pmu.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import play.pmu.ui.theme.areaColor

@Composable
fun WinnerReveal(
    winner: Winner,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val winningPlayer = when (winner) {
        Winner.PLAYER_ONE -> Player.ONE
        Winner.PLAYER_TWO -> Player.TWO
        Winner.DRAW -> null
    }

    var isRevealed by remember(winner) { mutableStateOf(false) }
    LaunchedEffect(winner) { isRevealed = true }

    val coverage by animateFloatAsState(
        targetValue = if (isRevealed) 1f else 0f,
        animationSpec = tween(durationMillis = REVEAL_MILLIS, easing = FastOutSlowInEasing),
        label = "winnerCoverage",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (winningPlayer == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(coverage)
                    .align(
                        if (winningPlayer == Player.ONE) {
                            Alignment.BottomCenter
                        } else {
                            Alignment.TopCenter
                        }
                    )
                    .background(winningPlayer.areaColor())
            )
        }

        content()
    }
}

private const val REVEAL_MILLIS = 450
