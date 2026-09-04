package play.pmu.sensor

enum class TiltGesture { CORRECT, SKIP }

class TiltGestureRecognizer(
    private val triggerThreshold: Float = DEFAULT_TRIGGER_THRESHOLD,
    private val neutralThreshold: Float = DEFAULT_NEUTRAL_THRESHOLD,
    private val debounceMillis: Long = DEFAULT_DEBOUNCE_MILLIS,
) {

    private var isArmed = true
    private var lastGestureAtMillis = 0L

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

    fun reset() {
        isArmed = true
        lastGestureAtMillis = 0L
    }

    private companion object {
        const val DEFAULT_TRIGGER_THRESHOLD = 6.5f

        const val DEFAULT_NEUTRAL_THRESHOLD = 3.5f
        const val DEFAULT_DEBOUNCE_MILLIS = 500L
    }
}
