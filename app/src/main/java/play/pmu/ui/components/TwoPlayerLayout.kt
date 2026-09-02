package play.pmu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

/**
 * Osnovni raspored svih igara za dva igraca.
 *
 * Ekran se deli na dve JEDNAKE polovine (`weight(1f)`), a gornja se rotira za
 * 180 stepeni. Telefon lezi na stolu izmedju igraca, pa igrac koji sedi
 * preko puta svoj deo ekrana vidi uspravno.
 *
 * `Modifier.rotate` rotira i crtanje i obradu dodira, tako da gornji igrac tapka
 * tamo gde vidi dugme - nema potrebe za rucnim preracunavanjem koordinata.
 *
 * [topModifier] i [bottomModifier] se primenjuju UNUTAR polovine, pa igra kroz
 * njih moze da postavi svoju podlogu i `clickable` na celu polovinu.
 */
@Composable
fun TwoPlayerLayout(
    topContent: @Composable BoxScope.() -> Unit,
    bottomContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    topModifier: Modifier = Modifier,
    bottomModifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(180f)
                .then(topModifier),
            contentAlignment = Alignment.Center,
            content = topContent,
        )
        HorizontalDivider(thickness = DIVIDER_THICKNESS)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(bottomModifier),
            contentAlignment = Alignment.Center,
            content = bottomContent,
        )
    }
}

/**
 * Raspored za igre sa JEDNOM zajednickom tablom u sredini (iks-oks, memorija).
 *
 * Tabla se ne moze rotirati jer je oba igraca gledaju istovremeno - kao karte na
 * stolu. Zato se rotira samo ono sto pripada pojedinom igracu: [topPanel] i
 * [bottomPanel] (skor, cij je potez). Tabla je [centerContent].
 */
@Composable
fun SharedBoardLayout(
    topPanel: @Composable () -> Unit,
    bottomPanel: @Composable () -> Unit,
    centerContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().rotate(180f)) { topPanel() }
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center,
            content = centerContent,
        )
        Box(modifier = Modifier.fillMaxWidth()) { bottomPanel() }
    }
}

private val DIVIDER_THICKNESS = 2.dp
