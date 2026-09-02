package play.pmu.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Zajednicke mere, da se rastojanja ne bi pisala "na oko" u svakom ekranu.
 * Obican objekat sa konstantama je dovoljan - Material 3 nema svoj spacing
 * sistem, a pravljenje sopstvenog CompositionLocal-a bi za ovoliko vrednosti
 * bilo previse.
 */
object PmuSpacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
}

/**
 * Najmanja visina dugmeta u igri. Party igre se igraju brzo i "naslepo", pa su
 * mete namerno vece od Material minimuma (48 dp).
 */
val GameTouchTargetSize = 72.dp

/** Visina dugmadi na ekranima rezultata, da svuda budu iste. */
val ActionButtonHeight = 56.dp
