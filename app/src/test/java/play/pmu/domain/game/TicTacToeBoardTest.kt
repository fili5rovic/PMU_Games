package play.pmu.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testovi pravila iks-oksa. Moguci su bez emulatora jer [TicTacToeBoard] ne
 * zavisi ni od jedne Android klase.
 */
class TicTacToeBoardTest {

    /** Odigra niz poteza naizmenicno, pocevsi od [first]. */
    private fun boardOf(vararg moves: Int, first: Mark = Mark.X): TicTacToeBoard {
        var board = TicTacToeBoard()
        moves.forEachIndexed { index, cell ->
            val mark = if (index % 2 == 0) first else if (first == Mark.X) Mark.O else Mark.X
            board = requireNotNull(board.place(cell, mark))
        }
        return board
    }

    @Test
    fun `nova tabla je prazna i bez pobednika`() {
        val board = TicTacToeBoard()
        assertEquals(9, board.cells.size)
        assertTrue(board.cells.all { it == null })
        assertNull(board.winner)
        assertFalse(board.isDraw)
    }

    @Test
    fun `tri u vrsti daju pobedu`() {
        // X: 0,1,2   O: 3,4
        val board = boardOf(0, 3, 1, 4, 2)
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(0, 1, 2), board.winningLine)
    }

    @Test
    fun `tri u koloni daju pobedu`() {
        // O: 1,4,7   X: 0,2
        val board = boardOf(1, 0, 4, 2, 7, first = Mark.O)
        assertEquals(Mark.O, board.winner)
        assertEquals(listOf(1, 4, 7), board.winningLine)
    }

    @Test
    fun `tri u dijagonali daju pobedu`() {
        // X: 0,4,8   O: 1,2
        val board = boardOf(0, 1, 4, 2, 8)
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(0, 4, 8), board.winningLine)
    }

    @Test
    fun `tri u drugoj dijagonali daju pobedu`() {
        // X: 2,4,6   O: 0,1
        val board = boardOf(2, 0, 4, 1, 6)
        assertEquals(Mark.X, board.winner)
        assertEquals(listOf(2, 4, 6), board.winningLine)
    }

    @Test
    fun `puna tabla bez tri u nizu je nereseno`() {
        // X O X
        // X O O
        // O X X
        val board = boardOf(0, 1, 2, 4, 3, 5, 7, 6, 8)
        assertNull(board.winner)
        assertTrue(board.isFull)
        assertTrue(board.isDraw)
    }

    @Test
    fun `zauzeto polje se ne moze ponovo odigrati`() {
        val board = requireNotNull(TicTacToeBoard().place(4, Mark.X))
        assertNull(board.place(4, Mark.O))
    }

    @Test
    fun `polje van table se odbija`() {
        val board = TicTacToeBoard()
        assertNull(board.place(-1, Mark.X))
        assertNull(board.place(9, Mark.X))
    }

    @Test
    fun `potez ne menja postojecu tablu`() {
        val original = TicTacToeBoard()
        original.place(0, Mark.X)
        // Tabla je immutable: place vraca novu, a stara ostaje prazna.
        assertTrue(original.cells.all { it == null })
    }

    @Test
    fun `nepuna tabla bez pobednika nije nereseno`() {
        val board = boardOf(0, 1)
        assertNull(board.winner)
        assertFalse(board.isDraw)
    }
}
