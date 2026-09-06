package play.pmu.navigation

import kotlinx.serialization.Serializable
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MiniGame
import kotlin.random.Random

@Serializable
object HomeRoute


@Serializable
object PartyGraph

@Serializable
data class PartyRoundRoute(val round: Int)

@Serializable
object PartyResultRoute


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
object StepByStepGameRoute

@Serializable
data class ResultRoute(val resultId: Long)

@Serializable
object StatisticsRoute

@Serializable
object SettingsRoute


fun GameType.startRoute(): Any = when (this) {
    GameType.CHARADES -> CharadesCategoriesRoute
    GameType.QUIZ -> QuizCategoriesRoute
    GameType.STEP_BY_STEP -> StepByStepGameRoute
}

fun MiniGame.soloRoute(random: Random = Random.Default): SoloGameRoute = SoloGameRoute(
    game = name,
    startsWithPlayerOne = random.nextBoolean(),
)
