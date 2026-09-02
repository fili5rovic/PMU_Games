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
    // v2: dodata tabela `matches` (istorija partija za dva igraca).
    // v3: dodata tabela `round_results` (pojedinacne runde mini igara).
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
