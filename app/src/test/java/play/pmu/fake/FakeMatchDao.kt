package play.pmu.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import play.pmu.data.local.MatchDao
import play.pmu.data.local.MatchEntity

/**
 * Istorija partija u memoriji, za testove.
 *
 * Kao i [FakeGameResultDao]: Room DAO je vec interface, pa u testu nije potrebna
 * nikakva dodatna apstrakcija - dovoljno je napisati drugu implementaciju.
 */
class FakeMatchDao : MatchDao {

    private val matches = MutableStateFlow<List<MatchEntity>>(emptyList())
    private var nextId = 1L

    val saved: List<MatchEntity> get() = matches.value

    override suspend fun insert(match: MatchEntity): Long {
        val id = nextId++
        matches.value = matches.value + match.copy(id = id)
        return id
    }

    override fun observeRecent(limit: Int): Flow<List<MatchEntity>> =
        matches.map { list -> list.sortedByDescending { it.playedAt }.take(limit) }

    override suspend fun deleteAll() {
        matches.value = emptyList()
    }
}
