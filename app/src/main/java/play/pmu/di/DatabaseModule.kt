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
import play.pmu.data.local.TriviaQuestionDao
import javax.inject.Singleton

/**
 * Baza se pravi na jednom mestu i deli kao @Singleton. Hilt zatim ubacuje
 * DAO-ve svuda gde su potrebni (repozitorijumi, Worker), pa nijedna klasa ne
 * mora sama da zna kako se baza otvara.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase =
        Room.databaseBuilder(context, GameDatabase::class.java, "pmu_games.db").build()

    @Provides
    fun provideGameResultDao(database: GameDatabase): GameResultDao = database.gameResultDao()

    @Provides
    fun provideTriviaQuestionDao(database: GameDatabase): TriviaQuestionDao =
        database.triviaQuestionDao()
}
