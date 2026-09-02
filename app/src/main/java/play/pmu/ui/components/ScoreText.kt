package play.pmu.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import play.pmu.R
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType

/**
 * Skor se ne cita isto u pantomimi i u kvizu, pa se tekst formira po tipu igre.
 * `when` nad enum-om je exhaustive: dodavanje nove igre nece se moci
 * prevesti dok se ovde ne doda i njen format.
 */
@Composable
fun scoreText(result: GameResultEntity): String = when (result.gameType) {
    GameType.CHARADES -> stringResource(R.string.score_words, result.score, result.total)
    GameType.QUIZ -> stringResource(R.string.score_answers, result.score, result.total)
}
