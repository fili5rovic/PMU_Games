package play.pmu.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import play.pmu.domain.model.CharadesCategory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pojmovi za pantomimu se citaju iz string-array resursa, pa se sadrzaj igre
 * prevodi zajedno sa UI-em (values/arrays.xml i values-sr/arrays.xml).
 */
@Singleton
class CharadesWordsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Pojmovi izabrane kategorije u slucajnom redosledu. */
    fun shuffledWords(category: CharadesCategory): List<String> =
        context.resources.getStringArray(category.wordsRes).toList().shuffled()
}
