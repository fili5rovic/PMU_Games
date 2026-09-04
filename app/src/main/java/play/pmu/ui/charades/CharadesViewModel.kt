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

enum class CharadesFeedback { NONE, CORRECT, SKIPPED }

data class CharadesUiState(
    val currentWord: String = "",
    val secondsLeft: Int = 0,
    val roundDurationSeconds: Int = 0,
    val correctWords: List<String> = emptyList(),
    val skippedWords: List<String> = emptyList(),
    val feedback: CharadesFeedback = CharadesFeedback.NONE,
    val showManualControls: Boolean = false,
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

    private var words: List<String> = emptyList()
    private var wordIndex = 0

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
                    showManualControls = settings.manualCharadesControls ||
                        !tiltDetector.isAvailable,
                )
            }
        }
    }

    fun startRound() {
        if (_uiState.value.isRoundStarted) return
        roundTimer.reset(roundDurationSeconds)
        _uiState.update { it.copy(isRoundStarted = true) }
        observeTimer()
        observeGestures()
    }

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

    fun clearFeedback() {
        _uiState.update { it.copy(feedback = CharadesFeedback.NONE) }
    }

    private fun advanceToNextWord() {
        wordIndex++
        if (wordIndex >= words.size) {
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
