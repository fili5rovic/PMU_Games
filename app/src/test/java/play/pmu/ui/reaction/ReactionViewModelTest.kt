package play.pmu.ui.reaction

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.domain.model.Player
import play.pmu.fake.FakeGameClock
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class ReactionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ReactionViewModel
    private lateinit var clock: FakeGameClock

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        clock = FakeGameClock()
        viewModel = ReactionViewModel(random = Random(1), clock = clock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `runda pocinje cekanjem na zeleno`() {
        assertTrue(viewModel.uiState.value.phase is ReactionPhase.Waiting)
    }

    @Test
    fun `tokom cekanja boje se naizmenicno smenjuju pre nego sto dodje zelena`() = runTest(dispatcher) {
        val observedColors = mutableListOf<ReactionColor>()
        val initialPhase = viewModel.uiState.value.phase
        assertTrue(initialPhase is ReactionPhase.Waiting)
        observedColors.add((initialPhase as ReactionPhase.Waiting).color)

        // Pratimo faze dok ne dodje zelena (Ready)
        while (viewModel.uiState.value.phase !is ReactionPhase.Ready) {
            testScheduler.advanceTimeBy(700L)
            val currentPhase = viewModel.uiState.value.phase
            if (currentPhase is ReactionPhase.Waiting) {
                if (observedColors.lastOrNull() != currentPhase.color) {
                    observedColors.add(currentPhase.color)
                }
            }
        }

        assertEquals(ReactionPhase.Ready, viewModel.uiState.value.phase)
        // Potvrda da se vise razlicitih boja smenjivalo
        assertTrue(observedColors.size >= 2)
        // Potvrda da susedne boje nisu iste (naizmenicno)
        observedColors.zipWithNext().forEach { (prev, next) ->
            assertTrue(prev != next)
        }
    }

    @Test
    fun `runda cekanja nikada ne traje duze od 10 sekundi`() = runTest(dispatcher) {
        for (seed in 1..20) {
            val vm = ReactionViewModel(random = Random(seed), clock = clock)
            testScheduler.advanceTimeBy(ReactionViewModel.MAX_TOTAL_WAIT_MILLIS)
            assertEquals(ReactionPhase.Ready, vm.uiState.value.phase)
            assertTrue(ReactionViewModel.MAX_TOTAL_WAIT_MILLIS < 10_000L)
        }
    }

    @Test
    fun `posle slucajnog cekanja ekran postaje zelen`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertEquals(ReactionPhase.Ready, viewModel.uiState.value.phase)
    }

    @Test
    fun `prvi tap na zelenom osvaja rundu`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onTap(Player.TWO)

        val phase = viewModel.uiState.value.phase as ReactionPhase.Done
        assertEquals(Player.TWO, phase.winner)
        assertFalse(phase.isFalseStart)
    }

    @Test
    fun `izmereno vreme je razlika po satu igre`() = runTest(dispatcher) {
        advanceUntilIdle()
        clock.nowMillis += 142

        viewModel.onTap(Player.ONE)

        assertEquals(142, (viewModel.uiState.value.phase as ReactionPhase.Done).timeMs)
    }

    @Test
    fun `tap pre zelenog je pogresan start i rundu dobija protivnik`() = runTest(dispatcher) {
        viewModel.onTap(Player.ONE)

        val phase = viewModel.uiState.value.phase as ReactionPhase.Done
        assertEquals(Player.TWO, phase.winner)
        assertTrue(phase.isFalseStart)
    }

    @Test
    fun `posle pogresnog starta ekran vise ne postaje zelen`() = runTest(dispatcher) {
        viewModel.onTap(Player.ONE)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.phase is ReactionPhase.Done)
    }

    @Test
    fun `drugi tap ne moze da preuzme pobedu`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onTap(Player.ONE)
        viewModel.onTap(Player.TWO)
        viewModel.onTap(Player.TWO)
        advanceUntilIdle()

        val phase = viewModel.uiState.value.phase as ReactionPhase.Done
        assertEquals(Player.ONE, phase.winner)
    }

    @Test
    fun `pobednik pogresnog starta se ne menja daljim tapkanjem`() = runTest(dispatcher) {
        viewModel.onTap(Player.TWO)
        viewModel.onTap(Player.ONE)

        val phase = viewModel.uiState.value.phase as ReactionPhase.Done
        assertEquals(Player.ONE, phase.winner)
        assertTrue(phase.isFalseStart)
    }
}
