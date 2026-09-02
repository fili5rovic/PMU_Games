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

/**
 * Testovi duela refleksa.
 *
 * viewModelScope radi na Dispatchers.Main, koji u unit testu ne postoji - zato
 * se preko Dispatchers.setMain podmece test dispatcher. Posto je
 * StandardTestDispatcher "lenj", coroutine koja ceka do zelenog se NE izvrsava
 * dok se ne pozove `advanceUntilIdle`. Zahvaljujuci tome test moze da bira da
 * li tapka pre zelenog (pogresan start) ili posle njega.
 */
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
        assertEquals(ReactionPhase.Waiting, viewModel.uiState.value.phase)
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
        // Sat je pod kontrolom testa, pa nema cekanja ni priblizne provere.
        clock.nowMillis += 142

        viewModel.onTap(Player.ONE)

        assertEquals(142, (viewModel.uiState.value.phase as ReactionPhase.Done).timeMs)
    }

    @Test
    fun `tap pre zelenog je pogresan start i rundu dobija protivnik`() = runTest(dispatcher) {
        // Bez advanceUntilIdle faza je jos Waiting.
        viewModel.onTap(Player.ONE)

        val phase = viewModel.uiState.value.phase as ReactionPhase.Done
        assertEquals(Player.TWO, phase.winner)
        assertTrue(phase.isFalseStart)
    }

    @Test
    fun `posle pogresnog starta ekran vise ne postaje zelen`() = runTest(dispatcher) {
        viewModel.onTap(Player.ONE)
        // Coroutine koja ceka do zelenog je otkazana, pa je faza i dalje Done.
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.phase is ReactionPhase.Done)
    }

    @Test
    fun `drugi tap ne moze da preuzme pobedu`() = runTest(dispatcher) {
        advanceUntilIdle()

        // Dva "istovremena" tapa: Compose ih poziva jedan za drugim na glavnoj
        // niti, pa drugi vidi da je runda vec resena.
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
