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
            // Migracije su destruktivne: pri svakom podizanju verzije stara
            // baza se odbacuje i pravi se nova, prazna.
            //
            // To je ovde ispravan izbor, a ne lenjost. Sadrzaj tabela se u toku
            // razvoja vise puta promenio u znacenju: Brzina reakcije i Memorija
            // su bile igre za jednog igraca (prosecno vreme, broj poteza), a
            // sada su dueli, pa ti stari brojevi ne znace nista. Zapisi su
            // lokalna istorija igranja, a ne podaci koje bi bilo steta izgubiti,
            // pa pisanje prave migracije za njih ne bi imalo svrhu.
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
