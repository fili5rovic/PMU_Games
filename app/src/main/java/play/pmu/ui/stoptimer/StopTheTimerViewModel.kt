package play.pmu.ui.stoptimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.domain.game.StopTheTimerResult
import play.pmu.domain.model.Player
import play.pmu.domain.util.GameClock
import javax.inject.Inject
import kotlin.random.Random

enum class StopTheTimerPhase { SHOWING_TARGET, RUNNING, FINISHED }

data class StopTheTimerUiState(
    val targetMillis: Int,
    val phase: StopTheTimerPhase = StopTheTimerPhase.SHOWING_TARGET,
    val stoppedOneMillis: Int? = null,
    val stoppedTwoMillis: Int? = null,
    val result: StopTheTimerResult? = null,
) {
    val targetSeconds: Int get() = targetMillis / MILLIS_IN_SECOND

    fun stoppedMillis(player: Player): Int? =
        if (player == Player.ONE) stoppedOneMillis else stoppedTwoMillis

    private companion object {
        const val MILLIS_IN_SECOND = 1_000
    }
}

@HiltViewModel
class StopTheTimerViewModel @Inject constructor(
    random: Random,
    private val clock: GameClock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StopTheTimerUiState(targetMillis = StopTheTimerResult.randomTargetMillis(random))
    )
    val uiState: StateFlow<StopTheTimerUiState> = _uiState.asStateFlow()

    private var startedAtMillis = 0L

    init {
        viewModelScope.launch {
            delay(TARGET_PREVIEW_MILLIS)
            startedAtMillis = clock.elapsedRealtimeMillis()
            _uiState.update { it.copy(phase = StopTheTimerPhase.RUNNING) }

            delay(_uiState.value.targetMillis * TIMEOUT_FACTOR + TIMEOUT_MARGIN_MILLIS)
            finishRound()
        }
    }

    fun onStop(player: Player) {
        val state = _uiState.value
        if (state.phase != StopTheTimerPhase.RUNNING) return
        if (state.stoppedMillis(player) != null) return

        val elapsed = elapsedMillis()
        _uiState.update {
            when (player) {
                Player.ONE -> it.copy(stoppedOneMillis = elapsed)
                Player.TWO -> it.copy(stoppedTwoMillis = elapsed)
            }
        }

        val updated = _uiState.value
        if (updated.stoppedOneMillis != null && updated.stoppedTwoMillis != null) finishRound()
    }

    private fun elapsedMillis(): Int = (clock.elapsedRealtimeMillis() - startedAtMillis).toInt()

    private fun finishRound() {
        val state = _uiState.value
        if (state.phase == StopTheTimerPhase.FINISHED) return

        val fallback = elapsedMillis()
        _uiState.update {
            it.copy(
                phase = StopTheTimerPhase.FINISHED,
                result = StopTheTimerResult(
                    targetMillis = it.targetMillis,
                    playerOneMillis = it.stoppedOneMillis ?: fallback,
                    playerTwoMillis = it.stoppedTwoMillis ?: fallback,
                ),
            )
        }
    }

    private companion object {
        const val TARGET_PREVIEW_MILLIS = 2_000L
        const val TIMEOUT_FACTOR = 2L
        const val TIMEOUT_MARGIN_MILLIS = 5_000L
    }
}
