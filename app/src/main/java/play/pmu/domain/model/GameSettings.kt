package play.pmu.domain.model

import androidx.annotation.StringRes
import play.pmu.R
import kotlin.random.Random

data class GameSettings(
    val ticTacToeBoardSize: BoardSizeOption = BoardSizeOption.RANDOM,
    val mathOperations: Set<MathOperation> = MathOperation.DEFAULT,
)

enum class BoardSizeOption(
    @StringRes val titleRes: Int,
    val size: Int?,
) {
    THREE(R.string.board_size_3, 3),
    FOUR(R.string.board_size_4, 4),
    FIVE(R.string.board_size_5, 5),
    RANDOM(R.string.board_size_random, null);

    fun resolve(random: Random = Random.Default): Int = size ?: FIXED_SIZES.random(random)

    companion object {
        val FIXED_SIZES: List<Int> = entries.mapNotNull { it.size }
    }
}

enum class MathOperation(val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("×"),
    DIVIDE("÷");

    companion object {
        val DEFAULT: Set<MathOperation> = entries.toSet()

        fun fromNames(names: Set<String>): Set<MathOperation> =
            names.mapNotNull { name -> entries.find { it.name == name } }
                .toSet()
                .ifEmpty { DEFAULT }
    }
}
