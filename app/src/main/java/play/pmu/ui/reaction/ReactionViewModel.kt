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
import play.pmu.domain.model.Player
import javax.inject.Inject
import kotlin.random.Random

/**
 * Faze jedne runde duela refleksa. Sealed interface je bolji od nekoliko
 * boolean polja jer su faze medjusobno isklucive - kompajler u `when` proverava
 * da nijedna nije zaboravljena.
 */
sealed interface ReactionPhase {

    /** Oba dela ekrana su crvena, ceka se slucajno vreme do zelenog. */
    data object Waiting : ReactionPhase

    /** Ekran je zelen - prvi tap pobedjuje. */
    data object Ready : ReactionPhase

    /**
     * Runda je odigrana. [isFalseStart] znaci da je protivnik tapnuo pre
     * zelenog, pa je [winner] dobio rundu bez tapkanja; tada je [timeMs] null.
     */
    data class Done(
        val winner: Player,
        val isFalseStart: Boolean,
        val timeMs: Int?,
    ) : ReactionPhase
}

data class ReactionUiState(
    val phase: ReactionPhase = ReactionPhase.Waiting,
)

/**
 * Duel refleksa za dva igraca na podeljenom ekranu.
 *
 * Cekanje do zelenog je `delay` u [viewModelScope], a ne blokiranje niti: UI za
 * to vreme normalno reaguje, a kada se ekran zatvori scope se otkazuje i
 * coroutine prestaje sama.
 */
@HiltViewModel
class ReactionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ReactionUiState())
    val uiState: StateFlow<ReactionUiState> = _uiState.asStateFlow()

    /** Coroutine koja ceka do zelenog; pamti se da bi mogla da se prekine na rani tap. */
    private var waitJob: Job? = null

    /** Trenutak kada je ekran postao zelen, po monotonom satu. */
    private var greenAtMillis = 0L

    init {
        startWaiting()
    }

    private fun startWaiting() {
        waitJob?.cancel()
        waitJob = viewModelScope.launch {
            delay(Random.nextLong(MIN_WAIT_MILLIS, MAX_WAIT_MILLIS))
            // SystemClock.elapsedRealtime je monoton, pa promena sistemskog
            // vremena ne moze da pokvari merenje.
            greenAtMillis = SystemClock.elapsedRealtime()
            _uiState.update { it.copy(phase = ReactionPhase.Ready) }
        }
    }

    /**
     * Jedini ulaz iz UI-a. Sta ce se desiti zavisi isklucivo od trenutne faze:
     * tap pre zelenog je pogresan start i rundu dobija protivnik, a tap na
     * zelenom pobedjuje.
     *
     * ZASTO NE MOZE DA SE UPISU DVA POBEDNIKA: Compose poziva `onClick` na glavnoj
     * niti, pa se dva "istovremena" tapa i dalje izvrsavaju jedan za drugim.
     * Prvi postavlja fazu na [ReactionPhase.Done], a drugi tada ulazi u granu
     * koja ne radi nista. Nema, dakle, prozora u kome bi oba tapa videla
     * [ReactionPhase.Ready].
     */
    fun onTap(player: Player) {
        when (val phase = _uiState.value.phase) {
            ReactionPhase.Waiting -> finish(
                winner = player.opponent,
                isFalseStart = true,
                timeMs = null,
            )

            ReactionPhase.Ready -> finish(
                winner = player,
                isFalseStart = false,
                timeMs = (SystemClock.elapsedRealtime() - greenAtMillis).toInt(),
            )

            // Runda je vec resena - drugi tap se ignorise.
            is ReactionPhase.Done -> Unit
        }
    }

    private fun finish(winner: Player, isFalseStart: Boolean, timeMs: Int?) {
        waitJob?.cancel()
        _uiState.update {
            it.copy(phase = ReactionPhase.Done(winner, isFalseStart, timeMs))
        }
    }

    private companion object {
        const val MIN_WAIT_MILLIS = 1_500L
        const val MAX_WAIT_MILLIS = 5_000L
    }
}
