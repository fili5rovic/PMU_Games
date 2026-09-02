package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Upiti nad istorijom partija. Kao i u [GameResultDao], citanje vraca Flow (pa
 * se ekran statistike sam osvezi kada se upise nova partija), a pisanje je
 * suspend jer se izvrsava na Room-ovom pozadinskom dispatcher-u.
 */
@Dao
interface MatchDao {

    @Insert
    suspend fun insert(match: MatchEntity): Long

    @Query("SELECT * FROM matches ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<MatchEntity>>

    @Query("DELETE FROM matches")
    suspend fun deleteAll()
}
