package play.pmu.data.repository

import kotlinx.coroutines.flow.Flow
import play.pmu.data.local.MiniGameStats
import play.pmu.data.local.RoundResultDao
import play.pmu.data.local.RoundResultEntity
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Winner
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Jedini put do istorije odigranih rundi mini igara.
 *
 * Runde upisuju i partija i pojedinacna igra, pa se u statistici vidi svaka
 * odigrana runda - bez obzira na to kako je pokrenuta.
 */
@Singleton
class RoundResultsRepository @Inject constructor(
    private val dao: RoundResultDao,
) {

    suspend fun save(game: MiniGame, winner: Winner): Long = dao.insert(
        RoundResultEntity(
            game = game,
            winner = winner,
            playedAt = System.currentTimeMillis(),
        )
    )

    fun observeRecent(limit: Int = RECENT_LIMIT): Flow<List<RoundResultEntity>> =
        dao.observeRecent(limit)

    fun observePerGame(): Flow<List<MiniGameStats>> =
        dao.observePerGame(playerOne = Winner.PLAYER_ONE, playerTwo = Winner.PLAYER_TWO)

    suspend fun clearHistory() = dao.deleteAll()

    private companion object {
        const val RECENT_LIMIT = 20
    }
}
