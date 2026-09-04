package play.pmu.ui.solo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.repository.RoundResultsRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.GameSettings
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.party.PartyRound
import play.pmu.navigation.SoloGameRoute
import javax.inject.Inject

data class SoloGameUiState(
    val gameSettings: GameSettings = GameSettings(),
    val outcome: RoundOutcome? = null,
)

@HiltViewModel
class SoloGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    settingsRepository: SettingsRepository,
    private val roundResultsRepository: RoundResultsRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SoloGameRoute>()

    val round: PartyRound = PartyRound(
        game = MiniGame.valueOf(route.game),
        startingPlayer = if (route.startsWithPlayerOne) Player.ONE else Player.TWO,
    )

    private val _uiState = MutableStateFlow(SoloGameUiState())
    val uiState: StateFlow<SoloGameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update { it.copy(gameSettings = settings.games) }
            }
        }
    }

    fun onRoundFinished(outcome: RoundOutcome) {
        if (_uiState.value.outcome != null) return
        _uiState.update { it.copy(outcome = outcome) }
        viewModelScope.launch { roundResultsRepository.save(round.game, outcome.winner) }
    }
}
