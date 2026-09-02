package play.pmu.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import play.pmu.data.local.GameResultEntity
import play.pmu.data.repository.GameResultsRepository
import androidx.navigation.toRoute
import play.pmu.navigation.ResultRoute
import javax.inject.Inject

data class ResultUiState(
    val result: GameResultEntity? = null,
    val isPersonalBest: Boolean = false,
    val isLoading: Boolean = true,
)

/**
 * Ekran rezultata je zajednicki za sve cetiri igre.
 *
 * Iz navigacije dobija samo id partije, a sve ostalo procita iz baze. Zato kroz
 * navigaciju ne mora da se prosledjuje ni skor ni liste pojmova.
 * SavedStateHandle.toRoute() vraca type-safe rutu, bez rucnog citanja stringova.
 */
@HiltViewModel
class ResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resultsRepository: GameResultsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        val resultId = savedStateHandle.toRoute<ResultRoute>().resultId
        viewModelScope.launch {
            val result = resultsRepository.findById(resultId)
            val best = result?.let {
                resultsRepository.observeBestScore(it.gameType).first()
            }
            _uiState.value = ResultUiState(
                result = result,
                // Partija je vec upisana, pa je "najbolja" ako je jednaka rekordu.
                isPersonalBest = result != null && best != null && result.score == best,
                isLoading = false,
            )
        }
    }
}
