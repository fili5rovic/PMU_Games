package play.pmu.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import play.pmu.ui.theme.PmuSpacing

@Composable
fun GameIntro(
    titleRes: Int,
    instructionRes: Int,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    roundLabel: String? = null,
    forBothPlayers: Boolean = true,
) {
    val currentOnStart by rememberUpdatedState(onStart)

    // null = jos se cita uputstvo; 3, 2, 1 = odbrojavanje.
    var countdown by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(titleRes) {
        countdown = null
        delay(INSTRUCTION_MILLIS)
        for (second in COUNTDOWN_FROM downTo 1) {
            countdown = second
            delay(COUNTDOWN_STEP_MILLIS)
        }
        currentOnStart()
    }

    val panel: @Composable () -> Unit = {
        IntroPanel(
            titleRes = titleRes,
            instructionRes = instructionRes,
            roundLabel = roundLabel,
            countdown = countdown,
        )
    }

    if (forBothPlayers) {
        TwoPlayerLayout(
            topContent = { panel() },
            bottomContent = { panel() },
            modifier = modifier,
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            panel()
        }
    }
}

@Composable
private fun IntroPanel(
    titleRes: Int,
    instructionRes: Int,
    roundLabel: String?,
    countdown: Int?,
) {
    Column(
        modifier = Modifier.padding(PmuSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
    ) {
        if (roundLabel != null) ScoreBadge(text = roundLabel)

        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(instructionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Svaki broj odbrojavanja "uskace" svojom animacijom, jer AnimatedContent
        // pri promeni targetState-a animira izlaz starog i ulaz novog sadrzaja.
        AnimatedContent(
            targetState = countdown,
            transitionSpec = {
                (fadeIn(tween(150)) + scaleIn(initialScale = 0.6f)) togetherWith fadeOut(tween(150))
            },
            label = "countdown",
            modifier = Modifier.padding(top = PmuSpacing.small),
        ) { value ->
            // Dok se cita uputstvo nema nicega: broj koji "uskace" sam kaze da
            // igra pocinje, pa je natpis bio suvisan.
            Text(
                text = value?.toString().orEmpty(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private const val INSTRUCTION_MILLIS = 1_500L
private const val COUNTDOWN_FROM = 3
private const val COUNTDOWN_STEP_MILLIS = 700L
