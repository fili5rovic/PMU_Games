package play.pmu.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import play.pmu.R
import javax.inject.Inject

/**
 * Odbrojavanje runde pantomime, kao foreground servis.
 *
 * ZASTO SERVIS, a ne coroutine u ViewModel-u:
 * telefon se u ovoj igri drzi na celu i naglo pomera, pa Activity lako izgubi
 * fokus - dodje notifikacija, ekran se zakljuca, igrac slucajno prevuce nagore.
 * Odbrojavanje u `viewModelScope` u tim trenucima nije zasticeno od toga da
 * sistem uspava ili ubije proces, a runda bi morala da tece dalje. Foreground
 * servis daje procesu prioritet i vidljivu notifikaciju sa preostalim vremenom.
 *
 * Tip servisa je `shortService`: kratak zadatak koji je pokrenuo korisnik i koji
 * mora da se dovrsi. Runda traje najduze 90 sekundi, pa se uklapa u ta pravila.
 */
@AndroidEntryPoint
class CharadesTimerService : Service() {

    @Inject
    lateinit var roundTimer: RoundTimer

    /**
     * Sopstveni scope, vezan za zivotni ciklus servisa. Nije GlobalScope - kada
     * servis nestane, u onDestroy se scope otkazuje i coroutine se zaustavlja.
     */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var countdownJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val durationSeconds = intent?.getIntExtra(EXTRA_DURATION_SECONDS, 0) ?: 0
        if (durationSeconds <= 0) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundWithNotification(durationSeconds)
        startCountdown(durationSeconds)
        return START_NOT_STICKY
    }

    private fun startCountdown(durationSeconds: Int) {
        countdownJob?.cancel()
        roundTimer.reset(durationSeconds)
        countdownJob = serviceScope.launch {
            for (secondsLeft in durationSeconds downTo 1) {
                roundTimer.update(secondsLeft)
                updateNotification(secondsLeft)
                delay(TICK_MILLIS)
            }
            roundTimer.markFinished()
            stopSelf()
        }
    }

    private fun buildNotification(secondsLeft: Int): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_charades)
            .setContentTitle(getString(R.string.notification_round_title))
            .setContentText(getString(R.string.notification_round_text, secondsLeft))
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun startForegroundWithNotification(secondsLeft: Int) {
        // Od API 34 svaki foreground servis mora da prijavi svoj tip.
        val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(secondsLeft),
            serviceType,
        )
    }

    private fun updateNotification(secondsLeft: Int) {
        val manager = NotificationManagerCompat.from(this)
        // Bez dozvole za notifikacije poziv se tiho ignorise, a runda i dalje tece.
        if (manager.areNotificationsEnabled()) {
            manager.notify(NOTIFICATION_ID, buildNotification(secondsLeft))
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW,
        )
            .setName(getString(R.string.notification_channel_name))
            .setDescription(getString(R.string.notification_channel_desc))
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    /** Servis se ne vezuje (nema Binder) - komunikacija ide preko [RoundTimer]. */
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "charades_round"
        private const val NOTIFICATION_ID = 1
        private const val EXTRA_DURATION_SECONDS = "duration_seconds"
        private const val TICK_MILLIS = 1_000L

        fun start(context: Context, durationSeconds: Int) {
            val intent = Intent(context, CharadesTimerService::class.java)
                .putExtra(EXTRA_DURATION_SECONDS, durationSeconds)
            // ContextCompat pokriva i Android 7 (minSdk 24), gde startForegroundService ne postoji.
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CharadesTimerService::class.java))
        }
    }
}
