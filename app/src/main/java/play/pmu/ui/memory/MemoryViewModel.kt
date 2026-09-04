package play.pmu.ui.memory

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
import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val symbol: String,
    val isRevealed: Boolean = false,
    val isMatched: Boolean = false,
)

data class MemoryUiState(
    val cards: List<MemoryCard> = emptyList(),
    val currentPlayer: Player = Player.ONE,
    val scoreOne: Int = 0,
    val scoreTwo: Int = 0,
    /** null dok se svi parovi ne nadju. */
    val winner: Winner? = null,
) {
    val totalPairs: Int get() = cards.size / 2
    val foundPairs: Int get() = scoreOne + scoreTwo

    fun scoreOf(player: Player): Int =
        if (player == Player.ONE) scoreOne else scoreTwo
}

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    private var isCheckingPair = false

    private var isConfigured = false

    init {
        val cards = SYMBOLS
            .flatMap { symbol -> listOf(symbol, symbol) }
            .shuffled(random)
            .mapIndexed { index, symbol -> MemoryCard(id = index, symbol = symbol) }
        _uiState.value = MemoryUiState(cards = cards)
    }

    fun startRound(startingPlayer: Player) {
        if (isConfigured) return
        isConfigured = true
        _uiState.update { it.copy(currentPlayer = startingPlayer) }
    }

    fun onCardClick(cardId: Int) {
        val state = _uiState.value
        if (state.winner != null || isCheckingPair) return

        val card = state.cards.find { it.id == cardId } ?: return
        if (card.isRevealed || card.isMatched) return

        val cards = state.cards.map { if (it.id == cardId) it.copy(isRevealed = true) else it }
        _uiState.update { it.copy(cards = cards) }

        val revealed = cards.filter { it.isRevealed && !it.isMatched }
        if (revealed.size == 2) evaluatePair(revealed[0], revealed[1])
    }

    private fun evaluatePair(first: MemoryCard, second: MemoryCard) {
        val isMatch = first.symbol == second.symbol
        isCheckingPair = true

        viewModelScope.launch {
            delay(if (isMatch) MATCH_DELAY_MILLIS else MISMATCH_DELAY_MILLIS)

            _uiState.update { state ->
                val scoringPlayer = state.currentPlayer
                if (isMatch) {
                    state.copy(
                        cards = state.cards.map { card ->
                            if (card.id == first.id || card.id == second.id) {
                                card.copy(isMatched = true)
                            } else {
                                card
                            }
                        },
                        scoreOne = state.scoreOne + if (scoringPlayer == Player.ONE) 1 else 0,
                        scoreTwo = state.scoreTwo + if (scoringPlayer == Player.TWO) 1 else 0,
                    )
                } else {
                    state.copy(
                        cards = state.cards.map { card ->
                            if (card.id == first.id || card.id == second.id) {
                                card.copy(isRevealed = false)
                            } else {
                                card
                            }
                        },
                        currentPlayer = state.currentPlayer.opponent,
                    )
                }
            }

            _uiState.update { state ->
                if (state.foundPairs == state.totalPairs) {
                    state.copy(winner = winnerFor(state.scoreOne, state.scoreTwo))
                } else {
                    state
                }
            }

            isCheckingPair = false
        }
    }

    private fun winnerFor(scoreOne: Int, scoreTwo: Int): Winner = when {
        scoreOne > scoreTwo -> Winner.PLAYER_ONE
        scoreTwo > scoreOne -> Winner.PLAYER_TWO
        else -> Winner.DRAW
    }

    private companion object {
        val SYMBOLS = listOf("🍎", "🚀", "🐬", "⚽", "🎵", "🌵")
        const val MATCH_DELAY_MILLIS = 400L
        const val MISMATCH_DELAY_MILLIS = 900L
    }
}
