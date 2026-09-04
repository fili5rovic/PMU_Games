package play.pmu.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import play.pmu.MainActivity
import play.pmu.R
import play.pmu.data.repository.SettingsRepository
import play.pmu.di.TestGameModule
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import play.pmu.domain.party.PartyRound
import play.pmu.domain.party.buildPartySequence
import play.pmu.testing.TestGameClock
import play.pmu.ui.PmuTestTags
import javax.inject.Inject
import kotlin.random.Random

@HiltAndroidTest
class PartyFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var clock: TestGameClock

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun string(id: Int, vararg args: Any): String = context.getString(id, *args)

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun partija_se_odigra_do_kraja_i_prikaze_pobednika() = runBlocking<Unit> {
        settingsRepository.setPartyRounds(PARTY_ROUNDS)
        settingsRepository.setTicTacToeBoardSize(BoardSizeOption.THREE)
        settingsRepository.setMathOperations(setOf(MathOperation.PLUS))

        val expectedRounds = buildPartySequence(
            rounds = PARTY_ROUNDS,
            random = Random(TestGameModule.SEED),
        )

        composeRule.awaitTag(PmuTestTags.START_PARTY)
        composeRule.onNodeWithTag(PmuTestTags.START_PARTY).performClick()

        expectedRounds.forEach { round -> playRoundAsPlayerOne(round) }

        composeRule.awaitTag(PmuTestTags.PARTY_RESULT)
        composeRule.onAllNodesWithTag(PmuTestTags.PARTY_RESULT)[0].assertIsDisplayed()
        composeRule.awaitText(string(R.string.party_winner, string(R.string.player_one)))

        composeRule.clickFirstWithTag(PmuTestTags.HOME_BUTTON)
        composeRule.awaitTag(PmuTestTags.START_PARTY)
    }

    @Test
    fun raspored_partije_ne_ponavlja_igru_dva_puta_zaredom() {
        val rounds = buildPartySequence(rounds = 9, random = Random(TestGameModule.SEED))
        rounds.map { it.game }.zipWithNext().forEach { (current, next) ->
            assertEquals(false, current == next)
        }
    }

    private fun playRoundAsPlayerOne(round: PartyRound) {
        when (round.game) {
            MiniGame.REACTION -> {
                composeRule.awaitTag(PmuTestTags.reactionHalf(Player.ONE.name))
                composeRule.awaitText(string(R.string.reaction_tap_now))
                composeRule.onNodeWithTag(PmuTestTags.reactionHalf(Player.ONE.name)).performClick()
            }

            MiniGame.TIC_TAC_TOE -> {
                composeRule.awaitTag(PmuTestTags.ticTacToeCell(0, 0))
                listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1, 0 to 2).forEach { (row, column) ->
                    composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(row, column)).performClick()
                }
            }

            MiniGame.MEMORY -> {
                composeRule.awaitTag(PmuTestTags.memoryCard(0))
                composeRule.playMemoryRound(
                    yourTurnText = string(R.string.tictactoe_your_turn),
                    cardCount = MEMORY_CARDS,
                )
            }

            MiniGame.TAP_RACE -> {
                composeRule.awaitTag(PmuTestTags.playerArea(Player.ONE.name))
                repeat(TAP_RACE_TAPS) {
                    composeRule.onNodeWithTag(PmuTestTags.playerArea(Player.ONE.name))
                        .performClick()
                }
            }

            MiniGame.MATH_DUEL -> {
                composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))
                val answer = solveMathPrompt(
                    composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
                )
                composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, answer))
                    .performClick()
            }

            MiniGame.STOP_THE_TIMER -> {
                composeRule.awaitTag(PmuTestTags.stopTarget(Player.ONE.name))
                val targetMillis =
                    composeRule.textOfTag(PmuTestTags.stopTarget(Player.ONE.name)).toInt() * 1_000
                composeRule.awaitTag(PmuTestTags.stopButton(Player.ONE.name))

                val startedAt = clock.nowMillis
                clock.nowMillis = startedAt + targetMillis + 100L
                composeRule.onNodeWithTag(PmuTestTags.stopButton(Player.ONE.name)).performClick()
                clock.nowMillis = startedAt + targetMillis + 1_500L
                composeRule.onNodeWithTag(PmuTestTags.stopButton(Player.TWO.name)).performClick()
            }

            MiniGame.BINARY_DECIMAL -> {
                composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))
                val answer = solveBinaryPrompt(
                    composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
                )
                composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, answer))
                    .performClick()
            }
        }

        composeRule.awaitTag(PmuTestTags.ROUND_RESULT)
    }

    private companion object {
        const val PARTY_ROUNDS = 3
        const val TAP_RACE_TAPS = 5
        const val MEMORY_CARDS = 12
    }
}
