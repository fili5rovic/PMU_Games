package play.pmu.ui.memory

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

/**
 * Jedna kartica. Immutable je, pa se "otvaranje" radi pravljenjem kopije
 * (`copy`) unutar nove liste - Compose tako pouzdano vidi da se state promenio.
 */
data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isRevealed: Boolean = false,
    val isMatched: Boolean = false,
)

data class MemoryUiState(
    val cards: List<MemoryCard> = emptyList(),
    val moves: Int = 0,
    val matchedPairs: Int = 0,
    val finishedResultId: Long? = null,
) {
    val totalPairs: Int get() = cards.size / 2
    val isFinished: Boolean get() = cards.isNotEmpty() && matchedPairs == totalPairs
}

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val resultsRepository: GameResultsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    /** Sprecava da se treca kartica otvori dok se neuparene dve jos vracaju. */
    private var isCheckingPair = false
    private var startedAtMillis = 0L

    init {
        startNewGame()
    }

    fun startNewGame() {
        val cards = SYMBOLS
            .flatMap { symbol -> listOf(symbol, symbol) } // svaki simbol dva puta
            .shuffled()
            .mapIndexed { index, symbol -> MemoryCard(id = index, symbol = symbol) }

        isCheckingPair = false
        startedAtMillis = SystemClock.elapsedRealtime()
        _uiState.value = MemoryUiState(cards = cards)
    }

    fun onCardClick(cardId: Int) {
        val state = _uiState.value
        val card = state.cards.find { it.id == cardId } ?: return
        if (isCheckingPair || card.isRevealed || card.isMatched) return

        val cards = state.cards.map { if (it.id == cardId) it.copy(isRevealed = true) else it }
        val revealed = cards.filter { it.isRevealed && !it.isMatched }

        _uiState.update { it.copy(cards = cards) }

        if (revealed.size == 2) evaluatePair(revealed[0], revealed[1])
    }

    private fun evaluatePair(first: MemoryCard, second: MemoryCard) {
        val isMatch = first.symbol == second.symbol
        isCheckingPair = true

        viewModelScope.launch {
            if (isMatch) {
                // Kratka pauza samo da igrac vidi drugi simbol pre nego sto par ostane otvoren.
                delay(MATCH_DELAY_MILLIS)
                _uiState.update { state ->
                    state.copy(
                        cards = state.cards.map {
                            if (it.id == first.id || it.id == second.id) {
                                it.copy(isMatched = true)
                            } else {
                                it
                            }
                        },
                        moves = state.moves + 1,
                        matchedPairs = state.matchedPairs + 1,
                    )
                }
                if (_uiState.value.isFinished) saveResult()
            } else {
                delay(MISMATCH_DELAY_MILLIS)
                _uiState.update { state ->
                    state.copy(
                        cards = state.cards.map {
                            if (it.id == first.id || it.id == second.id) {
                                it.copy(isRevealed = false)
                            } else {
                                it
                            }
                        },
                        moves = state.moves + 1,
                    )
                }
            }
            isCheckingPair = false
        }
    }

    private fun saveResult() {
        viewModelScope.launch {
            val state = _uiState.value
            val elapsedSeconds =
                ((SystemClock.elapsedRealtime() - startedAtMillis) / 1000).toInt()
            val id = resultsRepository.save(
                GameResultEntity(
                    gameType = GameType.MEMORY,
                    score = state.moves,
                    total = state.totalPairs,
                    durationSeconds = elapsedSeconds,
                    playedAt = System.currentTimeMillis(),
                )
            )
            _uiState.update { it.copy(finishedResultId = id) }
        }
    }

    private companion object {
        /** Osam parova daje mrezu 4x4. Emoji se ne prevode, pa nisu u resursima. */
        val SYMBOLS = listOf("🍎", "🚀", "🐬", "⚽", "🎵", "🌵", "🔑", "⭐")
        const val MATCH_DELAY_MILLIS = 300L
        const val MISMATCH_DELAY_MILLIS = 800L
    }
}
