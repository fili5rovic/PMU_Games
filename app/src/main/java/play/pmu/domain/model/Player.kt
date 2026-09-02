package play.pmu.domain.model

import androidx.annotation.StringRes
import play.pmu.R

/**
 * Igrac za jednom polovinom ekrana.
 *
 * Telefon lezi na stolu izmedju igraca: [ONE] sedi kod donje ivice, a [TWO] kod
 * gornje. Zato se gornja polovina ekrana crta rotirana za 180 stepeni (vidi
 * TwoPlayerLayout) - oba igraca svoj deo citaju uspravno.
 */
enum class Player(@StringRes val titleRes: Int) {
    ONE(R.string.player_one),
    TWO(R.string.player_two);

    /** Protivnik. Koristi se kod pogresnog starta: rundu dobija drugi igrac. */
    val opponent: Player get() = if (this == ONE) TWO else ONE

    val asWinner: Winner get() = if (this == ONE) Winner.PLAYER_ONE else Winner.PLAYER_TWO
}

/**
 * Ishod runde. [DRAW] postoji jer se neke igre mogu zavrsiti bez pobednika
 * (iks-oks pun bez tri u nizu, oba igraca pogresila u racunskom duelu).
 */
enum class Winner { PLAYER_ONE, PLAYER_TWO, DRAW }

/**
 * Sta je mini igra vratila onome ko je vodi (partiji ili pojedinacnoj igri).
 *
 * [detailRes] je opciono kratko objasnjenje za ekran rezultata runde, npr.
 * "142 ms" ili "8 tapkanja". Cuva se kao id resursa, a ne kao gotov tekst, da
 * ViewModel ne bi morao da drzi Context i da bi tekst ostao prevodiv.
 */
data class RoundOutcome(
    val winner: Winner,
    @StringRes val detailRes: Int? = null,
    val detailValue: Int? = null,
)
