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
        Room.databaseBuilder(context, GameDatabase::class.java, "pmu_games.db")
            // Prelaz na v2 je destruktivan: stara baza je sadrzala rezultate
            // Brzine reakcije i Memorije kao igara za JEDNOG igraca (prosecno
            // vreme, broj poteza). Te igre su sada dueli i ti brojevi vise nemaju
            // znacenje, pa nema sta da se migrira. Zapisi su lokalna istorija
            // igranja, a ne podaci koje bi bilo steta izgubiti.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideGameResultDao(database: GameDatabase): GameResultDao = database.gameResultDao()

    @Provides
    fun provideMatchDao(database: GameDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideTriviaQuestionDao(database: GameDatabase): TriviaQuestionDao =
        database.triviaQuestionDao()
}
