package play.pmu.domain.model

data class TriviaQuestion(
    val question: String,
    val correctAnswer: String,
    val answers: List<String>,
)
