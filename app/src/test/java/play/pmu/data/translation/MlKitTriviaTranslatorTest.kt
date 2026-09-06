package play.pmu.data.translation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import play.pmu.domain.model.TriviaQuestion

class MlKitTriviaTranslatorTest {

    @Test
    fun `translateQuestions vraca praznu listu kada je ulaz prazna lista`() = runTest {
        val translator = MlKitTriviaTranslator()
        val result = translator.translateQuestions(emptyList())
        assertEquals(emptyList<TriviaQuestion>(), result)
    }

    @Test
    fun `translateQuestions bezbedno vraca originalna pitanja ako model nije dostupan ili baci gresku`() = runTest {
        val translator = MlKitTriviaTranslator()
        val original = listOf(
            TriviaQuestion(
                question = "What is 2+2?",
                correctAnswer = "4",
                answers = listOf("3", "4", "5"),
            )
        )
        val result = translator.translateQuestions(original)
        // Na JVM unit testu ML Kit model download/translate baca izuzetak,
        // sto translator bezbedno obradjuje i vraca originalna pitanja.
        assertEquals(original, result)
    }
}
