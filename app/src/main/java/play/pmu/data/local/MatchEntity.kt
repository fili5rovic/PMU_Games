package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.Winner

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playedAt: Long,
    val scoreOne: Int,
    val scoreTwo: Int,
    val winner: Winner,
    val gamesPlayed: Int,
)
