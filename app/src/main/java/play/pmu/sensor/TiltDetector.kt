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

@Singleton
class TiltDetector @Inject constructor(
    private val sensorManager: SensorManager,
) {

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

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose { sensorManager.unregisterListener(listener) }
    }

    private companion object {
        const val Z_AXIS = 2
    }
}
