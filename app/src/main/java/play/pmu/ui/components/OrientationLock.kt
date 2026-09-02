package play.pmu.ui.components

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/** Orijentacija koju jedan ekran zahteva. */
enum class GameOrientation { PORTRAIT, LANDSCAPE }

/**
 * Zakljucava orijentaciju ekrana dok je ovaj composable u kompoziciji.
 *
 * Orijentacija je u ovoj aplikaciji deo pravila igre, pa se ne prepusta
 * slucaju: igre na podeljenom ekranu rade samo u portretu, a pantomima (telefon
 * se drzi na celu i naglo pomera) samo u landscape-u.
 *
 * Ovo je JEDINO mesto u projektu koje dira orijentaciju. `DisposableEffect`
 * vraca prethodnu vrednost u `onDispose`, tj. kada se ekran napusti - zato
 * nijedan ekran ne mora da "cisti" za sobom, niti da zna sta je bilo pre njega.
 *
 * Posledica zakljucavanja: slucajno okretanje telefona u toku runde ne pravi
 * promenu konfiguracije, pa ne moze da resetuje igru. A jednu promenu koja se
 * stvarno dogodi (ulaz u pantomimu) prezivljava state u ViewModel-u, jer ViewModel
 * nadzivljava rekreiranje Activity-ja.
 *
 * Podrazumevana orijentacija cele aplikacije je portret (AndroidManifest), pa
 * meniji i ekrani rezultata ne moraju nista da rade.
 */
@Composable
fun LockScreenOrientation(orientation: GameOrientation) {
    // LocalActivity je null u @Preview-u; tada se orijentacija prosto ne dira.
    val activity = LocalActivity.current ?: return

    DisposableEffect(orientation) {
        val previous = activity.requestedOrientation
        activity.requestedOrientation = when (orientation) {
            GameOrientation.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            GameOrientation.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        onDispose { activity.requestedOrientation = previous }
    }
}
