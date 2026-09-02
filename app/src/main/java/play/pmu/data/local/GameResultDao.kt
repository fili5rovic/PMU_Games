package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import play.pmu.domain.model.GameType

/**
 * Upiti nad istorijom partija.
 *
 * Metode koje citaju vracaju Flow, pa se statistika sama osvezava kad se upise
 * nova partija - nema rucnog refresh-a. Metode koje pisu su suspend, jer se
 * izvrsavaju na Room-ovom pozadinskom dispatcher-u.
 */
@Dao
interface GameResultDao {

    /** Vraca id upisane partije, koji se koristi kao navigacioni argument za ekran rezultata. */
    @Insert
    suspend fun insert(result: GameResultEntity): Long

    @Query("SELECT * FROM game_results WHERE id = :id")
    suspend fun findById(id: Long): GameResultEntity?

    @Query("SELECT * FROM game_results ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<GameResultEntity>>

    @Query("SELECT COUNT(*) FROM game_results WHERE gameType = :gameType")
    fun observePlayCount(gameType: GameType): Flow<Int>

    /** Najbolji rezultat kad je veci broj bolji (pantomima, kviz). */
    @Query("SELECT MAX(score) FROM game_results WHERE gameType = :gameType")
    fun observeMaxScore(gameType: GameType): Flow<Int?>

    /** Najbolji rezultat kad je manji broj bolji (brzina reakcije, memorija). */
    @Query("SELECT MIN(score) FROM game_results WHERE gameType = :gameType")
    fun observeMinScore(gameType: GameType): Flow<Int?>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}
