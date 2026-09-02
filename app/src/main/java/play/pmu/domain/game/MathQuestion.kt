package play.pmu.domain.game

import play.pmu.domain.model.MathOperation
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
 * Pravi slucajno pitanje iz jedne od UKLJUCENIH operacija.
 *
 * Funkcija je cista i prima [random] kao parametar, pa je u testu ponasanje
 * ponovljivo. [operations] dolazi iz podesavanja; ako je prazan, koriste se sve
 * operacije, jer pitanje mora da se napravi.
 *
 * Deljenje se generise NAOPAKO - prvo se izvuce rezultat i delilac, a deljenik
 * se izracuna kao njihov proizvod. Zato je odgovor uvek ceo broj i nikada nema
 * deljenja nulom (delilac pocinje od 2).
 */
fun randomMathQuestion(
    operations: Set<MathOperation> = MathOperation.DEFAULT,
    random: Random = Random.Default,
): MathQuestion {
    val operation = operations.ifEmpty { MathOperation.DEFAULT }.random(random)

    val left: Int
    val right: Int
    val correct: Int
    when (operation) {
        MathOperation.PLUS -> {
            left = random.nextInt(2, 40)
            right = random.nextInt(2, 40)
            correct = left + right
        }

        MathOperation.MINUS -> {
            // Veci broj je levo, pa rezultat nikada nije negativan.
            left = random.nextInt(10, 60)
            right = random.nextInt(2, left)
            correct = left - right
        }

        MathOperation.TIMES -> {
            left = random.nextInt(2, 10)
            right = random.nextInt(2, 10)
            correct = left * right
        }

        MathOperation.DIVIDE -> {
            correct = random.nextInt(2, 13)
            right = random.nextInt(2, 10)
            left = correct * right
        }
    }

    return MathQuestion(
        text = "$left ${operation.symbol} $right = ?",
        correctAnswer = correct,
        answers = plausibleAnswers(correct, random),
    )
}

/**
 * Tacan odgovor i tri netacna, izmesani.
 *
 * Netacni se biraju blizu tacnog (do +-6), da izbor ne bi bio ocigledan.
 * `while` petlja preko Set-a garantuje [ANSWER_COUNT] razlicitih vrednosti.
 */
private fun plausibleAnswers(correct: Int, random: Random): List<Int> {
    val answers = mutableSetOf(correct)
    while (answers.size < ANSWER_COUNT) {
        val offset = random.nextInt(-6, 7)
        val candidate = correct + offset
        if (offset != 0 && candidate >= 0) answers += candidate
    }
    return answers.shuffled(random)
}

const val ANSWER_COUNT = 4
