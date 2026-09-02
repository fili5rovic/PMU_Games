package play.pmu.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deljeno stanje odbrojavanja izmedju [CharadesTimerService] i ViewModel-a.
 *
 * Zahvaljujuci ovom @Singleton-u nije potreban `ServiceConnection` ni Binder:
 * servis upisuje preostale sekunde, a ViewModel ih samo posmatra. Hilt garantuje
 * da su to isti objekat, jer oba dobijaju istu instancu.
 */
@Singleton
class RoundTimer @Inject constructor() {

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    /** Poziva servis na svakom otkucaju. */
    fun update(seconds: Int) {
        _secondsLeft.value = seconds
    }

    fun markFinished() {
        _secondsLeft.value = 0
        _isFinished.value = true
    }

    /** Poziva se na pocetku nove runde. */
    fun reset(seconds: Int) {
        _secondsLeft.value = seconds
        _isFinished.value = false
    }
}
