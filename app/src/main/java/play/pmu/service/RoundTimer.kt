package play.pmu.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoundTimer @Inject constructor() {

    private val _secondsLeft = MutableStateFlow(0)
    val secondsLeft: StateFlow<Int> = _secondsLeft.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    fun update(seconds: Int) {
        _secondsLeft.value = seconds
    }

    fun markFinished() {
        _secondsLeft.value = 0
        _isFinished.value = true
    }

    fun reset(seconds: Int) {
        _secondsLeft.value = seconds
        _isFinished.value = false
    }
}
