package play.pmu.di

import android.content.Context
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import play.pmu.data.local.GameDatabase
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.MatchDao
import play.pmu.data.local.RoundResultDao
import play.pmu.data.local.TriviaQuestionDao
import play.pmu.domain.util.GameClock
import play.pmu.testing.TestGameClock
import java.io.File
import javax.inject.Singleton
import kotlin.random.Random

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GameDatabase =
        Room.inMemoryDatabaseBuilder(context, GameDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideGameResultDao(database: GameDatabase): GameResultDao = database.gameResultDao()

    @Provides
    fun provideMatchDao(database: GameDatabase): MatchDao = database.matchDao()

    @Provides
    fun provideRoundResultDao(database: GameDatabase): RoundResultDao = database.roundResultDao()

    @Provides
    fun provideTriviaQuestionDao(database: GameDatabase): TriviaQuestionDao =
        database.triviaQuestionDao()
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [AppModule::class])
object TestAppModule {

    @Provides
    @Singleton
    fun provideSensorManager(@ApplicationContext context: Context): SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = {
                File(context.cacheDir, "test_settings_${System.nanoTime()}.preferences_pb")
            }
        )
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [GameModule::class])
object TestGameModule {

    const val SEED = 20_260_902

    @Provides
    @Singleton
    fun provideRandom(): Random = Random(SEED)
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [ClockModule::class])
abstract class TestClockModule {

    @Binds
    abstract fun bindGameClock(clock: TestGameClock): GameClock
}
