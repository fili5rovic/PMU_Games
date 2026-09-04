package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

enum class GameType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val orientation: GameOrientation = GameOrientation.PORTRAIT,
) {
    CHARADES(
        titleRes = R.string.game_charades_title,
        descriptionRes = R.string.game_charades_desc,
        iconRes = R.drawable.ic_charades,
        // Telefon se drzi polozeno na celu, pa je pantomima jedini landscape ekran.
        orientation = GameOrientation.LANDSCAPE,
    ),
    QUIZ(R.string.game_quiz_title, R.string.game_quiz_desc, R.drawable.ic_quiz),
}
