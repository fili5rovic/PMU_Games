package play.pmu.ui.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.local.MatchEntity
import play.pmu.data.repository.MatchRepository
import play.pmu.data.repository.RoundResultsRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.GameSettings
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.domain.party.PartyRound
import play.pmu.domain.party.buildPartySequence
import javax.inject.Inject
import kotlin.random.Random

data class PartyUiState(
    val games: List<PartyRound> = emptyList(),
    val gameSettings: GameSettings = GameSettings(),
    val roundIndex: Int = 0,
    val scoreOne: Int = 0,
    val scoreTwo: Int = 0,
    val lastOutcome: RoundOutcome? = null,
) {
    val totalRounds: Int get() = games.size
    val isReady: Boolean get() = games.isNotEmpty()
    val isFinished: Boolean get() = isReady && roundIndex >= games.size

    fun roundAt(round: Int): PartyRound? = games.getOrNull(round)

    fun isRoundOver(round: Int): Boolean = roundIndex > round

    val winner: Winner
        get() = when {
            scoreOne > scoreTwo -> Winner.PLAYER_ONE
            scoreTwo > scoreOne -> Winner.PLAYER_TWO
            else -> Winner.DRAW
        }
}

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val matchRepository: MatchRepository,
    private val roundResultsRepository: RoundResultsRepository,
    private val random: Random,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    private var hasSavedMatch = false

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { state ->
                    state.copy(
                        gameSettings = settings.games,
                        games = state.games.ifEmpty {
                            buildPartySequence(rounds = settings.partyRounds, random = random)
                        },
                    )
                }
            }
        }
    }

    fun onRoundFinished(round: Int, outcome: RoundOutcome) {
        val state = _uiState.value
        if (round != state.roundIndex) return
        val game = state.roundAt(round)?.game ?: return

        _uiState.update {
            it.copy(
                roundIndex = it.roundIndex + 1,
                scoreOne = it.scoreOne + if (outcome.winner == Winner.PLAYER_ONE) 1 else 0,
                scoreTwo = it.scoreTwo + if (outcome.winner == Winner.PLAYER_TWO) 1 else 0,
                lastOutcome = outcome,
            )
        }

        viewModelScope.launch { roundResultsRepository.save(game, outcome.winner) }
    }

    fun saveMatch() {
        val state = _uiState.value
        if (hasSavedMatch || !state.isFinished) return
        hasSavedMatch = true

        viewModelScope.launch {
            matchRepository.save(
                MatchEntity(
                    playedAt = System.currentTimeMillis(),
                    scoreOne = state.scoreOne,
                    scoreTwo = state.scoreTwo,
                    winner = state.winner,
                    gamesPlayed = state.totalRounds,
                )
            )
        }
    }
}
