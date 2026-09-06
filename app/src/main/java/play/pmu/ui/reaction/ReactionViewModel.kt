package play.pmu.ui.reaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.domain.model.Player
import play.pmu.domain.util.GameClock
import javax.inject.Inject
import kotlin.random.Random

enum class ReactionColor {
    RED,
    BLUE,
    YELLOW,
    PURPLE,
    ORANGE,
}

sealed interface ReactionPhase {

    data class Waiting(val color: ReactionColor = ReactionColor.RED) : ReactionPhase

    data object Ready : ReactionPhase
    data class Done(
        val winner: Player,
        val isFalseStart: Boolean,
        val timeMs: Int?,
    ) : ReactionPhase
}

data class ReactionUiState(
    val phase: ReactionPhase = ReactionPhase.Waiting(),
)

@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val random: Random,
    private val clock: GameClock,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState: StateFlow<ReactionUiState> = _uiState.asStateFlow()

    private var waitJob: Job? = null

    private var greenAtMillis = 0L

    init {
        startWaiting()
    }

    private fun startWaiting() {
        waitJob?.cancel()
        waitJob = viewModelScope.launch {
            val stepsCount = random.nextInt(MIN_COLOR_STEPS, MAX_COLOR_STEPS + 1)
            var currentColor = (uiState.value.phase as? ReactionPhase.Waiting)?.color ?: ReactionColor.RED

            for (i in 0 until stepsCount) {
                val stepDelay = random.nextLong(MIN_STEP_DELAY_MILLIS, MAX_STEP_DELAY_MILLIS)
                delay(stepDelay)

                val availableColors = ReactionColor.entries.filter { it != currentColor }
                currentColor = availableColors[random.nextInt(availableColors.size)]
                _uiState.update { it.copy(phase = ReactionPhase.Waiting(currentColor)) }
            }

            val finalDelay = random.nextLong(MIN_STEP_DELAY_MILLIS, MAX_STEP_DELAY_MILLIS)
            delay(finalDelay)

            // Sat je monoton, pa promena sistemskog vremena ne moze da pokvari
            // merenje. Ide kroz GameClock da bi test mogao da zada vreme.
            greenAtMillis = clock.elapsedRealtimeMillis()
            _uiState.update { it.copy(phase = ReactionPhase.Ready) }
        }
    }

    fun onTap(player: Player) {
        when (val phase = _uiState.value.phase) {
            is ReactionPhase.Waiting -> finish(
                winner = player.opponent,
                isFalseStart = true,
                timeMs = null,
            )

            ReactionPhase.Ready -> finish(
                winner = player,
                isFalseStart = false,
                timeMs = (clock.elapsedRealtimeMillis() - greenAtMillis).toInt(),
            )

            is ReactionPhase.Done -> Unit
        }
    }

    private fun finish(winner: Player, isFalseStart: Boolean, timeMs: Int?) {
        waitJob?.cancel()
        _uiState.update {
            it.copy(phase = ReactionPhase.Done(winner, isFalseStart, timeMs))
        }
    }

    companion object {
        const val MIN_COLOR_STEPS = 1
        const val MAX_COLOR_STEPS = 4
        const val MIN_STEP_DELAY_MILLIS = 700L
        const val MAX_STEP_DELAY_MILLIS = 1_300L
        const val MAX_TOTAL_WAIT_MILLIS = (MAX_COLOR_STEPS + 1) * MAX_STEP_DELAY_MILLIS
    }
}
