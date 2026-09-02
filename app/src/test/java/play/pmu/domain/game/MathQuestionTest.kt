package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import play.pmu.domain.model.MathOperation
import kotlin.random.Random

/**
 * Testovi generisanja racunskih pitanja.
 *
 * Svaka provera se ponavlja mnogo puta sa razlicitim seed-om, jer je generisanje
 * slucajno - jedan prolaz ne bi nista dokazao.
 */
class MathQuestionTest {

    private fun questions(operations: Set<MathOperation>, count: Int = 200) =
        (0 until count).map { seed ->
            randomMathQuestion(operations = operations, random = Random(seed))
        }

    /** Rastavlja "12 ÷ 3 = ?" na levi operand, znak i desni operand. */
    private fun parse(text: String): Triple<Int, String, Int> {
        val parts = text.removeSuffix(" = ?").split(" ")
        return Triple(parts[0].toInt(), parts[1], parts[2].toInt())
    }

    @Test
    fun `koristi se samo ukljucena operacija`() {
        MathOperation.entries.forEach { operation ->
            val symbols = questions(setOf(operation)).map { parse(it.text).second }.distinct()
            assertEquals(listOf(operation.symbol), symbols)
        }
    }

    @Test
    fun `sa vise ukljucenih operacija koriste se sve i nijedna druga`() {
        val enabled = setOf(MathOperation.PLUS, MathOperation.TIMES)
        val symbols = questions(enabled, count = 400).map { parse(it.text).second }.toSet()
        assertEquals(enabled.map { it.symbol }.toSet(), symbols)
    }

    @Test
    fun `prazan skup operacija se vraca na podrazumevane`() {
        // Pitanje mora da se napravi, pa se prazan izbor tretira kao "sve".
        val symbols = questions(emptySet(), count = 400).map { parse(it.text).second }.toSet()
        assertEquals(MathOperation.entries.map { it.symbol }.toSet(), symbols)
    }

    @Test
    fun `deljenje daje ceo broj i nikada ne deli nulom`() {
        questions(setOf(MathOperation.DIVIDE)).forEach { question ->
            val (dividend, _, divisor) = parse(question.text)
            assertTrue("delilac je $divisor", divisor >= 2)
            assertEquals(0, dividend % divisor)
            assertEquals(dividend / divisor, question.correctAnswer)
        }
    }

    @Test
    fun `oduzimanje nikada ne daje negativan rezultat`() {
        questions(setOf(MathOperation.MINUS)).forEach { question ->
            assertTrue(question.correctAnswer >= 0)
        }
    }

    @Test
    fun `tacan odgovor odgovara postavljenom racunu`() {
        questions(MathOperation.DEFAULT, count = 400).forEach { question ->
            val (left, symbol, right) = parse(question.text)
            val expected = when (symbol) {
                MathOperation.PLUS.symbol -> left + right
                MathOperation.MINUS.symbol -> left - right
                MathOperation.TIMES.symbol -> left * right
                else -> left / right
            }
            assertEquals(question.text, expected, question.correctAnswer)
        }
    }

    @Test
    fun `ponudjena su cetiri razlicita odgovora, tacan tacno jednom`() {
        questions(MathOperation.DEFAULT, count = 400).forEach { question ->
            assertEquals(ANSWER_COUNT, question.answers.size)
            assertEquals(ANSWER_COUNT, question.answers.distinct().size)
            assertEquals(1, question.answers.count { it == question.correctAnswer })
            assertTrue(question.answers.all { it >= 0 })
        }
    }

    @Test
    fun `isti seed daje isto pitanje`() {
        val first = randomMathQuestion(random = Random(7))
        val second = randomMathQuestion(random = Random(7))
        assertEquals(first, second)
    }
}
