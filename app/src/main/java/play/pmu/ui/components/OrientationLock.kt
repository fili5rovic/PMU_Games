package play.pmu.ui.components

import android.content.pm.ActivityInfo
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import play.pmu.domain.model.GameOrientation

@Composable
fun LockScreenOrientation(orientation: GameOrientation) {
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
