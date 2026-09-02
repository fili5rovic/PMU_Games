package play.pmu.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import play.pmu.R

/**
 * Zajednicki top bar sa opcionim dugmetom za nazad, da svi ekrani izgledaju isto.
 * [actions] omogucava da pocetni ekran doda svoje ikonice (statistika, podesavanja).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PmuTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        // AutoMirrored varijanta se sama okrece za jezike koji se citaju s desna na levo
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
            }
        },
        actions = { actions() },
    )
}
