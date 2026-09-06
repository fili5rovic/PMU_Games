package play.pmu.ui.stepbystep

import android.content.ContextWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.StepByStepRepository
import play.pmu.domain.model.GameType
import play.pmu.domain.model.StepByStepPuzzle
import play.pmu.fake.FakeGameResultDao
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class StepByStepViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val fakeDao = FakeGameResultDao()
    private val resultsRepository = GameResultsRepository(fakeDao)

    private val testPuzzle = StepByStepPuzzle(
        solution = "Ajkula",
        clues = listOf(
            "Živi u vodi.",
            "Ima veoma razvijeno čulo mirisa.",
            "Neke vrste mogu da narastu više metara.",
            "Ima više redova zuba.",
            "Poznata je po tome što neprestano mora da pliva.",
            "Glavni je motiv u poznatom filmu „Ralje“.",
            "Pripada grupi riba grabljivica.",
        ),
    )

    private class FakeStepByStepRepository(
        private val puzzle: StepByStepPuzzle,
    ) : StepByStepRepository(ContextWrapper(null)) {
        override fun loadPuzzles(): List<StepByStepPuzzle> = listOf(puzzle)
        override fun getRandomPuzzle(random: Random): StepByStepPuzzle = puzzle
    }

    private lateinit var viewModel: StepByStepViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val fakeRepo = FakeStepByStepRepository(testPuzzle)
        viewModel = StepByStepViewModel(
            stepByStepRepository = fakeRepo,
            resultsRepository = resultsRepository,
            random = Random(1),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `igra pocinje sa prvim korakom`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(0, state.currentStepIndex)
        assertEquals(1, state.currentStepNumber)
        assertEquals(1, state.revealedClues.size)
        assertEquals("Živi u vodi.", state.revealedClues[0])
        assertEquals(30, state.currentPotentialPoints)
        assertFalse(state.isFinished)
    }

    @Test
    fun `tacan pogodak na prvom koraku donosi 30 poena i otkriva sve tragove`() = runTest(dispatcher) {
        viewModel.onGuessChanged("ajkula")
        viewModel.submitGuess()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertTrue(state.isGuessedCorrectly)
        assertEquals(30, state.score)
        assertEquals(7, state.revealedClues.size)
        assertNotNull(state.finishedResultId)

        // Provera da je rezultat upisan u bazu
        val saved = fakeDao.findById(state.finishedResultId!!)
        assertNotNull(saved)
        assertEquals(GameType.STEP_BY_STEP, saved?.gameType)
        assertEquals(30, saved?.score)
    }

    @Test
    fun `prelazak na sledece korake azurira tragove i bodove`() = runTest(dispatcher) {
        // Korak 1 -> 30 poena
        assertEquals(30, viewModel.uiState.value.currentPotentialPoints)

        // Prelazak na korak 2 -> 25 poena
        viewModel.nextClue()
        assertEquals(1, viewModel.uiState.value.currentStepIndex)
        assertEquals(2, viewModel.uiState.value.revealedClues.size)
        assertEquals(25, viewModel.uiState.value.currentPotentialPoints)

        // Prelazak na korak 3 -> 20 poena
        viewModel.nextClue()
        assertEquals(2, viewModel.uiState.value.currentStepIndex)
        assertEquals(3, viewModel.uiState.value.revealedClues.size)
        assertEquals(20, viewModel.uiState.value.currentPotentialPoints)

        // Pogodak na koraku 3
        viewModel.onGuessChanged("AJKULA")
        viewModel.submitGuess()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertTrue(state.isGuessedCorrectly)
        assertEquals(20, state.score)
    }

    @Test
    fun `pogresan unos postavlja gresku i ne prekida igru`() {
        viewModel.onGuessChanged("kit")
        viewModel.submitGuess()

        val state = viewModel.uiState.value
        assertTrue(state.isWrongGuess)
        assertFalse(state.isFinished)
    }

    @Test
    fun `otkrivanje svih 7 koraka bez tacnog odgovora zavrsava igru sa 0 poena`() = runTest(dispatcher) {
        // Idemo redom kroz sve korake do kraja
        repeat(6) {
            viewModel.nextClue()
        }
        assertEquals(6, viewModel.uiState.value.currentStepIndex)
        assertFalse(viewModel.uiState.value.isFinished)

        // Na sedmom koraku sledeci klik zavrsava igru
        viewModel.nextClue()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertFalse(state.isGuessedCorrectly)
        assertEquals(0, state.score)
        assertEquals(7, state.revealedClues.size)
        assertNotNull(state.finishedResultId)
    }

    @Test
    fun `normalizacija unosa podrzava dijakritike i praznine`() {
        assertTrue(viewModel.isCorrectGuess("  Ajkula  ", "Ajkula"))
        assertTrue(viewModel.isCorrectGuess("nikola tesla", "Nikola Tesla"))
        assertTrue(viewModel.isCorrectGuess("srek", "Šrek"))
        assertTrue(viewModel.isCorrectGuess("mona liza", "Mona Liza"))
        assertFalse(viewModel.isCorrectGuess("delfin", "Ajkula"))
    }
}
