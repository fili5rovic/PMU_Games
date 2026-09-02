package play.pmu.domain.game

import kotlin.random.Random

/**
 * Jedno racunsko pitanje sa ponudjenim odgovorima. [answers] sadrzi i
 * [correctAnswer], na slucajnom mestu.
 */
data class MathQuestion(
    val text: String,
    val correctAnswer: Int,
    val answers: List<Int>,
)

/**
 * Pravi slucajno pitanje: sabiranje, oduzimanje ili mnozenje malih brojeva.
 *
 * Kao i buildPartySequence, funkcija je cista i prima [random] kao parametar,
 * pa je u testu ponasanje ponovljivo.
 *
 * Netacni odgovori se biraju blizu tacnog (do +-6), da izbor ne bi bio ocigledan.
 * `while` petlja garantuje [ANSWER_COUNT] razlicitih vrednosti.
 */
fun randomMathQuestion(random: Random = Random.Default): MathQuestion {
    val operation = Operation.entries.random(random)
    val left: Int
    val right: Int
    when (operation) {
        Operation.PLUS -> {
            left = random.nextInt(2, 40)
            right = random.nextInt(2, 40)
        }
        Operation.MINUS -> {
            // Veci broj je levo, pa rezultat nikada nije negativan.
            left = random.nextInt(10, 60)
            right = random.nextInt(2, left)
        }
        Operation.TIMES -> {
            left = random.nextInt(2, 10)
            right = random.nextInt(2, 10)
        }
    }

    val correct = operation.apply(left, right)
    val answers = mutableSetOf(correct)
    while (answers.size < ANSWER_COUNT) {
        val offset = random.nextInt(-6, 7)
        val candidate = correct + offset
        if (offset != 0 && candidate >= 0) answers += candidate
    }

    return MathQuestion(
        text = "$left ${operation.symbol} $right = ?",
        correctAnswer = correct,
        answers = answers.shuffled(random),
    )
}

private enum class Operation(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("x");

    fun apply(left: Int, right: Int): Int = when (this) {
        PLUS -> left + right
        MINUS -> left - right
        TIMES -> left * right
    }
}

private const val ANSWER_COUNT = 4
