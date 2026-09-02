package play.pmu.data.repository

import kotlinx.coroutines.flow.Flow
import play.pmu.data.local.MatchDao
import play.pmu.data.local.MatchEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Jedini put do istorije partija za dva igraca.
 *
 * Stoji odvojeno od [GameResultsRepository] jer cuva drugu vrstu podatka:
 * partija ima dva skora i pobednika, a ne jedan skor kao pantomima i kviz.
 */
@Singleton
class MatchRepository @Inject constructor(
    private val dao: MatchDao,
) {

    suspend fun save(match: MatchEntity): Long = dao.insert(match)

    fun observeRecent(limit: Int = RECENT_LIMIT): Flow<List<MatchEntity>> =
        dao.observeRecent(limit)

    suspend fun clearHistory() = dao.deleteAll()

    private companion object {
        const val RECENT_LIMIT = 20
    }
}
