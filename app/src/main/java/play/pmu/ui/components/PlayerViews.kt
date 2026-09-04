package play.pmu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import play.pmu.R
import play.pmu.domain.model.Player
import play.pmu.domain.model.Winner
import play.pmu.ui.theme.accentColor

@Composable
fun PlayerAreaLabel(
    player: Player,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeColor = player.accentColor
    val nameColor by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "playerNameColor",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(player.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            color = nameColor,
        )
        if (isActive) {
            Text(
                text = stringResource(R.string.tictactoe_your_turn),
                style = MaterialTheme.typography.titleSmall,
                color = activeColor,
            )
        }
    }
}

@Composable
fun PartyScore(
    scoreOne: Int,
    scoreTwo: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PmuScoreSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = scoreOne.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Player.ONE.accentColor,
        )
        Text(
            text = ":",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = scoreTwo.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Player.TWO.accentColor,
        )
    }
}

@Composable
fun winnerName(winner: Winner): String = when (winner) {
    Winner.PLAYER_ONE -> stringResource(R.string.player_one)
    Winner.PLAYER_TWO -> stringResource(R.string.player_two)
    Winner.DRAW -> ""
}

@Composable
fun winnerColor(winner: Winner): Color = when (winner) {
    Winner.PLAYER_ONE -> Player.ONE.accentColor
    Winner.PLAYER_TWO -> Player.TWO.accentColor
    Winner.DRAW -> MaterialTheme.colorScheme.onSurfaceVariant
}

private val PmuScoreSpacing = 12.dp
