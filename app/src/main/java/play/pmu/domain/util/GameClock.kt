package play.pmu.domain.util

import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

interface GameClock {
    fun elapsedRealtimeMillis(): Long
}

@Singleton
class SystemGameClock @Inject constructor() : GameClock {
    override fun elapsedRealtimeMillis(): Long = SystemClock.elapsedRealtime()
}
