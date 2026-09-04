package play.pmu.e2e

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import play.pmu.MainActivity
import play.pmu.R
import play.pmu.data.repository.SettingsRepository
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import play.pmu.testing.TestGameClock
import play.pmu.ui.PmuTestTags
import javax.inject.Inject

@HiltAndroidTest
class MiniGameFlowTest {

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

    private val playerOneWins by lazy {
        string(R.string.round_winner, string(R.string.player_one))
    }
    private val yourTurn by lazy { string(R.string.tictactoe_your_turn) }

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun ticTacToe_igra_se_do_pobede_pa_pocinje_nova_runda() = runBlocking<Unit> {
        settingsRepository.setTicTacToeBoardSize(BoardSizeOption.THREE)

        composeRule.openMiniGame(MiniGame.TIC_TAC_TOE)
        composeRule.awaitTag(PmuTestTags.ticTacToeCell(0, 0))

        val starter = composeRule.activePlayerName(yourTurn)
        assertNotNull("neko mora da bude na potezu", starter)

        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(0, 0)).performClick()
        composeRule.waitForIdle()
        assertNotEquals(starter, composeRule.activePlayerName(yourTurn))

        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(1, 0)).performClick()
        composeRule.waitForIdle()
        assertEquals(starter, composeRule.activePlayerName(yourTurn))

        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(0, 1)).performClick()
        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(1, 1)).performClick()
        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(0, 2)).performClick()

        val expectedWinner = string(
            R.string.round_winner,
            string(if (starter == Player.ONE.name) R.string.player_one else R.string.player_two),
        )
        composeRule.awaitText(expectedWinner)
        composeRule.onAllNodesWithTag(PmuTestTags.ROUND_RESULT)[0].assertIsDisplayed()

        composeRule.clickFirstWithTag(PmuTestTags.NEW_GAME_BUTTON)
        composeRule.awaitTag(PmuTestTags.ticTacToeCell(0, 0))
        composeRule.onAllNodesWithText(yourTurn).assertCountEquals(1)
    }

    @Test
    fun ticTacToe_tabla_moze_da_bude_5x5() = runBlocking<Unit> {
        settingsRepository.setTicTacToeBoardSize(BoardSizeOption.FIVE)

        composeRule.openMiniGame(MiniGame.TIC_TAC_TOE)
        composeRule.awaitTag(PmuTestTags.ticTacToeCell(4, 4))

        composeRule.onNodeWithTag(PmuTestTags.ticTacToeCell(4, 4)).assertIsDisplayed()
    }

    @Test
    fun mathDuel_tacan_odgovor_osvaja_rundu() = runBlocking<Unit> {
        settingsRepository.setMathOperations(setOf(MathOperation.DIVIDE))

        composeRule.openMiniGame(MiniGame.MATH_DUEL)
        composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))

        val prompt = composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
        val answer = solveMathPrompt(prompt)

        composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, answer)).performClick()

        composeRule.awaitText(playerOneWins)
    }

    @Test
    fun mathDuel_promasaj_iskljucuje_igraca_a_protivnik_nastavlja() = runBlocking<Unit> {
        settingsRepository.setMathOperations(setOf(MathOperation.PLUS))

        composeRule.openMiniGame(MiniGame.MATH_DUEL)
        composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))

        val prompt = composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
        val correct = solveMathPrompt(prompt)

        val wrong = wrongAnswerOnScreen(Player.TWO, correct)
        composeRule.onNodeWithTag(PmuTestTags.answer(Player.TWO.name, wrong)).performClick()
        composeRule.awaitText(string(R.string.math_duel_locked))

        composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, correct)).performClick()
        composeRule.awaitText(playerOneWins)
    }

    private fun wrongAnswerOnScreen(player: Player, correct: Int): Int {
        val candidates = (correct - 6..correct + 6).filter { it != correct && it >= 0 }
        return candidates.first { composeRule.hasTagOnScreen(PmuTestTags.answer(player.name, it)) }
    }

    @Test
    fun binaryDuel_tacna_decimalna_vrednost_osvaja_rundu() {
        composeRule.openMiniGame(MiniGame.BINARY_DECIMAL)
        composeRule.awaitTag(PmuTestTags.duelPrompt(Player.ONE.name))

        val prompt = composeRule.textOfTag(PmuTestTags.duelPrompt(Player.ONE.name))
        val answer = solveBinaryPrompt(prompt)

        composeRule.onNodeWithTag(PmuTestTags.answer(Player.ONE.name, answer)).performClick()

        composeRule.awaitText(string(R.string.binary_correct_value, answer))
        composeRule.awaitText(playerOneWins)
    }

    @Test
    fun stopTheTimer_pobedjuje_igrac_sa_manjim_odstupanjem() {
        composeRule.openMiniGame(MiniGame.STOP_THE_TIMER)

        composeRule.awaitTag(PmuTestTags.stopTarget(Player.ONE.name))
        val targetMillis =
            composeRule.textOfTag(PmuTestTags.stopTarget(Player.ONE.name)).toInt() * 1_000

        composeRule.awaitTag(PmuTestTags.stopButton(Player.ONE.name))

        val startedAt = clock.nowMillis
        clock.nowMillis = startedAt + targetMillis + 200L
        composeRule.onNodeWithTag(PmuTestTags.stopButton(Player.ONE.name)).performClick()

        clock.nowMillis = startedAt + targetMillis + 900L
        composeRule.onNodeWithTag(PmuTestTags.stopButton(Player.TWO.name)).performClick()

        composeRule.awaitText(playerOneWins)
    }

    @Test
    fun reaction_prvi_tap_na_zelenom_osvaja_rundu() {
        composeRule.openMiniGame(MiniGame.REACTION)

        composeRule.awaitTag(PmuTestTags.reactionHalf(Player.ONE.name))
        composeRule.awaitText(string(R.string.reaction_tap_now))

        composeRule.onNodeWithTag(PmuTestTags.reactionHalf(Player.ONE.name)).performClick()

        composeRule.awaitText(playerOneWins)
    }

    @Test
    fun reaction_rani_tap_je_pogresan_start() {
        composeRule.openMiniGame(MiniGame.REACTION)

        composeRule.awaitTag(PmuTestTags.reactionHalf(Player.ONE.name))

        composeRule.onNodeWithTag(PmuTestTags.reactionHalf(Player.ONE.name)).performClick()

        composeRule.awaitText(string(R.string.round_winner, string(R.string.player_two)))
        composeRule.awaitText(string(R.string.outcome_false_start))
    }

    @Test
    fun memory_deli_dvanaest_karata_i_prati_red_poteza() {
        composeRule.openMiniGame(MiniGame.MEMORY)
        composeRule.awaitTag(PmuTestTags.memoryCard(0))

        repeat(12) { index ->
            composeRule.onNodeWithTag(PmuTestTags.memoryCard(index)).assertIsDisplayed()
        }
        composeRule.onAllNodesWithText(yourTurn).assertCountEquals(1)

        composeRule.onNodeWithTag(PmuTestTags.memoryCard(0)).performClick()
        composeRule.onNodeWithTag(PmuTestTags.memoryCard(1)).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(yourTurn).fetchSemanticsNodes().size == 1
        }
    }
}
