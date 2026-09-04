package play.pmu.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import play.pmu.data.repository.AppSettings
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.ThemeMode
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AppSettings(),
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setDynamicColor(enabled)
    }

    fun setPartyRounds(rounds: Int) = viewModelScope.launch {
        settingsRepository.setPartyRounds(rounds)
    }

    fun setTicTacToeBoardSize(option: BoardSizeOption) = viewModelScope.launch {
        settingsRepository.setTicTacToeBoardSize(option)
    }

    fun setMathOperations(operations: Set<MathOperation>) = viewModelScope.launch {
        settingsRepository.setMathOperations(operations)
    }

    fun setRoundDuration(seconds: Int) = viewModelScope.launch {
        settingsRepository.setRoundDuration(seconds)
    }

    fun setQuestionCount(count: Int) = viewModelScope.launch {
        settingsRepository.setQuestionCount(count)
    }

    fun setManualControls(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setManualCharadesControls(enabled)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
