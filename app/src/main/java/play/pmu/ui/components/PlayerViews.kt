package play.pmu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

/**
 * Oznaka igraca ("Igrac 1"), uvek u boji tog igraca. Zahvaljujuci
 * `Player.accentColor` nijedan ekran ne pamti koja je boja cija.
 *
 * [isActive] blago zatamnjuje oznaku igraca koji trenutno nije na potezu -
 * animirano, pa je promena poteza vidljiva i bez citanja teksta.
 */
@Composable
fun PlayerBadge(
    player: Player,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isActive) player.accentColor else player.accentColor.copy(alpha = 0.35f),
        label = "playerBadgeColor",
    )
    Surface(
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(player.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/**
 * Trenutni rezultat partije, u obliku "2 : 1". Svaki broj je u boji svog igraca,
 * pa se sa oba kraja telefona odmah vidi koji je broj ciji.
 */
@Composable
fun PartyScore(
    scoreOne: Int,
    scoreTwo: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = scoreOne.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Player.ONE.accentColor,
        )
        Text(text = ":", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = scoreTwo.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Player.TWO.accentColor,
        )
    }
}

/** Ime pobednika runde ili partije, za tekstove tipa "Igrac 1 osvaja rundu". */
@Composable
fun winnerName(winner: Winner): String = when (winner) {
    Winner.PLAYER_ONE -> stringResource(R.string.player_one)
    Winner.PLAYER_TWO -> stringResource(R.string.player_two)
    // Nereseno nema ime; pozivalac za DRAW koristi svoj tekst.
    Winner.DRAW -> ""
}

/** Boja kojom se prikazuje ishod: boja pobednika, neutralna kod neresenog. */
@Composable
fun winnerColor(winner: Winner): Color = when (winner) {
    Winner.PLAYER_ONE -> Player.ONE.accentColor
    Winner.PLAYER_TWO -> Player.TWO.accentColor
    Winner.DRAW -> MaterialTheme.colorScheme.onSurfaceVariant
}
