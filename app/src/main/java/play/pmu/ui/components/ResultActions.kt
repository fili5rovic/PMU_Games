package play.pmu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import play.pmu.ui.PmuTestTags
import play.pmu.ui.theme.ActionButtonHeight
import play.pmu.ui.theme.PmuSpacing

/**
 * Dugmad na ekranima rezultata: "Pocetna" i glavna akcija (nova runda / nova
 * partija / igraj ponovo).
 *
 * Jedna komponenta za sve ekrane rezultata, pa su dugmad svuda iste sirine
 * (`weight(1f)`), iste visine ([ActionButtonHeight]) i sa istim rastojanjem.
 * Ranije je svaki ekran slagao svoj Row ili Column, pa su se velicine
 * razlikovale od ekrana do ekrana.
 */
@Composable
fun ResultActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    homeLabel: String,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
    ) {
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier
                .weight(1f)
                .height(ActionButtonHeight)
                .testTag(PmuTestTags.HOME_BUTTON),
        ) {
            Text(text = homeLabel, style = MaterialTheme.typography.titleSmall)
        }
        Button(
            onClick = onPrimary,
            modifier = Modifier
                .weight(1f)
                .height(ActionButtonHeight)
                .testTag(PmuTestTags.NEW_GAME_BUTTON),
        ) {
            Text(text = primaryLabel, style = MaterialTheme.typography.titleSmall)
        }
    }
}
