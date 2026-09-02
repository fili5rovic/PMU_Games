package play.pmu.data.repository

import kotlinx.coroutines.flow.Flow
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.GameResultEntity
import play.pmu.domain.model.GameType
import javax.inject.Inject
import javax.inject.Singleton

/** Sazeta statistika jedne igre za ekran statistike. */
data class GameStats(
    val gameType: GameType,
    val playCount: Int,
    val bestScore: Int?,
)

/**
 * Jedini put do istorije partija. ViewModel-i ne diraju DAO direktno, pa je
 * lako videti sta se sve upisuje u bazu i odakle se cita.
 */
@Singleton
class GameResultsRepository @Inject constructor(
    private val dao: GameResultDao,
) {

    /** Upisuje partiju i vraca njen id, koji se prosledjuje ekranu rezultata. */
    suspend fun save(result: GameResultEntity): Long = dao.insert(result)

    suspend fun findById(id: Long): GameResultEntity? = dao.findById(id)

    fun observeRecent(limit: Int = RECENT_LIMIT): Flow<List<GameResultEntity>> =
        dao.observeRecent(limit)

    fun observePlayCount(gameType: GameType): Flow<Int> = dao.observePlayCount(gameType)

    /**
     * "Najbolji" rezultat zavisi od igre: kod brzine reakcije i memorije trazimo
     * najmanji broj, a kod pantomime i kviza najveci.
     */
    fun observeBestScore(gameType: GameType): Flow<Int?> =
        if (gameType.lowerIsBetter) dao.observeMinScore(gameType) else dao.observeMaxScore(gameType)

    suspend fun clearHistory() = dao.deleteAll()

    private companion object {
        const val RECENT_LIMIT = 30
    }
}
