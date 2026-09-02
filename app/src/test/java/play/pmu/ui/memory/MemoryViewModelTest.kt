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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.data.repository.GameResultsRepository
import play.pmu.domain.model.GameType
import play.pmu.fake.FakeGameResultDao

/**
 * Testovi logike igre Memorija.
 *
 * viewModelScope radi na Dispatchers.Main, koji u unit testu ne postoji - zato
 * se preko Dispatchers.setMain podmece test dispatcher. `advanceUntilIdle`
 * zatim preskace `delay` pozive, pa test ne mora stvarno da ceka.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MemoryViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var dao: FakeGameResultDao
    private lateinit var viewModel: MemoryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        dao = FakeGameResultDao()
        viewModel = MemoryViewModel(GameResultsRepository(dao))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `nova igra deli sesnaest karata u osam parova`() {
        val cards = viewModel.uiState.value.cards
        assertEquals(16, cards.size)
        assertEquals(8, viewModel.uiState.value.totalPairs)
        // Svaki simbol se pojavljuje tacno dva puta.
        assertTrue(cards.groupBy { it.symbol }.values.all { it.size == 2 })
    }

    @Test
    fun `dve iste karte ostaju otvorene i broje se kao par`() = runTest(dispatcher) {
        val (first, second) = viewModel.uiState.value.firstMatchingPair()

        viewModel.onCardClick(first)
        viewModel.onCardClick(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.matchedPairs)
        assertEquals(1, state.moves)
        assertTrue(state.cards.single { it.id == first }.isMatched)
        assertTrue(state.cards.single { it.id == second }.isMatched)
    }

    @Test
    fun `dve razlicite karte se zatvaraju i ne broje se kao par`() = runTest(dispatcher) {
        val (first, second) = viewModel.uiState.value.firstMismatchingPair()

        viewModel.onCardClick(first)
        viewModel.onCardClick(second)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.matchedPairs)
        assertEquals(1, state.moves)
        assertFalse(state.cards.single { it.id == first }.isRevealed)
        assertFalse(state.cards.single { it.id == second }.isRevealed)
    }

    @Test
    fun `ponovni klik na istu kartu se ignorise`() = runTest(dispatcher) {
        val cardId = viewModel.uiState.value.cards.first().id

        viewModel.onCardClick(cardId)
        viewModel.onCardClick(cardId)
        advanceUntilIdle()

        // Drugi klik ne sme da se protumaci kao otvaranje druge karte.
        assertEquals(0, viewModel.uiState.value.moves)
        assertEquals(1, viewModel.uiState.value.cards.count { it.isRevealed })
    }

    @Test
    fun `kada se nadju svi parovi partija se upisuje u bazu`() = runTest(dispatcher) {
        repeat(8) {
            val (first, second) = viewModel.uiState.value.firstMatchingPair()
            viewModel.onCardClick(first)
            viewModel.onCardClick(second)
            advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, dao.saved.size)
        val saved = dao.saved.single()
        assertEquals(GameType.MEMORY, saved.gameType)
        assertEquals(8, saved.total)
        assertEquals(8, saved.score) // osam poteza, bez ijedne greske
        assertEquals(state.finishedResultId, saved.id)
    }

    /** Prvi par jos neotvorenih karata sa istim simbolom. */
    private fun MemoryUiState.firstMatchingPair(): Pair<Int, Int> {
        val pair = cards.filterNot { it.isMatched }
            .groupBy { it.symbol }
            .values
            .first { it.size == 2 }
        return pair[0].id to pair[1].id
    }

    /** Dve neotvorene karte sa razlicitim simbolima. */
    private fun MemoryUiState.firstMismatchingPair(): Pair<Int, Int> {
        val first = cards.first { !it.isMatched }
        val second = cards.first { !it.isMatched && it.symbol != first.symbol }
        return first.id to second.id
    }
}
