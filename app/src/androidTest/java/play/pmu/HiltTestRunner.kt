package play.pmu

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Runner za instrumentacione testove.
 *
 * Umesto prave [PmuGamesApplication] pokrece [HiltTestApplication], bez koje
 * @HiltAndroidTest ne bi mogao da ubaci zavisnosti. Prijavljen je u
 * build.gradle.kts kao testInstrumentationRunner.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(classLoader, HiltTestApplication::class.java.name, context)
}
