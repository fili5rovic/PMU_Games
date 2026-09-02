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
import play.pmu.data.local.MatchEntity
import play.pmu.data.local.MiniGameStats
import play.pmu.data.local.RoundResultEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.GameStats
import play.pmu.data.repository.MatchRepository
import play.pmu.data.repository.RoundResultsRepository
import play.pmu.domain.model.GameType
import javax.inject.Inject

data class StatisticsUiState(
    /** Rekordi pantomime i kviza (igre koje daju skor). */
    val perGame: List<GameStats> = emptyList(),
    val history: List<GameResultEntity> = emptyList(),
    /** Odigrane partije za dva igraca. */
    val matches: List<MatchEntity> = emptyList(),
    /** Sazetak po mini igri: koliko je rundi odigrano i ko ih je dobijao. */
    val miniGames: List<MiniGameStats> = emptyList(),
    val rounds: List<RoundResultEntity> = emptyList(),
) {
    val isEmpty: Boolean
        get() = history.isEmpty() && matches.isEmpty() && rounds.isEmpty()
}

/**
 * Statistika se cita iz Room-a kao Flow, pa se ekran sam osvezava kada se
 * upise nova partija - nema rucnog ucitavanja ni "pull to refresh".
 *
 * Spajaju se tri izvora: partije za dva igraca, pojedinacne runde mini igara i
 * rezultati pantomime/kviza.
 *
 * Sazetak po mini igri dolazi iz GROUP BY upita nad tabelom rundi, pa se NE
 * navodi lista poznatih igara: nova mini igra ulazi u statistiku sama, samim tim
 * da je odigrana. Zato dodavanje igre ne moze da "zaboravi" statistiku.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val resultsRepository: GameResultsRepository,
    private val matchRepository: MatchRepository,
    private val roundResultsRepository: RoundResultsRepository,
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

    val uiState: StateFlow<StatisticsUiState> = combine(
        perGameFlow,
        resultsRepository.observeRecent(),
        matchRepository.observeRecent(),
        roundResultsRepository.observePerGame(),
        roundResultsRepository.observeRecent(),
    ) { perGame, history, matches, miniGames, rounds ->
        StatisticsUiState(
            perGame = perGame,
            history = history,
            matches = matches,
            miniGames = miniGames,
            rounds = rounds,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = StatisticsUiState(),
    )

    /** Brise celu istoriju - i partije, i runde, i rezultate pantomime/kviza. */
    fun clearHistory() = viewModelScope.launch {
        matchRepository.clearHistory()
        roundResultsRepository.clearHistory()
        resultsRepository.clearHistory()
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
