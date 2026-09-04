package play.pmu.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import play.pmu.MainActivity
import play.pmu.data.local.GameResultEntity
import play.pmu.data.local.MatchEntity
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.MatchRepository
import play.pmu.data.repository.RoundResultsRepository
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import play.pmu.ui.PmuTestTags
import javax.inject.Inject

@HiltAndroidTest
class StatisticsRegressionTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var gameResultsRepository: GameResultsRepository

    @Inject
    lateinit var matchRepository: MatchRepository

    @Inject
    lateinit var roundResultsRepository: RoundResultsRepository

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun statistika_radi_kada_su_popunjene_sve_tabele() = runBlocking<Unit> {
        gameResultsRepository.save(
            GameResultEntity(
                gameType = GameType.CHARADES,
                score = 4,
                total = 6,
                durationSeconds = 60,
                playedAt = System.currentTimeMillis(),
                correctItems = listOf("Slon", "Pingvin"),
                skippedItems = listOf("Kamila"),
            )
        )
        matchRepository.save(
            MatchEntity(
                playedAt = System.currentTimeMillis(),
                scoreOne = 3,
                scoreTwo = 2,
                winner = Winner.PLAYER_ONE,
                gamesPlayed = 5,
            )
        )
        roundResultsRepository.save(MiniGame.TIC_TAC_TOE, Winner.PLAYER_TWO)

        openStatistics()
    }

    @Test
    fun statistika_radi_posle_odigrane_mini_igre() = runBlocking<Unit> {
        settingsRepository.setMathOperations(setOf(MathOperation.PLUS))

        composeRule.openMiniGame(MiniGame.MATH_DUEL)
        composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))
        val answer = solveMathPrompt(
            composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
        )
        composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, answer)).performClick()

        composeRule.awaitTag(PmuTestTags.ROUND_RESULT)
        composeRule.clickFirstWithTag(PmuTestTags.HOME_BUTTON)

        openStatistics()
    }

    private fun openStatistics() {
        composeRule.awaitTag(PmuTestTags.STATISTICS)
        composeRule.onNodeWithTag(PmuTestTags.STATISTICS).performClick()

        composeRule.awaitTag(PmuTestTags.STATISTICS_SCREEN)
        composeRule.onNodeWithTag(PmuTestTags.STATISTICS_SCREEN).assertIsDisplayed()
    }
}
