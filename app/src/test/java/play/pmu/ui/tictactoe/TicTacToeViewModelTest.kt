package play.pmu.ui.tictactoe

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import play.pmu.domain.game.Mark
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner

/**
 * Testovi iks-oksa na nivou ViewModel-a: velicina table iz podesavanja, red
 * poteza i prijava pobednika. Sama pravila su testirana u TicTacToeBoardTest.
 *
 * ViewModel ne koristi coroutine, pa nije potreban ni test dispatcher.
 */
class TicTacToeViewModelTest {

    private lateinit var viewModel: TicTacToeViewModel

    @Before
    fun setUp() {
        viewModel = TicTacToeViewModel()
    }

    @Test
    fun `pre postavljanja runde nema table`() {
        assertNull(viewModel.uiState.value.board)
        // Klik se tada tiho ignorise, ne puca.
        viewModel.onCellClick(0)
        assertNull(viewModel.uiState.value.board)
    }

    @Test
    fun `izabrana velicina table se primenjuje`() {
        listOf(
            BoardSizeOption.THREE to 3,
            BoardSizeOption.FOUR to 4,
            BoardSizeOption.FIVE to 5,
        ).forEach { (option, expected) ->
            val model = TicTacToeViewModel()
            model.startRound(option, Player.ONE)
            assertEquals(expected, model.uiState.value.board?.size)
        }
    }

    @Test
    fun `slucajna velicina table je jedna od ponudjenih`() {
        repeat(30) {
            val model = TicTacToeViewModel()
            model.startRound(BoardSizeOption.RANDOM, Player.ONE)
            val size = requireNotNull(model.uiState.value.board).size
            assertTrue("velicina $size", size in BoardSizeOption.FIXED_SIZES)
        }
    }

    @Test
    fun `slucajna velicina se izvlaci za svako pojavljivanje igre`() {
        // Svaka runda je svoj ViewModel, pa se velicine razlikuju kroz partiju.
        val sizes = (0 until 60).map {
            TicTacToeViewModel()
                .also { model -> model.startRound(BoardSizeOption.RANDOM, Player.ONE) }
                .uiState.value.board!!.size
        }.toSet()
        assertEquals(BoardSizeOption.FIXED_SIZES.toSet(), sizes)
    }

    @Test
    fun `runda pocinje zadatim igracem`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.TWO)
        assertEquals(Player.TWO, viewModel.uiState.value.currentPlayer)
    }

    @Test
    fun `pocetni igrac igra svojim znakom`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.TWO)
        viewModel.onCellClick(0)
        // Igrac 2 igra kruzicima.
        assertEquals(Mark.O, viewModel.uiState.value.board?.cells?.first())
    }

    @Test
    fun `runda se postavlja samo jednom`() {
        viewModel.startRound(BoardSizeOption.FIVE, Player.ONE)
        viewModel.onCellClick(0)
        // Ponovni poziv (npr. pri ponovnom ulasku u kompoziciju) ne sme da
        // resetuje tablu koja je u toku.
        viewModel.startRound(BoardSizeOption.THREE, Player.TWO)

        val state = viewModel.uiState.value
        assertEquals(5, state.board?.size)
        assertNotNull(state.board?.cells?.first())
    }

    @Test
    fun `potezi se naizmenicno menjaju`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.ONE)

        viewModel.onCellClick(0)
        assertEquals(Player.TWO, viewModel.uiState.value.currentPlayer)
        viewModel.onCellClick(1)
        assertEquals(Player.ONE, viewModel.uiState.value.currentPlayer)
    }

    @Test
    fun `klik na zauzeto polje ne menja red poteza`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.ONE)
        viewModel.onCellClick(0)
        viewModel.onCellClick(0)

        assertEquals(Player.TWO, viewModel.uiState.value.currentPlayer)
        assertEquals(1, viewModel.uiState.value.board?.cells?.count { it != null })
    }

    @Test
    fun `tri u nizu prijavljuje pobedu igraca na potezu`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.ONE)
        // X: 0,1,2   O: 3,4
        listOf(0, 3, 1, 4, 2).forEach(viewModel::onCellClick)

        assertEquals(Winner.PLAYER_ONE, viewModel.uiState.value.winner)
    }

    @Test
    fun `posle pobede se vise ne moze igrati`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.ONE)
        listOf(0, 3, 1, 4, 2).forEach(viewModel::onCellClick)

        val before = viewModel.uiState.value
        viewModel.onCellClick(5)
        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun `puna tabla bez niza prijavljuje nereseno`() {
        viewModel.startRound(BoardSizeOption.THREE, Player.ONE)
        // X O X / X O O / O X X
        listOf(0, 1, 2, 4, 3, 5, 7, 6, 8).forEach(viewModel::onCellClick)

        assertEquals(Winner.DRAW, viewModel.uiState.value.winner)
    }

    @Test
    fun `pobeda na tabli 4x4 trazi cetiri u nizu`() {
        viewModel.startRound(BoardSizeOption.FOUR, Player.ONE)
        // X: 0,1,2,3   O: 4,5,6
        listOf(0, 4, 1, 5, 2, 6, 3).forEach(viewModel::onCellClick)

        assertEquals(Winner.PLAYER_ONE, viewModel.uiState.value.winner)
    }

    @Test
    fun `tri u nizu na tabli 4x4 jos nije pobeda`() {
        viewModel.startRound(BoardSizeOption.FOUR, Player.ONE)
        listOf(0, 4, 1, 5, 2).forEach(viewModel::onCellClick)

        assertNull(viewModel.uiState.value.winner)
    }

    @Test
    fun `pobeda na tabli 5x5 trazi pet u nizu`() {
        viewModel.startRound(BoardSizeOption.FIVE, Player.ONE)
        // X: 0,1,2,3,4   O: 5,6,7,8
        listOf(0, 5, 1, 6, 2, 7, 3, 8, 4).forEach(viewModel::onCellClick)

        assertEquals(Winner.PLAYER_ONE, viewModel.uiState.value.winner)
    }
}
