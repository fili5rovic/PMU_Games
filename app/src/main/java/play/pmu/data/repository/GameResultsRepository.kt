package play.pmu.data.repository

import kotlinx.coroutines.flow.Flow
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType
import javax.inject.Inject
import javax.inject.Singleton

data class GameStats(
    val gameType: GameType,
    val playCount: Int,
    val bestScore: Int?,
)

@Singleton
class GameResultsRepository @Inject constructor(
    private val dao: GameResultDao,
) {

    suspend fun save(result: GameResultEntity): Long = dao.insert(result)

    suspend fun findById(id: Long): GameResultEntity? = dao.findById(id)

    fun observeRecent(limit: Int = RECENT_LIMIT): Flow<List<GameResultEntity>> =
        dao.observeRecent(limit)

    fun observePlayCount(gameType: GameType): Flow<Int> = dao.observePlayCount(gameType)

    fun observeBestScore(gameType: GameType): Flow<Int?> = dao.observeMaxScore(gameType)

    suspend fun clearHistory() = dao.deleteAll()

    private companion object {
        const val RECENT_LIMIT = 30
    }
}
