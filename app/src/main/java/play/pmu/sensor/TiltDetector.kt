package play.pmu.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Prevodi akcelerometar u Flow pokreta.
 *
 * Zasto callbackFlow: registrovanje i odjavljivanje listener-a su vezani za
 * zivot samog Flow-a. `awaitClose` se izvrsava kada se collect prekine - a to
 * se dogadja automatski kada ViewModel bude unisten ili kada ekran napusti
 * kompoziciju. Time nema nacina da listener "ostane" registrovan i prazni bateriju.
 */
@Singleton
class TiltDetector @Inject constructor(
    private val sensorManager: SensorManager,
) {

    /** Emulatori bez akcelerometra: UI na osnovu ovoga prikazuje rucnu dugmad. */
    val isAvailable: Boolean
        get() = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    fun gestures(): Flow<TiltGesture> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val recognizer = TiltGestureRecognizer()
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val z = event.values[Z_AXIS]
                recognizer.onSensorValue(z, SystemClock.elapsedRealtime())?.let { gesture ->
                    trySend(gesture)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        // SENSOR_DELAY_GAME je dovoljno cest za pokrete rukom, a ne trosi bateriju
        // kao najbrzi rezim.
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private companion object {
        const val Z_AXIS = 2
    }
}
