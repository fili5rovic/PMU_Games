package play.pmu.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import play.pmu.domain.model.CharadesCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharadesWordsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun shuffledWords(category: CharadesCategory): List<String> =
        context.resources.getStringArray(category.wordsRes).toList().shuffled()
}
