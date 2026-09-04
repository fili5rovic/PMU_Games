package play.pmu.fake

import play.pmu.domain.util.GameClock

class FakeGameClock(var nowMillis: Long = 0L) : GameClock {
    override fun elapsedRealtimeMillis(): Long = nowMillis
}
