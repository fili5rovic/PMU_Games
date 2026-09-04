package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner

class AnswerDuelTest {

    @Test
    fun `na pocetku oba igraca smeju da odgovaraju`() {
        val duel = AnswerDuel()
        assertTrue(duel.canAnswer(Player.ONE))
        assertTrue(duel.canAnswer(Player.TWO))
        assertNull(duel.winner)
    }

    @Test
    fun `prvi tacan odgovor osvaja rundu`() {
        val duel = AnswerDuel().answer(Player.TWO, isCorrect = true)
        assertEquals(Winner.PLAYER_TWO, duel.winner)
    }

    @Test
    fun `pogresan odgovor iskljucuje igraca, protivnik nastavlja`() {
        val duel = AnswerDuel().answer(Player.ONE, isCorrect = false)

        assertNull(duel.winner)
        assertFalse(duel.canAnswer(Player.ONE))
        assertTrue(duel.canAnswer(Player.TWO))
    }

    @Test
    fun `iskljucen igrac ne moze da pobedi ni tacnim odgovorom`() {
        val duel = AnswerDuel()
            .answer(Player.ONE, isCorrect = false)
            .answer(Player.ONE, isCorrect = true)

        assertNull(duel.winner)
    }

    @Test
    fun `kada oba igraca promase runda je neresena`() {
        val duel = AnswerDuel()
            .answer(Player.ONE, isCorrect = false)
            .answer(Player.TWO, isCorrect = false)

        assertEquals(Winner.DRAW, duel.winner)
    }

    @Test
    fun `posle promasaja protivnik moze da pobedi`() {
        val duel = AnswerDuel()
            .answer(Player.ONE, isCorrect = false)
            .answer(Player.TWO, isCorrect = true)

        assertEquals(Winner.PLAYER_TWO, duel.winner)
    }

    @Test
    fun `drugi odgovor ne moze da preuzme pobedu`() {
        val duel = AnswerDuel()
            .answer(Player.ONE, isCorrect = true)
            .answer(Player.TWO, isCorrect = true)

        assertEquals(Winner.PLAYER_ONE, duel.winner)
    }

    @Test
    fun `spamovanje odgovora ne menja ishod`() {
        var duel = AnswerDuel()
        // Igrac 1 "isprska" sva cetiri odgovora: prvi promasaj ga iskljucuje.
        repeat(4) { duel = duel.answer(Player.ONE, isCorrect = false) }

        assertNull(duel.winner)
        assertEquals(setOf(Player.ONE), duel.lockedOut)
    }
}
