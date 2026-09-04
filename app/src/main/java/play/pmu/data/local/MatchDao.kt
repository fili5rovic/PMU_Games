package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: MatchEntity): Long

    @Query("SELECT * FROM matches ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<MatchEntity>>

    @Query("DELETE FROM matches")
    suspend fun deleteAll()
}
