package play.pmu.domain.model

import androidx.annotation.StringRes
import play.pmu.R

enum class Player(@StringRes val titleRes: Int) {
    ONE(R.string.player_one),
    TWO(R.string.player_two);

    val opponent: Player get() = if (this == ONE) TWO else ONE

    val asWinner: Winner get() = if (this == ONE) Winner.PLAYER_ONE else Winner.PLAYER_TWO
}

enum class Winner { PLAYER_ONE, PLAYER_TWO, DRAW }

data class RoundOutcome(
    val winner: Winner,
    @StringRes val detailRes: Int? = null,
    val detailValue: Int? = null,
)
