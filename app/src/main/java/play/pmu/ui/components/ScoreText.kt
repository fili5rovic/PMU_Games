package play.pmu.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import play.pmu.R
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType

@Composable
fun scoreText(result: GameResultEntity): String = when (result.gameType) {
    GameType.CHARADES -> stringResource(R.string.score_words, result.score, result.total)
    GameType.QUIZ -> stringResource(R.string.score_answers, result.score, result.total)
}
