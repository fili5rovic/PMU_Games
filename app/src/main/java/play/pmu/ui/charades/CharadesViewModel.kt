package play.pmu.ui.charades

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.CharadesWordsRepository
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.CharadesCategory
import play.pmu.domain.model.GameType
import play.pmu.navigation.CharadesGameRoute
import play.pmu.sensor.TiltDetector
import play.pmu.sensor.TiltGesture
import play.pmu.service.RoundTimer
import javax.inject.Inject

/** Kratka povratna informacija koja se prikazuje preko ekrana posle pokreta. */
enum class CharadesFeedback { NONE, CORRECT, SKIPPED }

data class CharadesUiState(
    val currentWord: String = "",
    val secondsLeft: Int = 0,
    val roundDurationSeconds: Int = 0,
    val correctWords: List<String> = emptyList(),
    val skippedWords: List<String> = emptyList(),
    val feedback: CharadesFeedback = CharadesFeedback.NONE,
    /** true kada treba prikazati dugmad Pogodak/Preskoci umesto (ili uz) senzor. */
    val showManualControls: Boolean = false,
    /** false dok se prikazuje uputstvo; tada se pokreti i odbrojavanje ignorisu. */
    val isRoundStarted: Boolean = false,
    val isRoundOver: Boolean = false,
    val finishedResultId: Long? = null,
) {
    val score: Int get() = correctWords.size
}

@HiltViewModel
class CharadesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val wordsRepository: CharadesWordsRepository,
    private val settingsRepository: SettingsRepository,
    private val resultsRepository: GameResultsRepository,
    private val tiltDetector: TiltDetector,
    private val roundTimer: RoundTimer,
) : ViewModel() {

    private val category: CharadesCategory =
        CharadesCategory.valueOf(savedStateHandle.toRoute<CharadesGameRoute>().category)

    private val _uiState = MutableStateFlow(CharadesUiState())
    val uiState: StateFlow<CharadesUiState> = _uiState.asStateFlow()

    /** Pojmovi runde i pozicija u njima. */
    private var words: List<String> = emptyList()
    private var wordIndex = 0

    /** Trajanje runde iz podesavanja; ekran ga koristi da pokrene servis. */
    var roundDurationSeconds: Int = 0
        private set

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            roundDurationSeconds = settings.roundDurationSeconds
            words = wordsRepository.shuffledWords(category)

            _uiState.update {
                it.copy(
                    currentWord = words.firstOrNull().orEmpty(),
                    roundDurationSeconds = settings.roundDurationSeconds,
                    secondsLeft = settings.roundDurationSeconds,
                    // Rucne kontrole se prikazuju kada ih korisnik izabere u podesavanjima
                    // ili kada telefon/emulator uopste nema akcelerometar.
                    showManualControls = settings.manualCharadesControls ||
                        !tiltDetector.isAvailable,
                )
            }
        }
    }

    /**
     * Poziva ekran kada se uputstvo i odbrojavanje zavrse.
     *
     * Runda NE pocinje u `init` iz dva razloga. Prvi je senzor: dok igrac
     * podize telefon do cela, akcelerometar bi lako prijavio pokret koji bi se
     * racunao kao pogodak. Drugi je odbrojavanje: [RoundTimer] je @Singleton i
     * moze da nosi `isFinished = true` iz prethodne runde, pa se ovde prvo
     * resetuje - inace bi nova runda mogla da se zavrsi u trenutku otvaranja.
     */
    fun startRound() {
        if (_uiState.value.isRoundStarted) return
        roundTimer.reset(roundDurationSeconds)
        _uiState.update { it.copy(isRoundStarted = true) }
        observeTimer()
        observeGestures()
    }

    /**
     * Odbrojavanje dolazi iz foreground servisa preko [RoundTimer]-a, pa runda
     * tece i kada ekran nije u prvom planu.
     */
    private fun observeTimer() {
        viewModelScope.launch {
            roundTimer.secondsLeft.collect { seconds ->
                _uiState.update { it.copy(secondsLeft = seconds) }
            }
        }
        viewModelScope.launch {
            roundTimer.isFinished.collect { finished ->
                if (finished && !_uiState.value.isRoundOver) finishRound()
            }
        }
    }

    /**
     * Pokreti se prate samo dok je ovaj ViewModel ziv. Kada se ekran zatvori,
     * viewModelScope se otkazuje, callbackFlow se zatvara i listener senzora se
     * odjavljuje sam - nema rucnog unregister-a koji bi mogao da se zaboravi.
     */
    private fun observeGestures() {
        viewModelScope.launch {
            tiltDetector.gestures().collect { gesture ->
                if (_uiState.value.isRoundOver) return@collect
                when (gesture) {
                    TiltGesture.CORRECT -> registerCorrect()
                    TiltGesture.SKIP -> registerSkip()
                }
            }
        }
    }

    fun registerCorrect() {
        val state = _uiState.value
        val word = state.currentWord
        if (word.isEmpty() || state.isRoundOver || !state.isRoundStarted) return
        _uiState.update {
            it.copy(correctWords = it.correctWords + word, feedback = CharadesFeedback.CORRECT)
        }
        advanceToNextWord()
    }

    fun registerSkip() {
        val state = _uiState.value
        val word = state.currentWord
        if (word.isEmpty() || state.isRoundOver || !state.isRoundStarted) return
        _uiState.update {
            it.copy(skippedWords = it.skippedWords + word, feedback = CharadesFeedback.SKIPPED)
        }
        advanceToNextWord()
    }

    /** Povratna informacija se sama sklanja, pa UI ne mora da je gasi. */
    fun clearFeedback() {
        _uiState.update { it.copy(feedback = CharadesFeedback.NONE) }
    }

    private fun advanceToNextWord() {
        wordIndex++
        if (wordIndex >= words.size) {
            // Potroseni su svi pojmovi kategorije - runda se zavrsava ranije.
            finishRound()
        } else {
            _uiState.update { it.copy(currentWord = words[wordIndex]) }
        }
    }

    private fun finishRound() {
        if (_uiState.value.isRoundOver) return
        _uiState.update { it.copy(isRoundOver = true, currentWord = "") }

        viewModelScope.launch {
            val state = _uiState.value
            val id = resultsRepository.save(
                GameResultEntity(
                    gameType = GameType.CHARADES,
                    score = state.correctWords.size,
                    total = state.correctWords.size + state.skippedWords.size,
                    durationSeconds = roundDurationSeconds,
                    playedAt = System.currentTimeMillis(),
                    correctItems = state.correctWords,
                    skippedItems = state.skippedWords,
                )
            )
            _uiState.update { it.copy(finishedResultId = id) }
        }
    }
}
