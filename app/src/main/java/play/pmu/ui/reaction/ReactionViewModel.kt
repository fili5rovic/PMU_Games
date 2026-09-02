package play.pmu.ui.reaction

import android.os.SystemClock
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
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.domain.model.GameType
import javax.inject.Inject
import kotlin.random.Random

/**
 * Faze jedne runde. Sealed interface je ovde bolji od bool-ova jer su faze
 * medjusobno isključive - kompajler u `when` proverava da nijedna nije zaboravljena.
 */
sealed interface ReactionPhase {
    /** Pocetno stanje i stanje izmedju rundi: ceka se tap. */
    data object Idle : ReactionPhase

    /** Ekran je crven, ceka se slucajno vreme do zelenog. */
    data object Waiting : ReactionPhase

    /** Ekran je zelen - meri se vreme do tapa. */
    data object Ready : ReactionPhase

    /** Tapnuto pre zelenog, runda se ponavlja. */
    data object TooSoon : ReactionPhase

    /** Svih pet rundi je odigrano, partija je upisana u bazu. */
    data class Finished(val resultId: Long) : ReactionPhase
}

data class ReactionUiState(
    val phase: ReactionPhase = ReactionPhase.Idle,
    val completedRounds: Int = 0,
    val lastTimeMs: Int? = null,
    val times: List<Int> = emptyList(),
) {
    val totalRounds: Int get() = TOTAL_ROUNDS

    companion object {
        const val TOTAL_ROUNDS = 5
    }
}

@HiltViewModel
class ReactionViewModel @Inject constructor(
    private val resultsRepository: GameResultsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState: StateFlow<ReactionUiState> = _uiState.asStateFlow()

    /** Coroutine koja ceka do zelenog; cuvamo je da bi mogla da se prekine na rani tap. */
    private var waitJob: Job? = null

    /** Trenutak kada je ekran postao zelen, po monotonom satu. */
    private var greenAtMillis = 0L

    /** Jedini ulaz iz UI-a: sve zavisi od trenutne faze. */
    fun onTap() {
        when (_uiState.value.phase) {
            ReactionPhase.Idle, ReactionPhase.TooSoon -> startWaiting()
            ReactionPhase.Waiting -> registerTooSoon()
            ReactionPhase.Ready -> registerReaction()
            is ReactionPhase.Finished -> Unit // ekran rezultata preuzima dalje
        }
    }

    private fun startWaiting() {
        _uiState.update { it.copy(phase = ReactionPhase.Waiting) }
        waitJob?.cancel()
        waitJob = viewModelScope.launch {
            delay(Random.nextLong(MIN_WAIT_MILLIS, MAX_WAIT_MILLIS))
            // SystemClock.elapsedRealtime je monoton, pa promena sistemskog vremena ne kvari merenje
            greenAtMillis = SystemClock.elapsedRealtime()
            _uiState.update { it.copy(phase = ReactionPhase.Ready) }
        }
    }

    private fun registerTooSoon() {
        waitJob?.cancel()
        _uiState.update { it.copy(phase = ReactionPhase.TooSoon) }
    }

    private fun registerReaction() {
        val elapsed = (SystemClock.elapsedRealtime() - greenAtMillis).toInt()
        val times = _uiState.value.times + elapsed
        _uiState.update {
            it.copy(
                phase = ReactionPhase.Idle,
                completedRounds = times.size,
                lastTimeMs = elapsed,
                times = times,
            )
        }
        if (times.size >= ReactionUiState.TOTAL_ROUNDS) saveResult(times)
    }

    private fun saveResult(times: List<Int>) {
        viewModelScope.launch {
            val average = times.average().toInt()
            val id = resultsRepository.save(
                GameResultEntity(
                    gameType = GameType.REACTION,
                    score = average,
                    total = times.size,
                    durationSeconds = 0,
                    playedAt = System.currentTimeMillis(),
                )
            )
            _uiState.update { it.copy(phase = ReactionPhase.Finished(id)) }
        }
    }

    private companion object {
        const val MIN_WAIT_MILLIS = 1_000L
        const val MAX_WAIT_MILLIS = 4_000L
    }
}
