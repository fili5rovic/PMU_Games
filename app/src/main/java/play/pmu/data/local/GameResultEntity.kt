package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.GameType

@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: GameType,
    val score: Int,
    val total: Int,
    val durationSeconds: Int,
    val playedAt: Long,
    val correctItems: List<String> = emptyList(),
    val skippedItems: List<String> = emptyList(),
)
