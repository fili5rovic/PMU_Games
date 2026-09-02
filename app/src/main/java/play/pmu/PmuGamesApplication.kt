package play.pmu

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import play.pmu.worker.TriviaPrefetchWorker
import javax.inject.Inject

/**
 * Application klasa je ulazna tacka Hilt-a: @HiltAndroidApp generise osnovnu
 * komponentu iz koje se izvode sve ostale (Activity, ViewModel, Worker).
 *
 * Implementira i Configuration.Provider da bi WorkManager umeo da napravi
 * Worker-e sa ubacenim zavisnostima (HiltWorkerFactory).
 */
@HiltAndroidApp
class PmuGamesApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Zakazivanje je idempotentno (politika KEEP), pa moze pri svakom pokretanju.
        TriviaPrefetchWorker.schedule(this)
    }
}
