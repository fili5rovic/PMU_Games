package play.pmu.data.repository

import android.content.Context
import androidx.core.text.HtmlCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import play.pmu.data.local.TriviaQuestionDao
import play.pmu.data.local.TriviaQuestionEntity
import play.pmu.data.remote.TriviaApi
import play.pmu.domain.model.TriviaCategory
import play.pmu.domain.model.TriviaQuestion
import javax.inject.Inject
import javax.inject.Singleton

/** Odakle su pitanja stigla - UI na osnovu ovoga prikazuje obavestenje. */
enum class QuestionSource { NETWORK, CACHE, BUNDLED }

data class TriviaQuestions(
    val questions: List<TriviaQuestion>,
    val source: QuestionSource,
)

/**
 * Pitanja za kviz, sa tri nivoa rezerve:
 *
 *  1. REST poziv na Open Trivia DB (i upis u Room cache),
 *  2. Room cache, ako mreza ne radi,
 *  3. pitanja ugradjena u resurse, ako je cache prazan (npr. prvo pokretanje bez interneta).
 *
 * Zbog toga kviz nikada ne ostane bez sadrzaja.
 */
@Singleton
class TriviaRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val api: TriviaApi,
    private val dao: TriviaQuestionDao,
) {

    suspend fun loadQuestions(category: TriviaCategory, count: Int): TriviaQuestions {
        val fromNetwork = fetchFromNetwork(category, count)
        if (fromNetwork.isNotEmpty()) {
            return TriviaQuestions(fromNetwork.map { it.toDomain() }, QuestionSource.NETWORK)
        }

        val cached = dao.findByCategory(category, count)
        if (cached.isNotEmpty()) {
            return TriviaQuestions(cached.map { it.toDomain() }, QuestionSource.CACHE)
        }

        return TriviaQuestions(bundledQuestions(category, count), QuestionSource.BUNDLED)
    }

    /**
     * Osvezavanje cache-a bez prikazivanja pitanja - koristi ga WorkManager.
     * Vraca true ako je cache uspesno napunjen.
     */
    suspend fun refreshCache(category: TriviaCategory, count: Int): Boolean =
        fetchFromNetwork(category, count).isNotEmpty()

    /**
     * Vraca pitanja sa mreze i usput puni cache. Mrezne greske se ovde hvataju i
     * pretvaraju u praznu listu, jer pozivaocu treba samo "ima ili nema" -
     * odluku o rezervi donosi loadQuestions.
     */
    private suspend fun fetchFromNetwork(
        category: TriviaCategory,
        count: Int,
    ): List<TriviaQuestionEntity> = try {
        val response = api.getQuestions(amount = count, categoryId = category.apiId)
        // response_code 0 znaci uspeh; sve ostalo je npr. "nema dovoljno pitanja".
        if (response.responseCode != RESPONSE_CODE_SUCCESS) {
            emptyList()
        } else {
            val entities = response.results.map { dto ->
                TriviaQuestionEntity(
                    category = category,
                    question = dto.question.unescapeHtml(),
                    correctAnswer = dto.correctAnswer.unescapeHtml(),
                    // Odgovori se mesaju odmah, pa tacan nije uvek na istom mestu.
                    answers = (dto.incorrectAnswers + dto.correctAnswer)
                        .map { it.unescapeHtml() }
                        .shuffled(),
                )
            }
            if (entities.isNotEmpty()) dao.replaceCategory(category, entities)
            entities
        }
    } catch (e: Exception) {
        // Nema interneta, timeout, neispravan JSON... - u svakom slucaju idemo na rezervu.
        emptyList()
    }

    /** Rezervna pitanja iz resursa. Format reda: pitanje | tacan | netacan | netacan | netacan */
    private fun bundledQuestions(category: TriviaCategory, count: Int): List<TriviaQuestion> =
        context.resources.getStringArray(category.fallbackRes)
            .mapNotNull { line ->
                val parts = line.split(FALLBACK_SEPARATOR)
                if (parts.size < 2) return@mapNotNull null
                TriviaQuestion(
                    question = parts[0],
                    correctAnswer = parts[1],
                    answers = parts.drop(1).shuffled(),
                )
            }
            .shuffled()
            .take(count)

    private fun TriviaQuestionEntity.toDomain() = TriviaQuestion(
        question = question,
        correctAnswer = correctAnswer,
        answers = answers,
    )

    /** Open Trivia DB vraca HTML entitete, npr. &quot; umesto navodnika. */
    private fun String.unescapeHtml(): String =
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    private companion object {
        const val RESPONSE_CODE_SUCCESS = 0
        const val FALLBACK_SEPARATOR = "|"
    }
}
