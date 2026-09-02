package play.pmu.ui.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.local.MatchEntity
import play.pmu.data.repository.MatchRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.domain.party.buildPartySequence
import javax.inject.Inject

data class PartyUiState(
    /** Slucajan raspored mini igara; prazan dok se ne procitaju podesavanja. */
    val games: List<MiniGame> = emptyList(),
    /** Redni broj runde koja se igra. Kada dostigne [totalRounds], partija je gotova. */
    val roundIndex: Int = 0,
    val scoreOne: Int = 0,
    val scoreTwo: Int = 0,
    val lastOutcome: RoundOutcome? = null,
) {
    val totalRounds: Int get() = games.size
    val isReady: Boolean get() = games.isNotEmpty()
    val isFinished: Boolean get() = isReady && roundIndex >= games.size

    /** Igra u datoj rundi, ili null ako partija jos nije spremna. */
    fun gameAt(round: Int): MiniGame? = games.getOrNull(round)

    /** true kada je runda [round] odigrana, tj. kada je skor za nju vec upisan. */
    fun isRoundOver(round: Int): Boolean = roundIndex > round

    val winner: Winner
        get() = when {
            scoreOne > scoreTwo -> Winner.PLAYER_ONE
            scoreTwo > scoreOne -> Winner.PLAYER_TWO
            else -> Winner.DRAW
        }
}

/**
 * Vodi celu partiju: raspored igara, redni broj runde i ukupan skor.
 *
 * ViewModel je vezan za UGNJEZDENI GRAF navigacije (vidi PmuNavHost), pa jedna
 * instanca zivi kroz sve runde. Same runde su odvojene destinacije, tako da
 * svaka mini igra dobija svoj ViewModel koji se ocisti kada runda prodje - o
 * tome ne mora niko rucno da vodi racuna.
 *
 * Mini igre ne znaju da partija postoji: prijave [RoundOutcome] i tu se njihov
 * posao zavrsava. Ceo tok partije je, dakle, na jednom mestu.
 */
@HiltViewModel
class PartyViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartyUiState())
    val uiState: StateFlow<PartyUiState> = _uiState.asStateFlow()

    /** Sprecava dvostruki upis partije ako se ekran rezultata ponovo iscrta. */
    private var hasSavedMatch = false

    init {
        viewModelScope.launch {
            val rounds = settingsRepository.settings.first().partyRounds
            _uiState.update { it.copy(games = buildPartySequence(rounds)) }
        }
    }

    /**
     * Zavrsetak jedne runde.
     *
     * [round] je redni broj runde koja se prijavljuje. Ako se ne poklapa sa
     * trenutnim [PartyUiState.roundIndex], runda je vec obradjena i poziv se
     * ignorise - zato se poen ne moze dodati dva puta ni kada ekran ponovo
     * udje u kompoziciju.
     */
    fun onRoundFinished(round: Int, outcome: RoundOutcome) {
        _uiState.update { state ->
            if (round != state.roundIndex) return@update state
            state.copy(
                roundIndex = state.roundIndex + 1,
                scoreOne = state.scoreOne + if (outcome.winner == Winner.PLAYER_ONE) 1 else 0,
                scoreTwo = state.scoreTwo + if (outcome.winner == Winner.PLAYER_TWO) 1 else 0,
                lastOutcome = outcome,
            )
        }
    }

    /** Upisuje odigranu partiju u istoriju. Poziva se sa ekrana rezultata, tacno jednom. */
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
