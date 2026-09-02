package play.pmu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

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
 * [bottomPanel]. Tabla je [centerContent] i zadrzava svoju velicinu, a panelima
 * ostaje sav prostor iznad i ispod (`weight(1f)`).
 *
 * Zbog toga su paneli dovoljno VELIKE povrsine da se mogu obojiti bojom igraca
 * koji je na potezu - sto je jedini nacin da se red poteza vidi u delicu sekunde.
 */
@Composable
fun SharedBoardLayout(
    topPanel: @Composable BoxScope.() -> Unit,
    bottomPanel: @Composable BoxScope.() -> Unit,
    centerContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    topPanelModifier: Modifier = Modifier,
    bottomPanelModifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(180f)
                .then(topPanelModifier),
            contentAlignment = Alignment.Center,
            content = topPanel,
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = centerContent,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(bottomPanelModifier),
            contentAlignment = Alignment.Center,
            content = bottomPanel,
        )
    }
}
