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
            if (index < TriviaCategory.entries.lastIndex) delay(REQUEST_SPACING_MILLIS)
        }

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
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
