package play.pmu.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Boje za povratnu informaciju u igrama (pogodak / promasaj). Nisu deo Material
 * color scheme-a jer njihovo znacenje ne zavisi od teme - zeleno je uvek "tacno".
 */
val CorrectGreen = Color(0xFF2E7D32)
val CorrectGreenLight = Color(0xFF66BB6A)
val WrongRed = Color(0xFFC62828)
val WrongRedLight = Color(0xFFEF5350)
val WaitingRed = Color(0xFFB71C1C)
val GoGreen = Color(0xFF1B5E20)

/**
 * Boje igraca. Iz istog razloga kao i boje povratne informacije stoje van
 * color scheme-a: igrac 1 je uvek plav, a igrac 2 uvek narandzast, i u svetloj i
 * u tamnoj temi. Da su uzete iz teme (ili iz dinamickih boja telefona), dva
 * igraca bi mogla da dobiju slicne boje i izgube se na podeljenom ekranu.
 *
 * `Container` varijante su podloge polovina ekrana, a osnovne boje su akcenti
 * (okvir, tekst, dugmad).
 */
val PlayerOneColor = Color(0xFF1565C0)
val PlayerOneContainer = Color(0xFF0D47A1)
val PlayerTwoColor = Color(0xFFEF6C00)
val PlayerTwoContainer = Color(0xFFBF360C)
