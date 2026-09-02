package play.pmu.testing

import play.pmu.domain.util.GameClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sat kojim upravlja test. Umesto cekanja, test pomeri [nowMillis] i time kaze
 * igri koliko je vremena "proslo".
 *
 * @Singleton je bitno: test i ViewModel moraju da dobiju ISTU instancu, da bi
 * pomeranje sata iz testa stiglo do igre.
 */
@Singleton
class TestGameClock @Inject constructor() : GameClock {

    var nowMillis: Long = 0L

    override fun elapsedRealtimeMillis(): Long = nowMillis
}
