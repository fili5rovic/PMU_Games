package play.pmu.domain.game

enum class Mark { X, O }

data class TicTacToeBoard(
    val size: Int = DEFAULT_SIZE,
    val cells: List<Mark?> = List(size * size) { null },
) {

    fun place(index: Int, mark: Mark): TicTacToeBoard? {
        if (index !in cells.indices) return null
        if (cells[index] != null) return null
        return copy(cells = cells.toMutableList().also { it[index] = mark })
    }

    val winningLine: List<Int>?
        get() = winningLines(size).firstOrNull { line ->
            val first = cells[line.first()]
            first != null && line.all { cells[it] == first }
        }

    val winner: Mark? get() = winningLine?.let { cells[it.first()] }

    val isFull: Boolean get() = cells.none { it == null }

    val isDraw: Boolean get() = isFull && winner == null

    companion object {
        const val DEFAULT_SIZE = 3

        fun winningLines(size: Int): List<List<Int>> = buildList {
            for (row in 0 until size) {
                add((0 until size).map { column -> row * size + column })
            }
            for (column in 0 until size) {
                add((0 until size).map { row -> row * size + column })
            }
            add((0 until size).map { i -> i * size + i })
            add((0 until size).map { i -> i * size + (size - 1 - i) })
        }
    }
}
