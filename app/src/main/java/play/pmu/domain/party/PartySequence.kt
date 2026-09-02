package play.pmu.domain.party

import play.pmu.domain.model.MiniGame
import kotlin.random.Random

/**
 * Pravi slucajan raspored mini igara za jednu partiju.
 *
 * Funkcija je cista (nema state-a, ne zavisi ni od jedne Android klase) i
 * [random] se prosledjuje kao parametar, pa se u testu moze dati Random(seed) i
 * dobiti uvek isti raspored. Zato je moguce testirati je obicnim JUnit testom.
 *
 * Ista igra se ne pojavljuje dva puta zaredom: kandidati za sledecu rundu su
 * sve igre osim poslednje izabrane.
 */
fun buildPartySequence(
    rounds: Int,
    games: List<MiniGame> = MiniGame.entries,
    random: Random = Random.Default,
): List<MiniGame> {
    require(rounds > 0) { "Partija mora imati najmanje jednu rundu" }
    require(games.isNotEmpty()) { "Lista igara ne sme biti prazna" }

    val sequence = mutableListOf<MiniGame>()
    repeat(rounds) {
        val candidates = games.filter { it != sequence.lastOrNull() }
            // Ako postoji samo jedna igra, ponavljanje je neizbezno.
            .ifEmpty { games }
        sequence += candidates.random(random)
    }
    return sequence
}
