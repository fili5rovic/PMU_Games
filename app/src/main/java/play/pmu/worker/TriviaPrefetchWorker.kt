package play.pmu.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import play.pmu.data.repository.TriviaRepository
import play.pmu.domain.model.TriviaCategory
import java.util.concurrent.TimeUnit

/**
 * Jednom dnevno osvezava lokalni cache trivia pitanja.
 *
 * ZASTO WORKMANAGER, a ne coroutine u ViewModel-u:
 * posao treba da se izvrsi i kada aplikacija nije otvorena, da preživi restart
 * telefona i da sam pocdeka da se pojavi mreza. To su tacno garancije koje daje
 * WorkManager - `viewModelScope` bi bio otkazan u trenutku kada korisnik zatvori
 * ekran. Rezultat je da kviz i bez interneta ima svez sadrzaj.
 *
 * @HiltWorker + @AssistedInject: Context i WorkerParameters daje WorkManager,
 * a repozitorijum ubacuje Hilt.
 */
@HiltWorker
class TriviaPrefetchWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val triviaRepository: TriviaRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        var refreshedAny = false

        TriviaCategory.entries.forEachIndexed { index, category ->
            if (triviaRepository.refreshCache(category, PREFETCH_COUNT)) refreshedAny = true
            // Open Trivia DB dozvoljava jedan poziv na 5 sekundi po IP adresi.
            // Posao je u pozadini, pa nam pauza ne pravi problem.
            if (index < TriviaCategory.entries.lastIndex) delay(REQUEST_SPACING_MILLIS)
        }

        // retry: WorkManager ce sam probati ponovo, sa rastucim odlaganjem.
        return if (refreshedAny) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "trivia_prefetch"
        private const val PREFETCH_COUNT = 15
        private const val REQUEST_SPACING_MILLIS = 5_000L

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TriviaPrefetchWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.DAYS,
            )
                // Bez mreze posao nema smisla, pa ga sistem uopste ne pokrece.
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            // KEEP: ako je posao vec zakazan, svako novo pokretanje aplikacije
            // ga ostavlja na miru (u suprotnom bi se interval stalno resetovao).
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
