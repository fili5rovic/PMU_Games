package play.pmu.e2e

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Player
import play.pmu.ui.PmuTestTags



private const val DEFAULT_TIMEOUT_MILLIS = 20_000L

fun ComposeTestRule.awaitTag(tag: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {
    waitUntil(timeoutMillis) { hasTagOnScreen(tag) }
}

fun ComposeTestRule.awaitText(text: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }
}

fun SemanticsNodeInteractionsProvider.hasTagOnScreen(tag: String): Boolean =
    onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

fun SemanticsNodeInteractionsProvider.textOfTag(tag: String): String =
    onAllNodesWithTag(tag)[0]
        .fetchSemanticsNode()
        .config[SemanticsProperties.Text]
        .joinToString("") { it.text }

fun SemanticsNodeInteractionsProvider.activePlayerName(yourTurnText: String): String? =
    Player.entries.firstOrNull { player ->
        onAllNodes(
            hasTestTag(PmuTestTags.playerArea(player.name)) and
                hasAnyDescendant(hasText(yourTurnText))
        ).fetchSemanticsNodes().isNotEmpty()
    }?.name

fun ComposeTestRule.clickFirstWithTag(tag: String) {
    awaitTag(tag)
    onAllNodesWithTag(tag)[0].performClick()
}


fun ComposeTestRule.playMemoryRound(
    yourTurnText: String,
    cardCount: Int = 12,
    maxMoves: Int = 40,
) {
    val known = mutableMapOf<Int, String>()

    repeat(maxMoves) {
        if (hasTagOnScreen(PmuTestTags.ROUND_RESULT)) return

        val hidden = hiddenCards(cardCount)
        if (hidden.size < 2) return

        val activeBefore = activePlayerName(yourTurnText)

        // Poznat par medju zatvorenim karticama - sparuj ga.
        val knownPair = hidden.filter { known.containsKey(it) }
            .groupBy { known.getValue(it) }
            .values
            .firstOrNull { it.size >= 2 }

        val (first, second) = if (knownPair != null) {
            knownPair[0] to knownPair[1]
        } else {
            val unknown = hidden.filterNot { known.containsKey(it) }
            val a = unknown.firstOrNull() ?: hidden[0]
            a to (unknown.getOrNull(1) ?: hidden.first { it != a })
        }

        flipCard(first)
        symbolAfterFlip(first)?.let { known[first] = it }
        flipCard(second)
        symbolAfterFlip(second)?.let { known[second] = it }

        awaitPairResolved(cardCount, hidden.size, activeBefore, yourTurnText)
    }
}

private fun SemanticsNodeInteractionsProvider.hiddenCards(cardCount: Int): List<Int> =
    (0 until cardCount).filter { textOfCard(it) == HIDDEN_CARD }

private fun ComposeTestRule.awaitPairResolved(
    cardCount: Int,
    hiddenBefore: Int,
    activeBefore: String?,
    yourTurnText: String,
) {
    runCatching {
        waitUntil(PAIR_RESOLVE_TIMEOUT_MILLIS) {
            hasTagOnScreen(PmuTestTags.ROUND_RESULT) ||
                hiddenCards(cardCount).size < hiddenBefore ||
                activePlayerName(yourTurnText) != activeBefore
        }
    }
}

private fun ComposeTestRule.flipCard(index: Int) {
    onNodeWithTag(PmuTestTags.memoryCard(index)).performClick()
}

private fun SemanticsNodeInteractionsProvider.textOfCard(index: Int): String =
    textOfTag(PmuTestTags.memoryCard(index))

private fun ComposeTestRule.symbolAfterFlip(index: Int): String? = runCatching {
    waitUntil(CARD_FLIP_TIMEOUT_MILLIS) { textOfCard(index) != HIDDEN_CARD }
    textOfCard(index)
}.getOrNull()

private const val HIDDEN_CARD = "?"
private const val CARD_FLIP_TIMEOUT_MILLIS = 2_000L
private const val PAIR_RESOLVE_TIMEOUT_MILLIS = 4_000L

fun ComposeTestRule.openMiniGame(game: MiniGame) {
    awaitTag(PmuTestTags.HOME_LIST)
    onNodeWithTag(PmuTestTags.HOME_LIST)
        .performScrollToNode(hasTestTag(PmuTestTags.miniGame(game.name)))
    onNodeWithTag(PmuTestTags.miniGame(game.name)).performClick()
}

fun solveMathPrompt(prompt: String): Int {
    val parts = prompt.removeSuffix(" = ?").split(" ")
    val left = parts[0].toInt()
    val right = parts[2].toInt()
    return when (parts[1]) {
        MathOperation.PLUS.symbol -> left + right
        MathOperation.MINUS.symbol -> left - right
        MathOperation.TIMES.symbol -> left * right
        MathOperation.DIVIDE.symbol -> left / right
        else -> error("nepoznata operacija u '$prompt'")
    }
}

fun solveBinaryPrompt(prompt: String): Int =
    prompt.filter { it == '0' || it == '1' }.toInt(2)
