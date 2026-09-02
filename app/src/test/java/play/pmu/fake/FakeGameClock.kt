package play.pmu.fake

import play.pmu.domain.util.GameClock

/**
 * Sat kojim test upravlja. Umesto cekanja, test prosto pomeri [nowMillis] i time
 * kaze igri koliko je vremena "proslo".
 */
class FakeGameClock(var nowMillis: Long = 0L) : GameClock {
    override fun elapsedRealtimeMillis(): Long = nowMillis
}
