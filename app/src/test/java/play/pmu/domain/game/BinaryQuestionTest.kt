package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * Testovi generisanja pitanja "binarno u decimalno".
 *
 * Pretvaranje se proverava obrnutim putem: `String.toInt(2)` mora da vrati istu
 * vrednost koju je generator upisao kao tacan odgovor.
 */
class BinaryQuestionTest {

    private val questions = (0 until 300).map { seed -> randomBinaryQuestion(Random(seed)) }

    @Test
    fun `binarni zapis odgovara tacnom decimalnom broju`() {
        questions.forEach { question ->
            assertEquals(question.correctAnswer, question.binary.toInt(2))
        }
    }

    @Test
    fun `broj ima izmedju cetiri i osam bita i pocinje jedinicom`() {
        questions.forEach { question ->
            assertTrue(question.binary.length in 4..8)
            assertTrue(question.binary.startsWith("1"))
            assertTrue(question.binary.all { it == '0' || it == '1' })
        }
    }

    @Test
    fun `trivijalni brojevi se ne postavljaju`() {
        questions.forEach { question ->
            val ones = question.binary.count { it == '1' }
            // Ni stepen dvojke (100000), ni sve jedinice (11111).
            assertTrue(question.binary, ones >= 2)
            assertTrue(question.binary, ones < question.binary.length)
        }
    }

    @Test
    fun `ponudjena su cetiri razlicita odgovora`() {
        questions.forEach { question ->
            assertEquals(ANSWER_COUNT, question.answers.size)
            assertEquals(ANSWER_COUNT, question.answers.distinct().size)
        }
    }

    @Test
    fun `tacan odgovor je medju ponudjenima tacno jednom`() {
        questions.forEach { question ->
            assertEquals(1, question.answers.count { it == question.correctAnswer })
        }
    }

    @Test
    fun `netacni odgovori su pozitivni i uverljivo blizu tacnog`() {
        questions.forEach { question ->
            val wrong = question.answers.filter { it != question.correctAnswer }
            assertTrue(wrong.all { it > 0 })
            // Okretanje jednog bita najdalje pomeri za najvisi bit broja.
            val maxDistance = 1 shl question.binary.length
            assertTrue(wrong.all { kotlin.math.abs(it - question.correctAnswer) <= maxDistance })
        }
    }

    @Test
    fun `isti seed daje isto pitanje`() {
        assertEquals(randomBinaryQuestion(Random(11)), randomBinaryQuestion(Random(11)))
    }
}
