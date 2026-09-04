package play.pmu.domain.game

import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner

data class AnswerDuel(
    val lockedOut: Set<Player> = emptySet(),
    val winner: Winner? = null,
) {

    fun canAnswer(player: Player): Boolean = winner == null && player !in lockedOut

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
