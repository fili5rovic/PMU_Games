package play.pmu.domain.model

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import play.pmu.R

/**
 * Kategorije kviza. [apiId] je id kategorije na Open Trivia DB (opentdb.com),
 * a [fallbackRes] su lokalna pitanja koja se koriste kad nema ni mreze ni cache-a.
 *
 * Kategorije su nabrojane u kodu (a ne dovucene sa /api_category.php) da bi
 * mrezni sloj ostao jednostavan - jedan endpoint umesto dva.
 */
enum class TriviaCategory(
    val apiId: Int,
    @StringRes val titleRes: Int,
    @ArrayRes val fallbackRes: Int,
) {
    GENERAL(9, R.string.trivia_general, R.array.fallback_questions_general),
    FILM(11, R.string.trivia_film, R.array.fallback_questions_film),
    SCIENCE(17, R.string.trivia_science, R.array.fallback_questions_science),
    SPORTS(21, R.string.trivia_sports, R.array.fallback_questions_sports),
    HISTORY(23, R.string.trivia_history, R.array.fallback_questions_history),
    ;

    companion object {
        /** Vraca kategoriju po id-u iz navigacionog argumenta, ili GENERAL ako id nije poznat. */
        fun fromApiId(apiId: Int): TriviaCategory = entries.find { it.apiId == apiId } ?: GENERAL
    }
}
