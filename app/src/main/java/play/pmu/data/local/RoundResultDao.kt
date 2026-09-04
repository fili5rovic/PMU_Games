package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import play.pmu.domain.model.Winner

@Dao
interface RoundResultDao {

    @Insert
    suspend fun insert(result: RoundResultEntity): Long

    @Query("SELECT * FROM round_results ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RoundResultEntity>>

    @Query(
        """
        SELECT game AS game,
               COUNT(*) AS played,
               SUM(CASE WHEN winner = :playerOne THEN 1 ELSE 0 END) AS winsOne,
               SUM(CASE WHEN winner = :playerTwo THEN 1 ELSE 0 END) AS winsTwo
        FROM round_results
        GROUP BY game
        ORDER BY played DESC
        """
    )
    fun observePerGame(playerOne: Winner, playerTwo: Winner): Flow<List<MiniGameStats>>

    @Query("DELETE FROM round_results")
    suspend fun deleteAll()
}
