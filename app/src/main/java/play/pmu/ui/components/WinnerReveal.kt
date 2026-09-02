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

/**
 * Prelaz na kraju runde: boja pobednika "prelazi" preko ekrana sa NJEGOVE
 * strane telefona ka protivnickoj.
 *
 * Igrac 1 sedi kod donje ivice, pa njegova pobeda ide odozdo nagore; pobeda
 * igraca 2 obrnuto. Smer je zato jasan sa oba kraja telefona: boja "gura" ka
 * gubitniku.
 *
 * Izvedeno je najprostije sto moze: jedan Box kome se animira visina
 * ([animateFloatAsState] + [fillMaxHeight]), poravnat na pobednikovu ivicu.
 * Nema Canvas-a, cestica ni sopstvenog crtanja.
 *
 * Boja je blag ton igraceve boje ([areaColor]), pa tamna tema ostaje tamna, a
 * tekst preko njega ostaje citljiv. Nereseno ne koristi ni jednu igracevu boju
 * nego neutralnu podlogu, i ne animira se.
 *
 * Komponenta je jedna za celu aplikaciju - koriste je i rezultat runde i
 * rezultat partije, pa se animacija ne pise u svakoj igri.
 */
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

    // Prvi kadar se iscrta sa 0f, pa LaunchedEffect ukljuci ciljnu vrednost i
    // animateFloatAsState odradi prelaz. Bez ovog koraka bi pokrivenost odmah
    // bila 1f i animacije ne bi bilo.
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
            // Nereseno: neutralna podloga, bez prelaza i bez igracevih boja.
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
