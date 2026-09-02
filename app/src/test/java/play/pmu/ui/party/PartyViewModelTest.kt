package play.pmu.ui.party

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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.data.repository.MatchRepository
import play.pmu.data.repository.RoundResultsRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.RoundOutcome
import play.pmu.domain.model.Winner
import play.pmu.fake.FakeMatchDao
import play.pmu.fake.FakeRoundResultDao
import play.pmu.fake.FakePreferencesDataStore

/**
 * Testovi toka partije: raspored igara, napredovanje po rundama, skor i upis u
 * istoriju.
 *
 * Koristi se PRAVI SettingsRepository nad fake DataStore-om, pa se testira
 * tacno onaj kod koji radi i u aplikaciji.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PartyViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var matchDao: FakeMatchDao
    private lateinit var roundDao: FakeRoundResultDao

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        settingsRepository = SettingsRepository(FakePreferencesDataStore())
        matchDao = FakeMatchDao()
        roundDao = FakeRoundResultDao()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Partija sa zadatim brojem rundi, spremna za igru.
     *
     * `scheduler.advanceUntilIdle` izvrsava coroutine iz `init` (citanje
     * podesavanja i pravljenje rasporeda igara) bez stvarnog cekanja.
     */
    private suspend fun startedParty(rounds: Int): PartyViewModel {
        settingsRepository.setPartyRounds(rounds)
        val viewModel = PartyViewModel(
            settingsRepository = settingsRepository,
            matchRepository = MatchRepository(matchDao),
            roundResultsRepository = RoundResultsRepository(roundDao),
        )
        dispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    /** Odigra jednu rundu u korist zadatog pobednika. */
    private fun PartyViewModel.playRound(round: Int, winner: Winner) {
        onRoundFinished(round, RoundOutcome(winner = winner))
    }

    @Test
    fun `partija se pravi sa brojem rundi iz podesavanja`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 5)

        val state = viewModel.uiState.value
        assertEquals(5, state.totalRounds)
        assertTrue(state.isReady)
        assertFalse(state.isFinished)
        assertEquals(0, state.roundIndex)
    }

    @Test
    fun `ista igra se ne ponavlja dva puta zaredom`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 9)

        viewModel.uiState.value.games.map { it.game }.zipWithNext().forEach { (current, next) ->
            assertNotEquals(current, next)
        }
    }

    @Test
    fun `zavrsena runda pomera partiju i dodaje poen pobedniku`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 5)

        viewModel.playRound(round = 0, winner = Winner.PLAYER_ONE)

        val state = viewModel.uiState.value
        assertEquals(1, state.roundIndex)
        assertEquals(1, state.scoreOne)
        assertEquals(0, state.scoreTwo)
    }

    @Test
    fun `nereseno ne donosi poen nikome`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 5)

        viewModel.playRound(round = 0, winner = Winner.DRAW)

        val state = viewModel.uiState.value
        assertEquals(1, state.roundIndex)
        assertEquals(0, state.scoreOne)
        assertEquals(0, state.scoreTwo)
    }

    @Test
    fun `ponovna prijava iste runde se ignorise`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 5)

        viewModel.playRound(round = 0, winner = Winner.PLAYER_ONE)
        // Isti poziv jos dva puta - poen sme da se doda tacno jednom.
        viewModel.playRound(round = 0, winner = Winner.PLAYER_ONE)
        viewModel.playRound(round = 0, winner = Winner.PLAYER_TWO)

        val state = viewModel.uiState.value
        assertEquals(1, state.roundIndex)
        assertEquals(1, state.scoreOne)
        assertEquals(0, state.scoreTwo)
    }

    @Test
    fun `partija se zavrsava posle poslednje runde i ima pobednika`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 3)

        viewModel.playRound(0, Winner.PLAYER_TWO)
        viewModel.playRound(1, Winner.PLAYER_ONE)
        viewModel.playRound(2, Winner.PLAYER_TWO)

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(1, state.scoreOne)
        assertEquals(2, state.scoreTwo)
        assertEquals(Winner.PLAYER_TWO, state.winner)
    }

    @Test
    fun `jednak skor daje neresenu partiju`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 2)

        viewModel.playRound(0, Winner.PLAYER_ONE)
        viewModel.playRound(1, Winner.PLAYER_TWO)

        assertEquals(Winner.DRAW, viewModel.uiState.value.winner)
    }

    @Test
    fun `zavrsena partija se upisuje u istoriju tacno jednom`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 2)
        viewModel.playRound(0, Winner.PLAYER_ONE)
        viewModel.playRound(1, Winner.PLAYER_ONE)

        // Ekran rezultata moze da se iscrta vise puta.
        viewModel.saveMatch()
        viewModel.saveMatch()
        advanceUntilIdle()

        assertEquals(1, matchDao.saved.size)
        val match = matchDao.saved.single()
        assertEquals(2, match.scoreOne)
        assertEquals(0, match.scoreTwo)
        assertEquals(Winner.PLAYER_ONE, match.winner)
        assertEquals(2, match.gamesPlayed)
    }

    @Test
    fun `svaka odigrana runda se upisuje u statistiku mini igara`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 3)
        val games = viewModel.uiState.value.games

        viewModel.playRound(0, Winner.PLAYER_ONE)
        viewModel.playRound(1, Winner.DRAW)
        viewModel.playRound(2, Winner.PLAYER_TWO)
        advanceUntilIdle()

        assertEquals(3, roundDao.saved.size)
        assertEquals(games.map { it.game }, roundDao.saved.map { it.game })
        assertEquals(
            listOf(Winner.PLAYER_ONE, Winner.DRAW, Winner.PLAYER_TWO),
            roundDao.saved.map { it.winner },
        )
    }

    @Test
    fun `ponovna prijava runde se ne upisuje drugi put`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 3)

        viewModel.playRound(0, Winner.PLAYER_ONE)
        viewModel.playRound(0, Winner.PLAYER_ONE)
        advanceUntilIdle()

        assertEquals(1, roundDao.saved.size)
    }

    @Test
    fun `podesavanja igara su dostupna rundama`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 3)

        viewModel.setTicTacToeBoardSize(BoardSizeOption.FIVE)
        viewModel.setMathOperations(setOf(MathOperation.PLUS))
        advanceUntilIdle()

        val settings = viewModel.uiState.value.gameSettings
        assertEquals(BoardSizeOption.FIVE, settings.ticTacToeBoardSize)
        assertEquals(setOf(MathOperation.PLUS), settings.mathOperations)
    }

    @Test
    fun `promena podesavanja ne premesa vec napravljen raspored`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 5)
        val before = viewModel.uiState.value.games

        viewModel.setTicTacToeBoardSize(BoardSizeOption.THREE)
        advanceUntilIdle()

        assertEquals(before, viewModel.uiState.value.games)
    }

    @Test
    fun `nedovrsena partija se ne upisuje u istoriju`() = runTest(dispatcher) {
        val viewModel = startedParty(rounds = 3)
        viewModel.playRound(0, Winner.PLAYER_ONE)

        viewModel.saveMatch()
        advanceUntilIdle()

        assertTrue(matchDao.saved.isEmpty())
    }
}
