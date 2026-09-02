package play.pmu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [GameResultEntity::class, MatchEntity::class, TriviaQuestionEntity::class],
    // v2: dodata tabela `matches` (istorija partija za dva igraca).
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun matchDao(): MatchDao
    abstract fun triviaQuestionDao(): TriviaQuestionDao
}
