package play.pmu.fake

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Podesavanja u memoriji, za testove.
 *
 * `DataStore` je interface sa samo dva clana, pa se - isto kao Room DAO - lako
 * zameni fake implementacijom. Zahvaljujuci tome se u testu koristi PRAVI
 * SettingsRepository, bez pisanja fajla na disk i bez ijedne dodatne apstrakcije
 * u produkcionom kodu.
 */
class FakePreferencesDataStore : DataStore<Preferences> {

    private val preferences = MutableStateFlow(emptyPreferences())

    override val data: Flow<Preferences> = preferences

    override suspend fun updateData(
        transform: suspend (Preferences) -> Preferences,
    ): Preferences {
        val updated = transform(preferences.value)
        preferences.value = updated
        return updated
    }
}
