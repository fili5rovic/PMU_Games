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

/**
 * Pomocne funkcije za instrumentacione testove.
 *
 * Sve cekanje ide kroz `waitUntil`, koje ANKETIRA stanje do zadatog roka - nema
 * `Thread.sleep` niti fiksnih pauza, pa test ne postaje ni sporiji ni
 * nepouzdaniji od brzine emulatora.
 */

private const val DEFAULT_TIMEOUT_MILLIS = 20_000L

/** Ceka da se element sa datim tagom pojavi. */
fun ComposeTestRule.awaitTag(tag: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {
    waitUntil(timeoutMillis) { hasTagOnScreen(tag) }
}

/** Ceka da se dati tekst pojavi (npr. lokalizovana poruka o pobedniku). */
fun ComposeTestRule.awaitText(text: String, timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }
}

fun SemanticsNodeInteractionsProvider.hasTagOnScreen(tag: String): Boolean =
    onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

/** Tekst elementa sa datim tagom - test tako cita pitanje ili ciljno vreme sa ekrana. */
fun SemanticsNodeInteractionsProvider.textOfTag(tag: String): String =
    onAllNodesWithTag(tag)[0]
        .fetchSemanticsNode()
        .config[SemanticsProperties.Text]
        .joinToString("") { it.text }

/**
 * Ime igraca koji je trenutno na potezu, prepoznato po tome sto se u NJEGOVOM
 * delu ekrana nalazi potvrda "Tvoj potez".
 *
 * Ovako se proverava i vizualno stanje aktivnog igraca: ne gleda se boja
 * piksela, nego kome pripada deo ekrana na kome pise da je na potezu.
 */
fun SemanticsNodeInteractionsProvider.activePlayerName(yourTurnText: String): String? =
    Player.entries.firstOrNull { player ->
        onAllNodes(
            hasTestTag(PmuTestTags.playerArea(player.name)) and
                hasAnyDescendant(hasText(yourTurnText))
        ).fetchSemanticsNodes().isNotEmpty()
    }?.name

/**
 * Tapka PRVI element sa datim tagom.
 *
 * Ekrani rezultata prikazuju iste dugmice na obe polovine ekrana (da ih dohvati
 * bilo koji igrac), pa `onNodeWithTag` tamo nadje dva cvora i pukne. Za igru je
 * svejedno koji je pritisnut.
 */
fun ComposeTestRule.clickFirstWithTag(tag: String) {
    awaitTag(tag)
    onAllNodesWithTag(tag)[0].performClick()
}

/**
 * Odigra celu rundu memorije.
 *
 * Kartice se ne pogadjaju: zatvorena kartica prikazuje "?", a otvorena svoj
 * simbol, pa test cita tablu isto kao igrac. U svakoj iteraciji otvori dve
 * NEPOZNATE kartice i zapamti sta su bile, pa posle nekoliko poteza zna celu
 * tablu i sparuje je.
 *
 * Vazno: dok se dve nesparene kartice vracaju, igra ODBIJA nove klikove. Zato se
 * posle svakog poteza ceka da se par razresi - a to se prepoznaje po tome sto se
 * ili smanjio broj zatvorenih kartica (par pogodjen) ili se promenio igrac na
 * potezu (promasaj). Bez tog cekanja bi sledeci klik bio tiho odbacen.
 */
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

/** Ceka da par bude razresen: par pogodjen ili potez presao protivniku. */
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

/** Simbol kartice posle obrtanja, ili null ako klik nije prihvacen. */
private fun ComposeTestRule.symbolAfterFlip(index: Int): String? = runCatching {
    waitUntil(CARD_FLIP_TIMEOUT_MILLIS) { textOfCard(index) != HIDDEN_CARD }
    textOfCard(index)
}.getOrNull()

private const val HIDDEN_CARD = "?"
private const val CARD_FLIP_TIMEOUT_MILLIS = 2_000L
private const val PAIR_RESOLVE_TIMEOUT_MILLIS = 4_000L

/** Otvara mini igru sa pocetnog ekrana, uz skrolovanje do njene kartice. */
fun ComposeTestRule.openMiniGame(game: MiniGame) {
    awaitTag(PmuTestTags.HOME_LIST)
    onNodeWithTag(PmuTestTags.HOME_LIST)
        .performScrollToNode(hasTestTag(PmuTestTags.miniGame(game.name)))
    onNodeWithTag(PmuTestTags.miniGame(game.name)).performClick()
}

/**
 * Racuna tacan odgovor iz postavljenog pitanja ("12 ÷ 3 = ?").
 *
 * Test dakle ne mora da zna sta je Random izvukao - procita pitanje sa ekrana i
 * sam ga resi, isto kao igrac.
 */
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

/** Pretvara prikazani binarni broj ("10110₂") u decimalni. */
fun solveBinaryPrompt(prompt: String): Int =
    prompt.filter { it == '0' || it == '1' }.toInt(2)
