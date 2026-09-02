package play.pmu.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.GameStats
import play.pmu.domain.model.GameType
import javax.inject.Inject

data class StatisticsUiState(
    val perGame: List<GameStats> = emptyList(),
    val history: List<GameResultEntity> = emptyList(),
)

/**
 * Statistika se cita iz Room-a kao Flow, pa se ekran sam osvezava kada se
 * upise nova partija - nema rucnog ucitavanja ni "pull to refresh".
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val resultsRepository: GameResultsRepository,
) : ViewModel() {

    /** Po jedan Flow za broj partija i rekord svake igre, spojeni u jednu listu. */
    private val perGameFlow = combine(
        GameType.entries.map { gameType ->
            combine(
                resultsRepository.observePlayCount(gameType),
                resultsRepository.observeBestScore(gameType),
            ) { count, best -> GameStats(gameType, count, best) }
        }
    ) { stats -> stats.toList() }

    val uiState: StateFlow<StatisticsUiState> =
        combine(perGameFlow, resultsRepository.observeRecent()) { perGame, history ->
            StatisticsUiState(perGame = perGame, history = history)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StatisticsUiState(),
        )

    fun clearHistory() = viewModelScope.launch {
        resultsRepository.clearHistory()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
