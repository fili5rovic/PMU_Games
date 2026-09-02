package play.pmu.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Odgovor Open Trivia DB-a (https://opentdb.com/api.php).
 *
 * @SerialName mapira snake_case imena iz JSON-a na Kotlin konvenciju, pa
 * ostatak koda ne mora da zna kako API imenuje polja.
 */
@Serializable
data class TriviaResponseDto(
    @SerialName("response_code") val responseCode: Int,
    val results: List<TriviaQuestionDto> = emptyList(),
)

@Serializable
data class TriviaQuestionDto(
    val question: String,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("incorrect_answers") val incorrectAnswers: List<String>,
)
