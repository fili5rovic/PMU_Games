package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import play.pmu.domain.model.Winner

/**
 * Upiti nad odigranim rundama mini igara.
 *
 * Statistika po igri se racuna U BAZI (GROUP BY), a ne u Kotlinu: tako se ne
 * cita cela istorija samo da bi se prebrojala. Rezultat Room sklapa u
 * [MiniGameStats].
 */
@Dao
interface RoundResultDao {

    @Insert
    suspend fun insert(result: RoundResultEntity): Long

    @Query("SELECT * FROM round_results ORDER BY playedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RoundResultEntity>>

    /**
     * Broj odigranih rundi i pobeda po igri.
     *
     * [playerOne] i [playerTwo] se prosledjuju kao parametri (Room ih pretvara
     * preko [Converters]) da imena konstanti ne bi bila upisana u SQL kao
     * tekst - preimenovanje enum-a bi tada tiho pokvarilo brojanje.
     *
     * Upit se NE poziva sa listom poznatih igara, pa nova mini igra ulazi u
     * statistiku sama, bez izmene ovog koda.
     */
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
