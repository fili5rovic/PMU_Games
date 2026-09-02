package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

/**
 * Sve igre u aplikaciji. Enum nosi i svoje resurse (naziv, opis, ikonicu), pa
 * Home ekran moze da se iscrta obicnim prolaskom kroz [GameType.entries] -
 * nema odvojene liste koja bi mogla da se raziđe sa enum-om.
 *
 * [lowerIsBetter] postoji jer se skor ne tumaci isto u svim igrama: u Brzini
 * reakcije (milisekunde) i u Memoriji (broj poteza) manji broj je bolji, a u
 * Pantomimi i Kvizu veci. Statistika na osnovu ovog polja zna da li najbolji
 * rezultat trazi kao MIN ili kao MAX.
 */
enum class GameType(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val lowerIsBetter: Boolean = false,
) {
    CHARADES(R.string.game_charades_title, R.string.game_charades_desc, R.drawable.ic_charades),
    QUIZ(R.string.game_quiz_title, R.string.game_quiz_desc, R.drawable.ic_quiz),
    REACTION(
        titleRes = R.string.game_reaction_title,
        descriptionRes = R.string.game_reaction_desc,
        iconRes = R.drawable.ic_reaction,
        lowerIsBetter = true,
    ),
    MEMORY(
        titleRes = R.string.game_memory_title,
        descriptionRes = R.string.game_memory_desc,
        iconRes = R.drawable.ic_memory,
        lowerIsBetter = true,
    ),
}
