package play.pmu.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import play.pmu.data.local.GameDatabase
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.MatchDao
import play.pmu.data.local.RoundResultDao
import play.pmu.data.local.TriviaQuestionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, "pmu_games.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideGameResultDao(database: GameDatabase): GameResultDao = database.gameResultDao()

    @Provides
    fun provideMatchDao(database: GameDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideRoundResultDao(database: GameDatabase): RoundResultDao =
        database.roundResultDao()

    @Provides
    fun provideTriviaQuestionDao(database: GameDatabase): TriviaQuestionDao =
        database.triviaQuestionDao()
}
