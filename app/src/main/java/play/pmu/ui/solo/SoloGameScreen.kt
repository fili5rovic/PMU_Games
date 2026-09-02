package play.pmu.ui.solo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import play.pmu.R
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.ui.components.PartyScore
import play.pmu.ui.components.RoundResultView
import play.pmu.ui.party.MiniGameRound
import play.pmu.ui.theme.PmuSpacing

/**
 * Jedna mini igra, izabrana sa pocetnog ekrana i van partije.
 *
 * Isti [MiniGameRound] kao u partiji, pa se tok (uputstvo, odbrojavanje, igra)
 * ne pise dva puta. Razlika je samo u tome sta se radi sa ishodom: partija ga
 * ubraja u ukupan skor, a ovde se broje pobede u nizu rundi i one se NE upisuju
 * u bazu - pojedinacne runde su vezbanje, a istorija se vodi za partije.
 *
 * Broj pobeda putuje kroz navigacione argumente ([winsOne], [winsTwo]), jer
 * svaka nova runda ide na novu destinaciju - tako svaka runda dobija cist
 * ViewModel, a rezultat se ne izgubi.
 */
@Composable
fun SoloGameScreen(
    game: MiniGame,
    attempt: Int,
    winsOne: Int,
    winsTwo: Int,
    onPlayAgain: (winsOne: Int, winsTwo: Int) -> Unit,
    onNavigateBack: () -> Unit,
) {
    // Ishod runde je kratkotrajan i vezan za ovaj prikaz, pa je `remember`
    // dovoljan: orijentacija je zakljucana na portret, tako da promene
    // konfiguracije koja bi ga obrisala nema.
    var outcome by remember { mutableStateOf<RoundOutcome?>(null) }

    val current = outcome
    if (current == null) {
        MiniGameRound(
            game = game,
            onFinished = { outcome = it },
            // U prvoj rundi jos nema sta da se prikaze, posle nje stoji
            // trenutni rezultat niza rundi.
            roundLabel = if (attempt > 1) {
                stringResource(R.string.party_score, winsOne, winsTwo)
            } else {
                null
            },
        )
        return
    }

    val newWinsOne = winsOne + if (current.winner == Winner.PLAYER_ONE) 1 else 0
    val newWinsTwo = winsTwo + if (current.winner == Winner.PLAYER_TWO) 1 else 0

    RoundResultView(outcome = current) {
        PartyScore(scoreOne = newWinsOne, scoreTwo = newWinsTwo)
        // Dugmad su na obe polovine ekrana, pa novu rundu moze da pokrene bilo
        // koji od dva igraca.
        Row(horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small)) {
            OutlinedButton(onClick = onNavigateBack) {
                Text(stringResource(R.string.result_home))
            }
            Button(onClick = { onPlayAgain(newWinsOne, newWinsTwo) }) {
                Text(stringResource(R.string.solo_play_again))
            }
        }
    }
}
