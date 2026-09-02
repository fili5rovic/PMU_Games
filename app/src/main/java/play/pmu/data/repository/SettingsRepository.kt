package play.pmu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import play.pmu.domain.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

/** Podesavanja aplikacije, citana kao jedan objekat. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val roundDurationSeconds: Int = 60,
    val questionCount: Int = 10,
    val manualCharadesControls: Boolean = false,
)

/**
 * Podesavanja se cuvaju u DataStore Preferences - lagana perzistencija za
 * nekoliko vrednosti, za koju bi Room bio preterano tesko resenje.
 *
 * Citanje je Flow, pa se svaka promena odmah propagira do UI-a (npr. tema).
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            // valueOf bi pukao na nepoznatom tekstu, zato ide preko entries.find
            themeMode = ThemeMode.entries.find { it.name == prefs[KeyThemeMode] }
                ?: ThemeMode.SYSTEM,
            dynamicColor = prefs[KeyDynamicColor] ?: true,
            roundDurationSeconds = prefs[KeyRoundDuration] ?: 60,
            questionCount = prefs[KeyQuestionCount] ?: 10,
            manualCharadesControls = prefs[KeyManualControls] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[KeyThemeMode] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = edit { it[KeyDynamicColor] = enabled }

    suspend fun setRoundDuration(seconds: Int) = edit { it[KeyRoundDuration] = seconds }

    suspend fun setQuestionCount(count: Int) = edit { it[KeyQuestionCount] = count }

    suspend fun setManualCharadesControls(enabled: Boolean) =
        edit { it[KeyManualControls] = enabled }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private companion object {
        val KeyThemeMode = stringPreferencesKey("theme_mode")
        val KeyDynamicColor = booleanPreferencesKey("dynamic_color")
        val KeyRoundDuration = intPreferencesKey("round_duration")
        val KeyQuestionCount = intPreferencesKey("question_count")
        val KeyManualControls = booleanPreferencesKey("manual_controls")
    }
}
