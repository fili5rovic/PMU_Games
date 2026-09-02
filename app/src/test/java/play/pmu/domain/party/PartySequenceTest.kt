package play.pmu.domain.party

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import play.pmu.domain.model.MiniGame
import kotlin.random.Random

/**
 * Testovi rasporeda mini igara u partiji.
 *
 * Random se prosledjuje kao parametar, pa se sa istim seed-om dobija uvek isti
 * raspored - test ne zavisi od slucajnosti.
 */
class PartySequenceTest {

    @Test
    fun `raspored ima tacno onoliko rundi koliko je trazeno`() {
        assertEquals(7, buildPartySequence(rounds = 7, random = Random(1)).size)
        assertEquals(1, buildPartySequence(rounds = 1, random = Random(1)).size)
    }

    @Test
    fun `ista igra se nikada ne pojavljuje dva puta zaredom`() {
        // Vise seed-ova, da provera ne prodje slucajno.
        repeat(50) { seed ->
            val sequence = buildPartySequence(rounds = 20, random = Random(seed))
            sequence.zipWithNext().forEach { (current, next) ->
                assertNotEquals("seed $seed", current, next)
            }
        }
    }

    @Test
    fun `isti seed daje isti raspored`() {
        val first = buildPartySequence(rounds = 9, random = Random(42))
        val second = buildPartySequence(rounds = 9, random = Random(42))
        assertEquals(first, second)
    }

    @Test
    fun `duza partija koristi vise razlicitih igara`() {
        val sequence = buildPartySequence(rounds = 30, random = Random(7))
        // Uz 30 rundi je prakticno nemoguce da se neka igra ne pojavi.
        assertEquals(MiniGame.entries.size, sequence.distinct().size)
    }

    @Test
    fun `sa samo jednom igrom raspored je ponavljanje te igre`() {
        val sequence = buildPartySequence(
            rounds = 3,
            games = listOf(MiniGame.TAP_RACE),
            random = Random(1),
        )
        assertEquals(List(3) { MiniGame.TAP_RACE }, sequence)
    }

    @Test
    fun `broj rundi manji od jedan nije dozvoljen`() {
        val failed = runCatching { buildPartySequence(rounds = 0) }.isFailure
        assertTrue(failed)
    }

    @Test
    fun `prazna lista igara nije dozvoljena`() {
        val failed = runCatching {
            buildPartySequence(rounds = 3, games = emptyList())
        }.isFailure
        assertTrue(failed)
    }
}
