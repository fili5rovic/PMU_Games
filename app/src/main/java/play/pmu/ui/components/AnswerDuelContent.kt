package play.pmu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import play.pmu.R
import play.pmu.domain.game.AnswerDuel
import play.pmu.domain.model.Player
import play.pmu.ui.PmuTestTags
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.areaColor

@Composable
fun AnswerDuelContent(
    prompt: String,
    answers: List<String>,
    duel: AnswerDuel,
    onAnswer: (Player, Int) -> Unit,
    modifier: Modifier = Modifier,
    promptStyle: TextStyle = MaterialTheme.typography.displaySmall,
    footer: String? = null,
) {
    TwoPlayerLayout(
        topContent = { DuelHalf(Player.TWO, prompt, promptStyle, answers, duel, footer, onAnswer) },
        bottomContent = { DuelHalf(Player.ONE, prompt, promptStyle, answers, duel, footer, onAnswer) },
        modifier = modifier,
        topModifier = Modifier
            .fillMaxSize()
            .background(Player.TWO.areaColor(isActive = Player.TWO !in duel.lockedOut)),
        bottomModifier = Modifier
            .fillMaxSize()
            .background(Player.ONE.areaColor(isActive = Player.ONE !in duel.lockedOut)),
    )
}

@Composable
private fun DuelHalf(
    player: Player,
    prompt: String,
    promptStyle: TextStyle,
    answers: List<String>,
    duel: AnswerDuel,
    footer: String?,
    onAnswer: (Player, Int) -> Unit,
) {
    Column(
        modifier = Modifier.padding(PmuSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        Text(
            text = if (player in duel.lockedOut) stringResource(R.string.math_duel_locked) else prompt,
            style = promptStyle,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            // Test procita pitanje sa ekrana i sam izracuna tacan odgovor.
            modifier = Modifier.testTag(PmuTestTags.duelPrompt(player.name)),
        )

        // chunked(2) daje mrezu 2x2 od cetiri ponudjena odgovora.
        answers.chunked(ANSWERS_PER_ROW).forEachIndexed { rowIndex, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEachIndexed { columnIndex, answer ->
                    val index = rowIndex * ANSWERS_PER_ROW + columnIndex
                    Button(
                        onClick = { onAnswer(player, index) },
                        enabled = duel.canAnswer(player),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = GameTouchTargetSize)
                            // Test tapka odgovor po VREDNOSTI, ne po mestu na
                            // ekranu - pa ne zavisi od toga kako su izmesani.
                            .testTag(
                                PmuTestTags.answer(player.name, answer.toIntOrNull() ?: index)
                            ),
                    ) {
                        Text(text = answer, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }

        // Posle runde se prikazuje tacna vrednost, da igraci vide resenje.
        if (footer != null) {
            Text(
                text = footer,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val ANSWERS_PER_ROW = 2
