package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Winner

@Entity(tableName = "round_results")
data class RoundResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game: MiniGame,
    val winner: Winner,
    val playedAt: Long,
)

/**
 * Sazetak jedne mini igre, koji Room sklapa iz GROUP BY upita (vidi
 * [RoundResultDao.observePerGame]). Nije @Entity - to je samo oblik rezultata.
 */
data class MiniGameStats(
    val game: MiniGame,
    val played: Int,
    val winsOne: Int,
    val winsTwo: Int,
)
