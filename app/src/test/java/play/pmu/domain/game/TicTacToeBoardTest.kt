package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testovi pravila iks-oksa. Moguci su bez emulatora jer [TicTacToeBoard] ne
 * zavisi ni od jedne Android klase.
 *
 * Provera se radi za sve tri velicine table, jer je pobednicki niz uvek dug
 * koliko i strana table (tri na 3x3, cetiri na 4x4, pet na 5x5).
 */
class TicTacToeBoardTest {

    /** Upisuje znak direktno u polja, da test ne mora da glumi naizmenicne poteze. */
    private fun boardWith(size: Int, marks: Map<Int, Mark>): TicTacToeBoard =
        TicTacToeBoard(size = size, cells = List(size * size) { marks[it] })

    private fun lineOf(size: Int, indices: List<Int>, mark: Mark = Mark.X) =
        boardWith(size, indices.associateWith { mark })

    @Test
    fun `nova tabla je prazna i bez pobednika`() {
        val board = TicTacToeBoard()
        assertEquals(3, board.size)
        assertEquals(9, board.cells.size)
        assertTrue(board.cells.all { it == null })
        assertNull(board.winner)
        assertFalse(board.isDraw)
    }

    @Test
    fun `tabla trazene velicine ima odgovarajuci broj polja`() {
        assertEquals(16, TicTacToeBoard(size = 4).cells.size)
        assertEquals(25, TicTacToeBoard(size = 5).cells.size)
    }

    // --- 3x3 ---

    @Test
    fun `tri u vrsti pobedjuje na tabli 3x3`() {
        val board = lineOf(3, listOf(0, 1, 2))
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(0, 1, 2), board.winningLine)
    }

    @Test
    fun `tri u koloni pobedjuje na tabli 3x3`() {
        val board = lineOf(3, listOf(1, 4, 7), Mark.O)
        assertEquals(Mark.O, board.winner)
        assertEquals(listOf(1, 4, 7), board.winningLine)
    }

    @Test
    fun `tri u dijagonali pobedjuje na tabli 3x3`() {
        assertEquals(Mark.X, lineOf(3, listOf(0, 4, 8)).winner)
        assertEquals(Mark.X, lineOf(3, listOf(2, 4, 6)).winner)
    }

    @Test
    fun `dva u nizu ne pobedjuju na tabli 3x3`() {
        assertNull(lineOf(3, listOf(0, 1)).winner)
    }

    // --- 4x4 ---

    @Test
    fun `cetiri u vrsti pobedjuju na tabli 4x4`() {
        val board = lineOf(4, listOf(4, 5, 6, 7))
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(4, 5, 6, 7), board.winningLine)
    }

    @Test
    fun `cetiri u koloni pobedjuju na tabli 4x4`() {
        assertEquals(Mark.O, lineOf(4, listOf(2, 6, 10, 14), Mark.O).winner)
    }

    @Test
    fun `cetiri u dijagonali pobedjuju na tabli 4x4`() {
        assertEquals(Mark.X, lineOf(4, listOf(0, 5, 10, 15)).winner)
        assertEquals(Mark.X, lineOf(4, listOf(3, 6, 9, 12)).winner)
    }

    @Test
    fun `tri u nizu ne pobedjuju na tabli 4x4`() {
        // Na vecoj tabli tri u nizu nisu dovoljna - trazi se cetiri.
        assertNull(lineOf(4, listOf(0, 1, 2)).winner)
    }

    // --- 5x5 ---

    @Test
    fun `pet u vrsti pobedjuje na tabli 5x5`() {
        val board = lineOf(5, listOf(10, 11, 12, 13, 14))
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(10, 11, 12, 13, 14), board.winningLine)
    }

    @Test
    fun `pet u koloni pobedjuje na tabli 5x5`() {
        assertEquals(Mark.O, lineOf(5, listOf(3, 8, 13, 18, 23), Mark.O).winner)
    }

    @Test
    fun `pet u dijagonali pobedjuje na tabli 5x5`() {
        assertEquals(Mark.X, lineOf(5, listOf(0, 6, 12, 18, 24)).winner)
        assertEquals(Mark.X, lineOf(5, listOf(4, 8, 12, 16, 20)).winner)
    }

    @Test
    fun `cetiri u nizu ne pobedjuju na tabli 5x5`() {
        assertNull(lineOf(5, listOf(0, 1, 2, 3)).winner)
    }

    @Test
    fun `broj pobednickih linija je vrste plus kolone plus dve dijagonale`() {
        listOf(3, 4, 5).forEach { size ->
            assertEquals(2 * size + 2, TicTacToeBoard.winningLines(size).size)
            // Svaka linija je duga koliko i strana table.
            assertTrue(TicTacToeBoard.winningLines(size).all { it.size == size })
        }
    }

    // --- nereseno i potezi ---

    @Test
    fun `puna tabla bez niza je nereseno`() {
        // X O X
        // X O O
        // O X X
        val board = boardWith(
            3,
            mapOf(
                0 to Mark.X, 1 to Mark.O, 2 to Mark.X,
                3 to Mark.X, 4 to Mark.O, 5 to Mark.O,
                6 to Mark.O, 7 to Mark.X, 8 to Mark.X,
            ),
        )
        assertNull(board.winner)
        assertTrue(board.isFull)
        assertTrue(board.isDraw)
    }

    @Test
    fun `puna tabla sa nizom nije nereseno`() {
        val board = boardWith(
            3,
            mapOf(
                0 to Mark.X, 1 to Mark.X, 2 to Mark.X,
                3 to Mark.O, 4 to Mark.O, 5 to Mark.X,
                6 to Mark.X, 7 to Mark.O, 8 to Mark.O,
            ),
        )
        assertEquals(Mark.X, board.winner)
        assertFalse(board.isDraw)
    }

    @Test
    fun `zauzeto polje se ne moze ponovo odigrati`() {
        val board = requireNotNull(TicTacToeBoard().place(4, Mark.X))
        assertNull(board.place(4, Mark.O))
    }

    @Test
    fun `polje van table se odbija`() {
        val board = TicTacToeBoard(size = 4)
        assertNull(board.place(-1, Mark.X))
        assertNull(board.place(16, Mark.X))
        // Poslednje polje jos jeste na tabli.
        assertTrue(board.place(15, Mark.X) != null)
    }

    @Test
    fun `potez ne menja postojecu tablu`() {
        val original = TicTacToeBoard()
        original.place(0, Mark.X)
        // Tabla je immutable: place vraca novu, a stara ostaje prazna.
        assertTrue(original.cells.all { it == null })
    }
}
