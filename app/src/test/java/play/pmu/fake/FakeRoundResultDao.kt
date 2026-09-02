package play.pmu.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import play.pmu.data.local.MiniGameStats
import play.pmu.data.local.RoundResultDao
import play.pmu.data.local.RoundResultEntity
import play.pmu.domain.model.Winner

/**
 * Istorija odigranih rundi u memoriji, za testove.
 *
 * GROUP BY upit iz prave baze je ovde napisan kao obicno grupisanje po
 * kolekcijama - dovoljno da se proveri sta ViewModel radi sa rezultatom.
 */
class FakeRoundResultDao : RoundResultDao {

    private val rounds = MutableStateFlow<List<RoundResultEntity>>(emptyList())
    private var nextId = 1L

    val saved: List<RoundResultEntity> get() = rounds.value

    override suspend fun insert(result: RoundResultEntity): Long {
        val id = nextId++
        rounds.value = rounds.value + result.copy(id = id)
        return id
    }

    override fun observeRecent(limit: Int): Flow<List<RoundResultEntity>> =
        rounds.map { list -> list.sortedByDescending { it.playedAt }.take(limit) }

    override fun observePerGame(playerOne: Winner, playerTwo: Winner): Flow<List<MiniGameStats>> =
        rounds.map { list ->
            list.groupBy { it.game }.map { (game, played) ->
                MiniGameStats(
                    game = game,
                    played = played.size,
                    winsOne = played.count { it.winner == playerOne },
                    winsTwo = played.count { it.winner == playerTwo },
                )
            }
        }

    override suspend fun deleteAll() {
        rounds.value = emptyList()
    }
}
