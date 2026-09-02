package play.pmu.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType

/**
 * Baza u memoriji za testove.
 *
 * Nije bila potrebna nikakva dodatna apstrakcija: Room DAO je vec interface,
 * pa se u testu prosto zameni ovom implementacijom.
 */
class FakeGameResultDao : GameResultDao {

    private val results = MutableStateFlow<List<GameResultEntity>>(emptyList())
    private var nextId = 1L

    val saved: List<GameResultEntity> get() = results.value

    override suspend fun insert(result: GameResultEntity): Long {
        val id = nextId++
        results.value = results.value + result.copy(id = id)
        return id
    }

    override suspend fun findById(id: Long): GameResultEntity? =
        results.value.find { it.id == id }

    override fun observeRecent(limit: Int): Flow<List<GameResultEntity>> =
        results.map { list -> list.sortedByDescending { it.playedAt }.take(limit) }

    override fun observePlayCount(gameType: GameType): Flow<Int> =
        results.map { list -> list.count { it.gameType == gameType } }

    override fun observeMaxScore(gameType: GameType): Flow<Int?> =
        results.map { list -> list.filter { it.gameType == gameType }.maxOfOrNull { it.score } }

    override suspend fun deleteAll() {
        results.value = emptyList()
    }
}
