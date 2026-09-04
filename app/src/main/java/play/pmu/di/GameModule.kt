package play.pmu.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import play.pmu.domain.util.GameClock
import play.pmu.domain.util.SystemGameClock
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object GameModule {

    @Provides
    @Singleton
    fun provideRandom(): Random = Random.Default
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ClockModule {

    @Binds
    abstract fun bindGameClock(clock: SystemGameClock): GameClock
}
