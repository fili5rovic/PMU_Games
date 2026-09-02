package play.pmu.ui.memory

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import kotlin.random.Random

/**
 * Testovi logike duela memorije.
 *
 * `advanceUntilIdle` preskace `delay` posle otvaranja dve kartice, pa test ne
 * mora stvarno da ceka da se kartice zatvore.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var viewModel: MemoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        viewModel = MemoryViewModel(Random(1))
        // Pocetnog igraca u pravoj igri postavlja raspored partije.
        viewModel.startRound(Player.ONE)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nova igra deli dvanaest karata u sest parova`() {
        val state = viewModel.uiState.value
        assertEquals(12, state.cards.size)
        assertEquals(6, state.totalPairs)
        // Svaki simbol se pojavljuje tacno dva puta.
        assertTrue(state.cards.groupBy { it.symbol }.values.all { it.size == 2 })
        assertEquals(Player.ONE, state.currentPlayer)
    }

    @Test
    fun `runda pocinje zadatim igracem`() {
        val model = MemoryViewModel(Random(2))
        model.startRound(Player.TWO)
        assertEquals(Player.TWO, model.uiState.value.currentPlayer)
    }

    @Test
    fun `poen dobija igrac koji je zaista poceo`() = runTest(dispatcher) {
        val model = MemoryViewModel(Random(3))
        model.startRound(Player.TWO)

        val pair = model.uiState.value.cards
            .groupBy { it.symbol }.values.first { it.size == 2 }
        model.onCardClick(pair[0].id)
        model.onCardClick(pair[1].id)
        advanceUntilIdle()

        assertEquals(0, model.uiState.value.scoreOne)
        assertEquals(1, model.uiState.value.scoreTwo)
    }

    @Test
    fun `par donosi poen igracu na potezu i pravo na novi potez`() = runTest(dispatcher) {
        val (first, second) = viewModel.uiState.value.firstMatchingPair()

        viewModel.onCardClick(first)
        viewModel.onCardClick(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.scoreOne)
        assertEquals(0, state.scoreTwo)
        // Par znaci da igrac ostaje na potezu.
        assertEquals(Player.ONE, state.currentPlayer)
        assertTrue(state.cards.single { it.id == first }.isMatched)
        assertTrue(state.cards.single { it.id == second }.isMatched)
    }

    @Test
    fun `promasaj zatvara kartice i predaje potez protivniku`() = runTest(dispatcher) {
        val (first, second) = viewModel.uiState.value.firstMismatchingPair()

        viewModel.onCardClick(first)
        viewModel.onCardClick(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.scoreOne)
        assertEquals(0, state.scoreTwo)
        assertEquals(Player.TWO, state.currentPlayer)
        assertFalse(state.cards.single { it.id == first }.isRevealed)
        assertFalse(state.cards.single { it.id == second }.isRevealed)
    }

    @Test
    fun `poen se upisuje igracu koji je bio na potezu`() = runTest(dispatcher) {
        // Prvi igrac promasi, pa potez preuzima drugi.
        val (missOne, missTwo) = viewModel.uiState.value.firstMismatchingPair()
        viewModel.onCardClick(missOne)
        viewModel.onCardClick(missTwo)
        advanceUntilIdle()

        val (first, second) = viewModel.uiState.value.firstMatchingPair()
        viewModel.onCardClick(first)
        viewModel.onCardClick(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.scoreOne)
        assertEquals(1, state.scoreTwo)
    }

    @Test
    fun `ponovni klik na istu kartu se ignorise`() = runTest(dispatcher) {
        val cardId = viewModel.uiState.value.cards.first().id

        viewModel.onCardClick(cardId)
        viewModel.onCardClick(cardId)
        advanceUntilIdle()

        // Drugi klik ne sme da se protumaci kao otvaranje druge karte.
        assertEquals(1, viewModel.uiState.value.cards.count { it.isRevealed })
        assertNull(viewModel.uiState.value.winner)
    }

    @Test
    fun `treca kartica se ne otvara dok se par proverava`() = runTest(dispatcher) {
        val (first, second) = viewModel.uiState.value.firstMismatchingPair()
        viewModel.onCardClick(first)
        viewModel.onCardClick(second)

        // Jos se ceka zatvaranje: klik na trecu karticu se odbija.
        val third = viewModel.uiState.value.cards
            .first { it.id != first && it.id != second }
            .id
        viewModel.onCardClick(third)

        assertFalse(viewModel.uiState.value.cards.single { it.id == third }.isRevealed)
        advanceUntilIdle()
    }

    @Test
    fun `kada se nadju svi parovi pobedjuje igrac sa vise parova`() = runTest(dispatcher) {
        // Prvi igrac nalazi sve parove, jer par donosi pravo na novi potez.
        repeat(6) {
            val (first, second) = viewModel.uiState.value.firstMatchingPair()
            viewModel.onCardClick(first)
            viewModel.onCardClick(second)
            advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertEquals(6, state.foundPairs)
        assertEquals(6, state.scoreOne)
        assertEquals(Winner.PLAYER_ONE, state.winner)
    }

    @Test
    fun `posle zavrsene igre klikovi se ignorisu`() = runTest(dispatcher) {
        repeat(6) {
            val (first, second) = viewModel.uiState.value.firstMatchingPair()
            viewModel.onCardClick(first)
            viewModel.onCardClick(second)
            advanceUntilIdle()
        }

        val before = viewModel.uiState.value
        viewModel.onCardClick(before.cards.first().id)
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value)
    }

    /** Prvi par jos neuparenih karata sa istim simbolom. */
    private fun MemoryUiState.firstMatchingPair(): Pair<Int, Int> {
        val pair = cards.filterNot { it.isMatched }
            .groupBy { it.symbol }
            .values
            .first { it.size == 2 }
        return pair[0].id to pair[1].id
    }

    /** Dve neuparene karte sa razlicitim simbolima. */
    private fun MemoryUiState.firstMismatchingPair(): Pair<Int, Int> {
        val first = cards.first { !it.isMatched }
        val second = cards.first { !it.isMatched && it.symbol != first.symbol }
        return first.id to second.id
    }
}
