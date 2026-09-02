package play.pmu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
                // Jedna Surface preko celog sadrzaja daje pozadinu SVIM ekranima.
                // Bez nje ekrani bez Scaffold-a (mini igre, uputstva, rezultat
                // runde) puste da se vidi pozadina prozora, koja ne zna nista o
                // temi izabranoj u aplikaciji - pa su u tamnoj temi ostajali beli.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PmuNavHost()
                }
            }
        }
    }
}
