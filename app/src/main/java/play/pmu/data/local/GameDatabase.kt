package play.pmu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GameResultEntity::class,
        MatchEntity::class,
        RoundResultEntity::class,
        TriviaQuestionEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameResultDao(): GameResultDao
    abstract fun matchDao(): MatchDao
    abstract fun roundResultDao(): RoundResultDao
    abstract fun triviaQuestionDao(): TriviaQuestionDao
}
