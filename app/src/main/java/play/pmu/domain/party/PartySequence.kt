package play.pmu.domain.party

import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import kotlin.random.Random

/**
 * Jedna runda u rasporedu partije.
 *
 * Sadrzi samo ono sto zavisi od PARTIJE: koja se igra igra i ko pocinje.
 * Korisnicka podesavanja (velicina table, dozvoljene operacije) NISU ovde -
 * njih igre citaju iz GameSettings kada runda pocne. Tako "Random" velicina
 * table ostaje neizvucena do trenutka igranja, a raspored ne dobija polja koja
 * vecini igara nista ne znace.
 */
data class PartyRound(
    val game: MiniGame,
    /** null za igre kod kojih nije vazno ko prvi igra. */
    val startingPlayer: Player?,
) {
    /**
     * Ko igra prvi potez. Raspored uvek odredi pocetnog igraca za igre sa
     * naizmenicnim potezima; kod ostalih igara vrednost nista ne znaci, pa se
     * vraca [Player.ONE].
     */
    val firstPlayer: Player get() = startingPlayer ?: Player.ONE
}

/**
 * Pravi slucajan raspored mini igara za jednu partiju.
 *
 * Funkcija je cista (nema state-a, ne zavisi ni od jedne Android klase) i
 * [random] se prosledjuje kao parametar, pa se u testu moze dati Random(seed) i
 * dobiti uvek isti raspored. Zato je moguce testirati je obicnim JUnit testom.
 *
 * Dva pravila:
 *
 *  1. Ista igra se ne pojavljuje dva puta zaredom - kandidati za sledecu rundu
 *     su sve igre osim poslednje izabrane.
 *  2. Kod igara sa naizmenicnim potezima ([MiniGame.needsStartingPlayer]) prvo
 *     pojavljivanje dobija SLUCAJNOG pocetnog igraca, a svako sledece
 *     pojavljivanje TE igre obrce redosled. Racuna se po igri, pa iks-oks i
 *     memorija ne uticu jedno na drugo. Posto se raspored pravi za svaku partiju
 *     iznova, istorija se sama resetuje - nema sta da se cuva.
 */
fun buildPartySequence(
    rounds: Int,
    games: List<MiniGame> = MiniGame.entries,
    random: Random = Random.Default,
): List<PartyRound> {
    require(rounds > 0) { "Partija mora imati najmanje jednu rundu" }
    require(games.isNotEmpty()) { "Lista igara ne sme biti prazna" }

    val sequence = mutableListOf<PartyRound>()
    val lastStartingPlayer = mutableMapOf<MiniGame, Player>()

    repeat(rounds) {
        val candidates = games.filter { it != sequence.lastOrNull()?.game }
            // Ako postoji samo jedna igra, ponavljanje je neizbezno.
            .ifEmpty { games }
        val game = candidates.random(random)

        val startingPlayer = if (game.needsStartingPlayer) {
            val next = lastStartingPlayer[game]?.opponent ?: randomPlayer(random)
            lastStartingPlayer[game] = next
            next
        } else {
            null
        }

        sequence += PartyRound(game = game, startingPlayer = startingPlayer)
    }
    return sequence
}

/** Slucajan igrac - koristi se za prvo pojavljivanje igre u partiji. */
fun randomPlayer(random: Random = Random.Default): Player =
    if (random.nextBoolean()) Player.ONE else Player.TWO
