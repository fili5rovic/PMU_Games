package play.pmu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import play.pmu.navigation.PmuNavHost
import play.pmu.ui.settings.SettingsViewModel
import play.pmu.ui.theme.PmuGamesTheme

/**
 * Jedina Activity u aplikaciji - sva navigacija ide kroz Navigation Compose.
 *
 * @AndroidEntryPoint omogucava da composable-i unutar nje dobiju @HiltViewModel-e.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Tema se cita iz podesavanja, pa promena u Settings ekranu
            // odmah prefarba celu aplikaciju.
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsStateWithLifecycle()

            PmuGamesTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor,
            ) {
                PmuNavHost()
            }
        }
    }
}
