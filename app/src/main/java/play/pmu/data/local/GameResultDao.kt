package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import play.pmu.domain.model.GameType


@Dao
interface GameResultDao {

    @Insert
    suspend fun insert(result: GameResultEntity): Long

    @Query("SELECT * FROM game_results WHERE id = :id")
    suspend fun findById(id: Long): GameResultEntity?

    @Query("SELECT * FROM game_results ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<GameResultEntity>>

    @Query("SELECT COUNT(*) FROM game_results WHERE gameType = :gameType")
    fun observePlayCount(gameType: GameType): Flow<Int>

    /** Rekord igre - u pantomimi i kvizu je to najveci skor. */
    @Query("SELECT MAX(score) FROM game_results WHERE gameType = :gameType")
    fun observeMaxScore(gameType: GameType): Flow<Int?>

    @Query("DELETE FROM game_results")
    suspend fun deleteAll()
}
