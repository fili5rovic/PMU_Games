package play.pmu.domain.party

import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import kotlin.random.Random

data class PartyRound(
    val game: MiniGame,
    val startingPlayer: Player?,
) {
    val firstPlayer: Player get() = startingPlayer ?: Player.ONE
}

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

fun randomPlayer(random: Random = Random.Default): Player =
    if (random.nextBoolean()) Player.ONE else Player.TWO
