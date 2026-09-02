package play.pmu.ui.solo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.domain.model.Winner
import play.pmu.ui.components.PartyScore
import play.pmu.ui.components.ResultActions
import play.pmu.ui.components.RoundResultView
import play.pmu.ui.party.MiniGameRound

/**
 * Jedna mini igra van partije.
 *
 * Koristi isti [MiniGameRound] kao partija, pa se tok (uputstvo, odbrojavanje,
 * igra) ne pise dva puta. Razlika je samo u tome sta se radi sa ishodom: partija
 * ga ubraja u ukupan skor, a ovde se broje pobede u nizu rundi.
 *
 * Broj pobeda putuje kroz navigacione argumente ([winsOne], [winsTwo]), jer
 * svaka nova runda ide na novu destinaciju - tako svaka runda dobija cist
 * ViewModel, a rezultat se ne izgubi.
 */
@Composable
fun SoloGameScreen(
    attempt: Int,
    winsOne: Int,
    winsTwo: Int,
    onPlayAgain: (winsOne: Int, winsTwo: Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: SoloGameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val outcome = uiState.outcome

    if (outcome == null) {
        MiniGameRound(
            round = viewModel.round,
            gameSettings = uiState.gameSettings,
            onFinished = viewModel::onRoundFinished,
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

    val newWinsOne = winsOne + if (outcome.winner == Winner.PLAYER_ONE) 1 else 0
    val newWinsTwo = winsTwo + if (outcome.winner == Winner.PLAYER_TWO) 1 else 0

    RoundResultView(outcome = outcome) {
        PartyScore(scoreOne = newWinsOne, scoreTwo = newWinsTwo)
        // Dugmad su na obe polovine ekrana, pa novu rundu moze da pokrene bilo
        // koji od dva igraca.
        ResultActions(
            primaryLabel = stringResource(R.string.solo_play_again),
            onPrimary = { onPlayAgain(newWinsOne, newWinsTwo) },
            homeLabel = stringResource(R.string.result_home),
            onHome = onNavigateBack,
        )
    }
}
