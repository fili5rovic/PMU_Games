package play.pmu.navigation

import kotlinx.serialization.Serializable
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MiniGame
import kotlin.random.Random

/**
 * Destinacije u aplikaciji.
 *
 * Koristi se type-safe navigacija: ruta je obican @Serializable objekat, pa
 * kompajler proverava argumente. Nema string ruta tipa "game/{id}" u kojima
 * greska u imenu argumenta puca tek u toku rada.
 *
 * Argumenti su svedeni na minimum. Ekran rezultata dobija samo [ResultRoute.resultId]
 * i sam procita partiju iz baze - liste pogodjenih i preskocenih pojmova se
 * NE prosledjuju kroz navigaciju.
 */
@Serializable
object HomeRoute

/**
 * Ugnjezdeni graf partije. Postoji da bi PartyViewModel mogao da se veze za
 * NJEGA, a ne za pojedinacnu rundu: tako jedna instanca (skor, raspored igara)
 * zivi kroz celu partiju, dok runde dolaze i prolaze.
 */
@Serializable
object PartyGraph

@Serializable
object PartyStartRoute

/**
 * Jedna runda partije. Svaka runda je SVOJA destinacija, pa dobija svoj
 * ViewModelStore: ViewModel mini igre se napravi na pocetku runde i ocisti kada
 * runda izadje sa steka. Time nema ni zaostalih coroutine-a ni prenosa stanja
 * iz prethodne runde.
 */
@Serializable
data class PartyRoundRoute(val round: Int)

@Serializable
object PartyResultRoute

/**
 * Jedna mini igra izabrana sa pocetnog ekrana, van partije.
 *
 * [game] je ime [MiniGame] konstante, a [winsOne]/[winsTwo] su broj pobeda u
 * nizu odigranih rundi. Broj runde ([attempt]) je tu da svaka nova runda bude
 * nova destinacija.
 */
@Serializable
data class SoloGameRoute(
    val game: String,
    val attempt: Int = 1,
    val winsOne: Int = 0,
    val winsTwo: Int = 0,
    /** Ko igra prvi potez, za igre sa naizmenicnim potezima. */
    val startsWithPlayerOne: Boolean = true,
)

@Serializable
object CharadesCategoriesRoute

@Serializable
data class CharadesGameRoute(val category: String)

@Serializable
object QuizCategoriesRoute

@Serializable
data class QuizGameRoute(val categoryApiId: Int)

@Serializable
data class ResultRoute(val resultId: Long)

@Serializable
object StatisticsRoute

@Serializable
object SettingsRoute

/**
 * Pocetna destinacija za igru sa kategorijama. Funkcija je na jednom mestu jer
 * je koriste i pocetni ekran i ekran rezultata (dugme "Igraj ponovo").
 */
fun GameType.startRoute(): Any = when (this) {
    GameType.CHARADES -> CharadesCategoriesRoute
    GameType.QUIZ -> QuizCategoriesRoute
}

/**
 * Kroz navigaciju ide samo ime enum konstante, ne cela vrednost.
 *
 * Pocetni igrac se izvlaci slucajno, pa ni u pojedinacnoj igri prvi potez ne
 * pripada uvek istom igracu. Pri svakoj sledecoj rundi ekran ga obrce.
 */
fun MiniGame.soloRoute(random: Random = Random.Default): SoloGameRoute = SoloGameRoute(
    game = name,
    startsWithPlayerOne = random.nextBoolean(),
)
