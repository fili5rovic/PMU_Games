package play.pmu.domain.game

/** Znak jednog igraca na tabli. */
enum class Mark { X, O }

/**
 * Pravila iks-oksa za tablu PROIZVOLJNE velicine, bez ijedne Android ili Compose
 * zavisnosti - zato se testiraju obicnim JUnit testom (vidi TicTacToeBoardTest).
 *
 * Za pobedu treba niz duzine [size]: tri u nizu na 3x3, cetiri na 4x4, pet na
 * 5x5. Zato su pobednicke linije uvek sve vrste, sve kolone i dve dijagonale -
 * jedna funkcija ([winningLines]) pokriva sve velicine, bez tri odvojene
 * implementacije.
 *
 * Tabla je immutable: [place] ne menja postojeci objekat nego vraca novu tablu.
 * Tako Compose pouzdano vidi da se state promenio, i nemoguce je slucajno
 * pokvariti staro stanje.
 */
data class TicTacToeBoard(
    val size: Int = DEFAULT_SIZE,
    val cells: List<Mark?> = List(size * size) { null },
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
     * Polja koja su dala pobedu, ili null ako pobednika nema. UI ovu liniju
     * oboji na tabli, pa je odmah jasno cime je partija dobijena.
     */
    val winningLine: List<Int>?
        get() = winningLines(size).firstOrNull { line ->
            val first = cells[line.first()]
            first != null && line.all { cells[it] == first }
        }

    /** Znak koji je napravio niz, ili null ako ga nema. */
    val winner: Mark? get() = winningLine?.let { cells[it.first()] }

    val isFull: Boolean get() = cells.none { it == null }

    /** Nereseno je samo puna tabla bez pobednika. */
    val isDraw: Boolean get() = isFull && winner == null

    companion object {
        const val DEFAULT_SIZE = 3

        /**
         * Sve vrste, sve kolone i dve dijagonale, kao indeksi u [cells].
         *
         * Posto je potreban niz duzine [size], drugih linija te duzine na tabli
         * nema - zato je ova lista potpuna za svaku velicinu.
         */
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
