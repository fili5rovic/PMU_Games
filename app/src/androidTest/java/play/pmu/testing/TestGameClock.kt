package play.pmu.testing

import play.pmu.domain.util.GameClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestGameClock @Inject constructor() : GameClock {

    var nowMillis: Long = 0L

    override fun elapsedRealtimeMillis(): Long = nowMillis
}
