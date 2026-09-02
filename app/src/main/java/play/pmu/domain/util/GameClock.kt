package play.pmu.domain.util

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Monotoni sat koji igre koriste za merenje vremena.
 *
 * Interface postoji SAMO zbog testova: proteklo vreme je vanjska, nedeterministicka
 * vrednost, pa instrumentacioni test umesto pravog sata dobije laznu
 * implementaciju i sam zada koliko je "proslo". Bez toga bi test igre "Stani na
 * vreme" morao stvarno da ceka po nekoliko sekundi i i dalje bio nepouzdan.
 *
 * Ovo je jedan od dva takva seam-a u projektu (drugi je slucajnost). ViewModel-i,
 * repozitorijumi i ostale klase se NE skrivaju iza interface-a.
 */
interface GameClock {
    /** Milisekunde po monotonom satu - ne menja se ako se promeni sistemsko vreme. */
    fun elapsedRealtimeMillis(): Long
}

@Singleton
class SystemGameClock @Inject constructor() : GameClock {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
