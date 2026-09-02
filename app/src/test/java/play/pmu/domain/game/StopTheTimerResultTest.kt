package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Test
import play.pmu.domain.model.Winner

/**
 * Testovi poredjenja u igri "stani na vreme".
 *
 * Poredjenje je izvuceno iz ViewModel-a u [StopTheTimerResult], pa se testira
 * zadatim brojevima - bez ijednog stvarnog cekanja i bez `Thread.sleep`, koji bi
 * test ucinio nepouzdanim.
 */
class StopTheTimerResultTest {

    private fun result(one: Int, two: Int, target: Int = 5_000) =
        StopTheTimerResult(targetMillis = target, playerOneMillis = one, playerTwoMillis = two)

    @Test
    fun `odstupanje je apsolutna razlika od cilja`() {
        val result = result(one = 4_720, two = 5_310)
        assertEquals(280, result.playerOneErrorMillis)
        assertEquals(310, result.playerTwoErrorMillis)
    }

    @Test
    fun `pobedjuje igrac sa manjim odstupanjem - prvi igrac`() {
        // Primer iz zadatka: 4.72 s prema 5.31 s uz cilj 5.0 s.
        assertEquals(Winner.PLAYER_ONE, result(one = 4_720, two = 5_310).winner)
    }

    @Test
    fun `pobedjuje igrac sa manjim odstupanjem - drugi igrac`() {
        assertEquals(Winner.PLAYER_TWO, result(one = 6_000, two = 5_100).winner)
    }

    @Test
    fun `rano i pozno odstupanje vrede isto`() {
        // Isto odstupanje sa obe strane cilja - nereseno.
        assertEquals(Winner.DRAW, result(one = 4_500, two = 5_500).winner)
    }

    @Test
    fun `prerano zaustavljanje gubi od blizeg pogotka`() {
        assertEquals(Winner.PLAYER_TWO, result(one = 1_000, two = 4_900).winner)
    }

    @Test
    fun `isto vreme je nereseno`() {
        assertEquals(Winner.DRAW, result(one = 5_100, two = 5_100).winner)
    }

    @Test
    fun `razlika unutar tolerancije je nereseno`() {
        val within = StopTheTimerResult.DRAW_TOLERANCE_MILLIS
        assertEquals(Winner.DRAW, result(one = 5_000, two = 5_000 + within).winner)
    }

    @Test
    fun `razlika iznad tolerancije daje pobednika`() {
        val above = StopTheTimerResult.DRAW_TOLERANCE_MILLIS + 1
        assertEquals(Winner.PLAYER_ONE, result(one = 5_000, two = 5_000 + above).winner)
    }

    @Test
    fun `manje od dva odstupanja se prikazuje kao rezultat runde`() {
        assertEquals(280, result(one = 4_720, two = 5_310).bestErrorMillis)
    }

    @Test
    fun `ciljno vreme je u dogovorenom opsegu`() {
        repeat(100) { seed ->
            val target = StopTheTimerResult.randomTargetMillis(kotlin.random.Random(seed))
            assertEquals(0, target % 1_000)
            assert(target / 1_000 in StopTheTimerResult.TARGET_RANGE_SECONDS)
        }
    }
}
