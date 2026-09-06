package play.pmu.data.translation

import play.pmu.domain.model.TriviaQuestion

interface TriviaTranslator {
    suspend fun translateQuestions(questions: List<TriviaQuestion>): List<TriviaQuestion>
}
