package play.pmu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.GameSettings
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.ThemeMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Podesavanja aplikacije, citana kao jedan objekat.
 *
 * Podesavanja pojedinih igara su izdvojena u [GameSettings], pa se vidi sta je
 * opsti izgled/tok aplikacije, a sta pravila jedne igre.
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val partyRounds: Int = DEFAULT_PARTY_ROUNDS,
    val games: GameSettings = GameSettings(),
    val roundDurationSeconds: Int = DEFAULT_ROUND_DURATION,
    val questionCount: Int = DEFAULT_QUESTION_COUNT,
    val manualCharadesControls: Boolean = false,
) {
    companion object {
        /** Sedam rundi je dovoljno da se vise razlicitih igara pojavi, a partija ne oduzi. */
        const val DEFAULT_PARTY_ROUNDS = 7
        const val DEFAULT_ROUND_DURATION = 60
        const val DEFAULT_QUESTION_COUNT = 10
    }
}

/**
 * Podesavanja se cuvaju u DataStore Preferences - lagana perzistencija za
 * nekoliko vrednosti, za koju bi Room bio preterano tesko resenje.
 *
 * Citanje je Flow, pa se svaka promena odmah propagira do UI-a (npr. tema ili
 * dozvoljene racunske operacije).
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
            partyRounds = prefs[KeyPartyRounds] ?: AppSettings.DEFAULT_PARTY_ROUNDS,
            games = GameSettings(
                ticTacToeBoardSize = BoardSizeOption.entries
                    .find { it.name == prefs[KeyBoardSize] }
                    ?: BoardSizeOption.RANDOM,
                // fromNames se sam vraca na sve operacije ako je sacuvani skup
                // prazan ili nepoznat - igra uvek mora da ima sta da postavi.
                mathOperations = MathOperation.fromNames(prefs[KeyMathOperations].orEmpty()),
            ),
            roundDurationSeconds = prefs[KeyRoundDuration] ?: AppSettings.DEFAULT_ROUND_DURATION,
            questionCount = prefs[KeyQuestionCount] ?: AppSettings.DEFAULT_QUESTION_COUNT,
            manualCharadesControls = prefs[KeyManualControls] ?: false,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = edit { it[KeyThemeMode] = mode.name }

    suspend fun setDynamicColor(enabled: Boolean) = edit { it[KeyDynamicColor] = enabled }

    suspend fun setPartyRounds(rounds: Int) = edit { it[KeyPartyRounds] = rounds }

    suspend fun setTicTacToeBoardSize(option: BoardSizeOption) =
        edit { it[KeyBoardSize] = option.name }

    /**
     * Prazan skup se ne upisuje: racunski duel bez ijedne operacije ne bi mogao
     * da napravi pitanje. UI i sam ne dozvoljava da se ugasi zadnja operacija,
     * pa je ovo druga brana.
     */
    suspend fun setMathOperations(operations: Set<MathOperation>) {
        if (operations.isEmpty()) return
        edit { prefs -> prefs[KeyMathOperations] = operations.map { it.name }.toSet() }
    }

    suspend fun setRoundDuration(seconds: Int) = edit { it[KeyRoundDuration] = seconds }

    suspend fun setQuestionCount(count: Int) = edit { it[KeyQuestionCount] = count }

    suspend fun setManualCharadesControls(enabled: Boolean) =
        edit { it[KeyManualControls] = enabled }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        dataStore.edit(block)
    }

    private companion object {
        val KeyThemeMode = stringPreferencesKey("theme_mode")
        val KeyDynamicColor = booleanPreferencesKey("dynamic_color")
        val KeyPartyRounds = intPreferencesKey("party_rounds")
        val KeyBoardSize = stringPreferencesKey("tictactoe_board_size")
        val KeyMathOperations = stringSetPreferencesKey("math_operations")
        val KeyRoundDuration = intPreferencesKey("round_duration")
        val KeyQuestionCount = intPreferencesKey("question_count")
        val KeyManualControls = booleanPreferencesKey("manual_controls")
    }
}
