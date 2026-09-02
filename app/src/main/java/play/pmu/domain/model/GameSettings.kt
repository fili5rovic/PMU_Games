package play.pmu.domain.model

import androidx.annotation.StringRes
import play.pmu.R
import kotlin.random.Random

/**
 * Podesavanja pojedinih igara, koja igraci menjaju pre pocetka partije.
 *
 * Ovde su SAMO opcije koje stvarno menjaju igru. Igre koje nemaju sta da podese
 * (refleks, trka tapkanja, stani na vreme, binarno u decimalno) se ovde ne
 * pominju i ne dobijaju prazna polja.
 *
 * Struktura je namerno plitka: dve vrednosti ne zasluzuju po jednu klasu, a
 * `settings.games.ticTacToeBoardSize` se cita bez razmisljanja.
 */
data class GameSettings(
    val ticTacToeBoardSize: BoardSizeOption = BoardSizeOption.RANDOM,
    val mathOperations: Set<MathOperation> = MathOperation.DEFAULT,
)

/**
 * Velicina table u iks-oksu.
 *
 * [RANDOM] se NE resava kada partija pocne, nego pri svakom pojavljivanju igre -
 * zato [size] ostaje null, a [resolve] se poziva iz ViewModel-a runde. Posto je
 * svaka runda svoja destinacija sa svojim ViewModel-om, tri pojavljivanja
 * iks-oksa u istoj partiji dobiju tri nezavisno izvucene velicine.
 */
enum class BoardSizeOption(
    @StringRes val titleRes: Int,
    /** Fiksna velicina, ili null za [RANDOM]. */
    val size: Int?,
) {
    THREE(R.string.board_size_3, 3),
    FOUR(R.string.board_size_4, 4),
    FIVE(R.string.board_size_5, 5),
    RANDOM(R.string.board_size_random, null);

    fun resolve(random: Random = Random.Default): Int = size ?: FIXED_SIZES.random(random)

    companion object {
        /** Velicine koje [RANDOM] moze da izvuce. */
        val FIXED_SIZES: List<Int> = entries.mapNotNull { it.size }
    }
}

/**
 * Racunske operacije koje racunski duel sme da postavi.
 *
 * Simbol je i oznaka na dugmetu: matematicki znakovi se ne prevode, pa ne moraju
 * u resurse.
 */
enum class MathOperation(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("×"),
    DIVIDE("÷");

    companion object {
        /** Podrazumevano su ukljucene sve operacije. */
        val DEFAULT: Set<MathOperation> = entries.toSet()

        /**
         * Cita skup iz sacuvanih imena. Nepoznata imena se preskacu, a prazan
         * rezultat se vraca na podrazumevani - najmanje jedna operacija mora da
         * ostane ukljucena da bi igra imala sta da postavi.
         */
        fun fromNames(names: Set<String>): Set<MathOperation> =
            names.mapNotNull { name -> entries.find { it.name == name } }
                .toSet()
                .ifEmpty { DEFAULT }
    }
}
