package play.pmu.domain.model

/**
 * Pitanje spremno za prikaz: [answers] su vec izmesani ponudjeni odgovori,
 * pa UI samo iscrtava listu i uporedjuje izbor sa [correctAnswer].
 */
data class TriviaQuestion(
    val question: String,
    val correctAnswer: String,
    val answers: List<String>,
)
