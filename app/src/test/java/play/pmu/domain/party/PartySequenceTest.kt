package play.pmu.domain.party

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import kotlin.random.Random

/**
 * Testovi rasporeda partije: koje se igre pojavljuju i ko u njima pocinje.
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
            sequence.map { it.game }.zipWithNext().forEach { (current, next) ->
                assertNotEquals("seed $seed", current, next)
            }
        }
    }

    @Test
    fun `isti seed daje isti raspored`() {
        assertEquals(
            buildPartySequence(rounds = 9, random = Random(42)),
            buildPartySequence(rounds = 9, random = Random(42)),
        )
    }

    @Test
    fun `duza partija koristi sve igre`() {
        val sequence = buildPartySequence(rounds = 60, random = Random(7))
        assertEquals(MiniGame.entries.size, sequence.map { it.game }.distinct().size)
    }

    @Test
    fun `sa samo jednom igrom raspored je ponavljanje te igre`() {
        val sequence = buildPartySequence(
            rounds = 3,
            games = listOf(MiniGame.TAP_RACE),
            random = Random(1),
        )
        assertEquals(List(3) { MiniGame.TAP_RACE }, sequence.map { it.game })
    }

    @Test
    fun `broj rundi manji od jedan nije dozvoljen`() {
        assertTrue(runCatching { buildPartySequence(rounds = 0) }.isFailure)
    }

    @Test
    fun `prazna lista igara nije dozvoljena`() {
        assertTrue(runCatching { buildPartySequence(rounds = 3, games = emptyList()) }.isFailure)
    }

    // --- pocetni igrac ---

    @Test
    fun `igre bez naizmenicnih poteza ne dobijaju pocetnog igraca`() {
        val sequence = buildPartySequence(rounds = 40, random = Random(3))
        sequence.filterNot { it.game.needsStartingPlayer }.forEach { round ->
            assertNull(round.game.name, round.startingPlayer)
            // firstPlayer je i tada upotrebljiv, samo nista ne znaci.
            assertEquals(Player.ONE, round.firstPlayer)
        }
    }

    @Test
    fun `igre sa naizmenicnim potezima uvek dobiju pocetnog igraca`() {
        val sequence = buildPartySequence(rounds = 40, random = Random(4))
        sequence.filter { it.game.needsStartingPlayer }.forEach { round ->
            assertTrue(round.game.name, round.startingPlayer != null)
        }
    }

    @Test
    fun `ponovno pojavljivanje iste igre obrce pocetnog igraca`() {
        // Samo iks-oks u listi, pa se u svakoj rundi ponavlja.
        val sequence = buildPartySequence(
            rounds = 6,
            games = listOf(MiniGame.TIC_TAC_TOE),
            random = Random(5),
        )
        val starters = sequence.map { it.startingPlayer }

        starters.zipWithNext().forEach { (current, next) ->
            assertNotEquals(current, next)
        }
        // Dakle: A, B, A, B, A, B
        assertEquals(starters[0], starters[2])
        assertEquals(starters[1], starters[3])
    }

    @Test
    fun `prvi pocetni igrac nije uvek isti`() {
        // Preko mnogo seed-ova moraju da se pojave oba igraca kao prvi.
        val firstStarters = (0 until 40).map { seed ->
            buildPartySequence(
                rounds = 1,
                games = listOf(MiniGame.TIC_TAC_TOE),
                random = Random(seed),
            ).first().startingPlayer
        }.toSet()

        assertEquals(setOf(Player.ONE, Player.TWO), firstStarters)
    }

    @Test
    fun `svaka igra ima svoju istoriju pocetnog igraca`() {
        val sequence = buildPartySequence(
            rounds = 8,
            games = listOf(MiniGame.TIC_TAC_TOE, MiniGame.MEMORY),
            random = Random(9),
        )

        // Iks-oks i memorija se naizmenicno ponavljaju, ali svaka obrce SVOJ
        // redosled - jedna ne utice na drugu.
        listOf(MiniGame.TIC_TAC_TOE, MiniGame.MEMORY).forEach { game ->
            val starters = sequence.filter { it.game == game }.map { it.startingPlayer }
            starters.zipWithNext().forEach { (current, next) ->
                assertNotEquals(game.name, current, next)
            }
        }
    }
}
