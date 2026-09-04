package play.pmu.data.repository

import kotlinx.coroutines.flow.Flow
import play.pmu.data.local.MatchDao
import play.pmu.data.local.MatchEntity
import javax.inject.Inject
import javax.inject.Singleton

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
