package play.pmu.ui.taprace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import javax.inject.Inject

data class TapRaceUiState(
    val secondsLeft: Int = DURATION_SECONDS,
    val tapsOne: Int = 0,
    val tapsTwo: Int = 0,
    val winner: Winner? = null,
) {
    val isRunning: Boolean get() = winner == null
    val totalSeconds: Int get() = DURATION_SECONDS

    fun tapsOf(player: Player): Int =
        if (player == Player.ONE) tapsOne else tapsTwo

    companion object {
        const val DURATION_SECONDS = 5
    }
}

@HiltViewModel
class TapRaceViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TapRaceUiState())
    val uiState: StateFlow<TapRaceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            for (second in TapRaceUiState.DURATION_SECONDS - 1 downTo 0) {
                delay(TICK_MILLIS)
                _uiState.update { it.copy(secondsLeft = second) }
            }
            _uiState.update { it.copy(winner = winnerFor(it.tapsOne, it.tapsTwo)) }
        }
    }

    fun onTap(player: Player) {
        if (!_uiState.value.isRunning) return
        _uiState.update {
            it.copy(
                tapsOne = it.tapsOne + if (player == Player.ONE) 1 else 0,
                tapsTwo = it.tapsTwo + if (player == Player.TWO) 1 else 0,
            )
        }
    }

    private fun winnerFor(tapsOne: Int, tapsTwo: Int): Winner = when {
        tapsOne > tapsTwo -> Winner.PLAYER_ONE
        tapsTwo > tapsOne -> Winner.PLAYER_TWO
        else -> Winner.DRAW
    }

    private companion object {
        const val TICK_MILLIS = 1_000L
    }
}
