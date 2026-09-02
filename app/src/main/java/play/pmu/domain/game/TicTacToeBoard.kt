package play.pmu.domain.game

/** Znak jednog igraca na tabli. */
enum class Mark { X, O }

/**
 * Pravila iks-oksa, bez ijedne Android ili Compose zavisnosti - zato se testiraju
 * obicnim JUnit testom (vidi TicTacToeBoardTest). ViewModel samo drzi trenutnu
 * tablu i naizmenicno menja igraca.
 *
 * Tabla je immutable: [place] ne menja postojeci objekat nego vraca novu tablu.
 * Tako Compose pouzdano vidi da se state promenio, i nemoguce je slucajno
 * pokvariti staro stanje.
 */
data class TicTacToeBoard(
    val cells: List<Mark?> = List(SIZE * SIZE) { null },
) {

    /**
     * Novi potez ili `null` ako je potez nemoguc (polje van table ili zauzeto).
     * Nullable povratna vrednost je ovde dovoljna: pozivalac samo treba da zna
     * da li se potez odigrao.
     */
    fun place(index: Int, mark: Mark): TicTacToeBoard? {
        if (index !in cells.indices) return null
        if (cells[index] != null) return null
        return copy(cells = cells.toMutableList().also { it[index] = mark })
    }

    /**
     * Tri polja koja su dala pobedu, ili null ako pobednika nema. UI ovu liniju
     * oboji na tabli, pa je odmah jasno cime je partija dobijena.
     */
    val winningLine: List<Int>?
        get() = WINNING_LINES.firstOrNull { line ->
            val first = cells[line.first()]
            first != null && line.all { cells[it] == first }
        }

    /** Znak koji je napravio tri u nizu, ili null ako ga nema. */
    val winner: Mark? get() = winningLine?.let { cells[it.first()] }

    val isFull: Boolean get() = cells.none { it == null }

    /** Nereseno je samo puna tabla bez pobednika. */
    val isDraw: Boolean get() = isFull && winner == null

    companion object {
        const val SIZE = 3

        /** Tri vrste, tri kolone, dve dijagonale - kao indeksi u [cells]. */
        val WINNING_LINES: List<List<Int>> = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6),
        )
    }
}
