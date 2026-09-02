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

/**
 * Slucajnost i vreme - dve stvari koje igre koriste, a koje test mora da moze da
 * zameni.
 *
 * Zato se [Random] ne uzima kao `Random.Default` po klasama, nego se ubacuje:
 * instrumentacioni test istu ovu vezu zameni sa `Random(seed)` i tada su i
 * raspored partije i sadrzaj svake igre ponovljivi.
 */
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
