package play.pmu.sensor

/** Prepoznati pokret telefona. */
enum class TiltGesture { CORRECT, SKIP }

/**
 * Prevodi vrednosti akcelerometra u pokrete, bez ijedne Android zavisnosti -
 * zato se moze testirati obicnim JUnit testom (vidi TiltGestureRecognizerTest).
 *
 * Koristi se `z` osa: kada telefon stoji uspravno na celu, gravitacija je
 * uglavnom na `y` osi, a nagib napred/nazad je ono sto menja `z`.
 *
 * Da jedan pokret ne bi prijavio vise odgovora, kombinovane su dve zastite:
 *
 *  1. **Histereza** - posle prijavljenog pokreta sledeci je moguc samo ako se
 *     telefon prvo vrati u neutralnu zonu (|z| < [neutralThreshold]). Ovo je
 *     glavna zastita: dok igrac drzi telefon nagnut, nista se ne prijavljuje.
 *  2. **Minimalni interval** - [debounceMillis] odbacuje podrhtavanje na samoj
 *     granici praga.
 */
class TiltGestureRecognizer(
    private val triggerThreshold: Float = DEFAULT_TRIGGER_THRESHOLD,
    private val neutralThreshold: Float = DEFAULT_NEUTRAL_THRESHOLD,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) {

    /** true kada je telefon prosao prag i jos se nije vratio u neutralnu zonu. */
    private var isArmed = true
    private var lastGestureAtMillis = 0L

    /**
     * @param z vrednost `z` ose akcelerometra (m/s^2)
     * @param nowMillis trenutno vreme po monotonom satu
     * @return prepoznati pokret ili null ako se nista nije desilo
     */
    fun onSensorValue(z: Float, nowMillis: Long): TiltGesture? {
        // Telefon je u neutralnom polozaju: dozvoljavamo sledeci pokret.
        if (kotlin.math.abs(z) < neutralThreshold) {
            isArmed = true
            return null
        }

        if (!isArmed) return null
        if (kotlin.math.abs(z) < triggerThreshold) return null
        if (nowMillis - lastGestureAtMillis < debounceMillis) return null

        isArmed = false
        lastGestureAtMillis = nowMillis
        return if (z > 0) TiltGesture.CORRECT else TiltGesture.SKIP
    }

    /** Poziva se na pocetku nove runde. */
    fun reset() {
        isArmed = true
        lastGestureAtMillis = 0L
    }

    private companion object {
        /** Nagib od oko 45 stepeni; gravitacija je ~9.81 m/s^2. */
        const val DEFAULT_TRIGGER_THRESHOLD = 6.5f

        /** Mora biti izrazito manji od praga, da granicno drhtanje ne "otkljuca" gest. */
        const val DEFAULT_NEUTRAL_THRESHOLD = 3.5f
        const val DEFAULT_DEBOUNCE_MILLIS = 500L
    }
}
