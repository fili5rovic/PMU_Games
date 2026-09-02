package play.pmu.domain.game

import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner

/**
 * Pravila duela u kome oba igraca odgovaraju na ISTO pitanje, a pobedjuje prvi
 * tacan odgovor.
 *
 * Koriste ga racunski duel i binarno-u-decimalno, pa je pravilo o kazni napisano
 * jednom: pogresan odgovor iskljucuje igraca do kraja runde (nagadjanje se ne
 * isplati), a ako oba promase, runda je neresena.
 *
 * Klasa je immutable i bez ijedne Android zavisnosti, pa se testira obicnim
 * JUnit testom.
 */
data class AnswerDuel(
    /** Igraci koji su promasili i vise ne mogu da odgovaraju u ovoj rundi. */
    val lockedOut: Set<Player> = emptySet(),
    /** null dok runda traje. */
    val winner: Winner? = null,
) {

    fun canAnswer(player: Player): Boolean = winner == null && player !in lockedOut

    /**
     * Odgovor jednog igraca. Vraca novo stanje, ili isto ako igrac trenutno ne
     * sme da odgovara.
     *
     * ZASTO NE MOGU DVA POBEDNIKA: Compose poziva `onClick` na glavnoj niti, pa
     * se dva "istovremena" odgovora izvrsavaju jedan za drugim. Prvi tacan
     * postavlja pobednika, a drugi odmah ispada iz [canAnswer].
     */
    fun answer(player: Player, isCorrect: Boolean): AnswerDuel {
        if (!canAnswer(player)) return this

        if (isCorrect) return copy(winner = player.asWinner)

        val locked = lockedOut + player
        return copy(
            lockedOut = locked,
            winner = if (locked.size == Player.entries.size) Winner.DRAW else null,
        )
    }
}
