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

enum class QuestionSource { NETWORK, CACHE, BUNDLED }

data class TriviaQuestions(
    val questions: List<TriviaQuestion>,
    val source: QuestionSource,
)

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

    suspend fun refreshCache(category: TriviaCategory, count: Int): Boolean =
        fetchFromNetwork(category, count).isNotEmpty()

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
        emptyList()
    }

    private fun bundledQuestions(category: TriviaCategory, count: Int): List<TriviaQuestion> {
        val res = try {
            context.resources?.getStringArray(category.fallbackRes)
        } catch (e: Exception) {
            null
        } ?: return emptyList()

        return res.mapNotNull { line ->
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
    }

    private fun TriviaQuestionEntity.toDomain() = TriviaQuestion(
        question = question,
        correctAnswer = correctAnswer,
        answers = answers,
    )

    private fun String.unescapeHtml(): String = try {
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)?.toString() ?: this
    } catch (e: Exception) {
        this
    }

    private companion object {
        const val RESPONSE_CODE_SUCCESS = 0
        const val FALLBACK_SEPARATOR = "|"
    }
}
