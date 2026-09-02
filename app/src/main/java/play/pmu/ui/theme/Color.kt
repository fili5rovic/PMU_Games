package play.pmu.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Boje za povratnu informaciju u igrama (pogodak / promasaj, cekaj / kreni).
 *
 * NISU deo Material color scheme-a jer njihovo znacenje ne zavisi od teme -
 * zeleno je uvek "tacno". Sve su namerno tamne, pa beli tekst preko njih ostaje
 * citljiv i u svetloj i u tamnoj temi.
 */
val CorrectGreen = Color(0xFF2E7D32)
val CorrectGreenLight = Color(0xFF66BB6A)
val WrongRed = Color(0xFFC62828)
val WrongRedLight = Color(0xFFEF5350)
val WaitingRed = Color(0xFFB71C1C)
val GoGreen = Color(0xFF1B5E20)

/**
 * Boje identiteta igraca: igrac 1 je uvek plav, igrac 2 uvek narandzast.
 *
 * Stoje van color scheme-a iz istog razloga kao boje povratne informacije - da
 * dva igraca ne bi slucajno dobila slicne boje (npr. iz dinamickih boja
 * telefona) i izgubila se na podeljenom ekranu.
 *
 * Svaka ima svetlu i tamnu varijantu: tamna tema trazi svetliji ton da bi tekst
 * u boji igraca ostao citljiv. Koja se koristi bira `Player.accentColor`.
 */
val PlayerOneColor = Color(0xFF1565C0)
val PlayerOneColorDark = Color(0xFF82B9F5)
val PlayerTwoColor = Color(0xFFE65100)
val PlayerTwoColorDark = Color(0xFFFFB067)
