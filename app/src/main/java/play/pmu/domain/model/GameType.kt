package play.pmu.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import play.pmu.R

/**
 * Igre koje se ne igraju jedan na jedan na podeljenom ekranu, pa ne mogu da
 * budu deo partije: pantomima je igra za celo drustvo (i jedina koja radi u
 * landscape orijentaciji), a kviz je niz pitanja sa mreze.
 *
 * Za razliku od [MiniGame], ove igre daju SKOR (broj pogodjenih pojmova ili
 * tacnih odgovora), a ne pobednika, pa se njihove partije cuvaju u tabeli
 * game_results i prikazuju na ekranu statistike.
 */
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
