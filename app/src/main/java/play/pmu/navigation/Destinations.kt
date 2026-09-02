package play.pmu.navigation

import kotlinx.serialization.Serializable
import play.pmu.domain.model.GameType

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

@Serializable
object CharadesCategoriesRoute

@Serializable
data class CharadesGameRoute(val category: String)

@Serializable
object QuizCategoriesRoute

@Serializable
data class QuizGameRoute(val categoryApiId: Int)

@Serializable
object ReactionGameRoute

@Serializable
object MemoryGameRoute

@Serializable
data class ResultRoute(val resultId: Long)

@Serializable
object StatisticsRoute

@Serializable
object SettingsRoute

/**
 * Pocetna destinacija za jednu igru. Igre sa kategorijama vode na izbor
 * kategorije, ostale direktno u igru.
 *
 * Funkcija je na jednom mestu jer je koriste i pocetni ekran i ekran rezultata
 * (dugme "Igraj ponovo").
 */
fun GameType.startRoute(): Any = when (this) {
    GameType.CHARADES -> CharadesCategoriesRoute
    GameType.QUIZ -> QuizCategoriesRoute
    GameType.REACTION -> ReactionGameRoute
    GameType.MEMORY -> MemoryGameRoute
}
