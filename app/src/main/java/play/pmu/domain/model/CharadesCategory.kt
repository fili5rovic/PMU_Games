package play.pmu.domain.model

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import play.pmu.R

enum class CharadesCategory(
    @StringRes val titleRes: Int,
    @ArrayRes val wordsRes: Int,
) {
    ANIMALS(R.string.category_animals, R.array.words_animals),
    MOVIES(R.string.category_movies, R.array.words_movies),
    SPORTS(R.string.category_sports, R.array.words_sports),
    PROFESSIONS(R.string.category_professions, R.array.words_professions),
    FOOD(R.string.category_food, R.array.words_food),
}
