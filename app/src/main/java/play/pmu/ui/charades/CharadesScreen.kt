package play.pmu.ui.charades

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import play.pmu.R
import play.pmu.service.CharadesTimerService
import play.pmu.domain.model.GameType
import play.pmu.ui.components.GameIntro
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.LockScreenOrientation
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.components.TimerBar
import play.pmu.ui.theme.CorrectGreen
import play.pmu.ui.theme.GameTouchTargetSize
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.WrongRed

@Composable
fun CharadesScreen(
    onNavigateBack: () -> Unit,
    onRoundFinished: (Long) -> Unit,
    viewModel: CharadesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Orijentacija se cita iz metapodataka igre (GameType.orientation), pa je
    // pravilo "pantomima je landscape" zapisano na jednom mestu. Po izlasku sa
    // ekrana LockScreenOrientation sam vraca portret.
    LockScreenOrientation(GameType.CHARADES.orientation)

    // Notifikacija foreground servisa se od Androida 13 prikazuje samo sa dozvolom.
    // Ako je korisnik odbije, runda i dalje radi - samo nema notifikacije.
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Servis se pokrece kada runda stvarno pocne (posle uputstva), a zaustavlja
    // kada se runda zavrsi ili kada se ekran napusti.
    val duration = uiState.roundDurationSeconds
    LaunchedEffect(uiState.isRoundStarted) {
        if (uiState.isRoundStarted && duration > 0) {
            CharadesTimerService.start(context, duration)
        }
    }
    LaunchedEffect(uiState.isRoundOver) {
        if (uiState.isRoundOver) CharadesTimerService.stop(context)
    }

    LaunchedEffect(uiState.finishedResultId) {
        uiState.finishedResultId?.let(onRoundFinished)
    }

    // Pojmovi i trajanje runde se citaju iz resursa i podesavanja; do tada nema
    // sta da se prikaze.
    if (duration == 0) {
        LoadingView(message = stringResource(R.string.game_get_ready))
        return
    }

    if (!uiState.isRoundStarted) {
        GameIntro(
            titleRes = R.string.game_charades_title,
            instructionRes = if (uiState.showManualControls) {
                R.string.charades_manual_hint
            } else {
                R.string.charades_tilt_hint
            },
            onStart = viewModel::startRound,
            // Telefon drzi jedan igrac na celu, pa uputstvo nije podeljeno na dva dela.
            forBothPlayers = false,
        )
        return
    }

    CharadesContent(
        uiState = uiState,
        onCorrect = viewModel::registerCorrect,
        onSkip = viewModel::registerSkip,
        onFeedbackShown = viewModel::clearFeedback,
        onNavigateBack = {
            CharadesTimerService.stop(context)
            onNavigateBack()
        },
    )
}

@Composable
private fun CharadesContent(
    uiState: CharadesUiState,
    onCorrect: () -> Unit,
    onSkip: () -> Unit,
    onFeedbackShown: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val hasFeedback = uiState.feedback != CharadesFeedback.NONE

    // Poslednji ishod se pamti da bi overlay zadrzao svoju boju i tekst i dok se
    // gasi: u tom trenutku je uiState.feedback vec NONE.
    var lastFeedback by remember { mutableStateOf(CharadesFeedback.CORRECT) }
    LaunchedEffect(uiState.feedback) {
        if (uiState.feedback == CharadesFeedback.NONE) return@LaunchedEffect
        lastFeedback = uiState.feedback
        // Bez ovog zadrzavanja bi se overlay ugasio u istom kadru u kome se
        // pojavio, pa igrac ne bi ni video da li je pokret primljen.
        delay(FEEDBACK_HOLD_MILLIS)
        onFeedbackShown()
    }

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_charades_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(PmuSpacing.large),
                verticalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimerBar(
                    secondsLeft = uiState.secondsLeft,
                    totalSeconds = uiState.roundDurationSeconds,
                )
                ScoreBadge(text = uiState.score.toString())

                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    // Novi pojam se pojavljuje pretapanjem, pa je promena jasno vidljiva.
                    AnimatedContent(
                        targetState = uiState.currentWord,
                        transitionSpec = {
                            (fadeIn(tween(200)) + scaleIn(initialScale = 0.9f)) togetherWith
                                fadeOut(tween(120))
                        },
                        label = "wordTransition",
                    ) { word ->
                        Text(
                            text = word,
                            style = MaterialTheme.typography.displayMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                // Uputstvo se NE ponavlja u toku runde - procitano je pre nje, a
                // igrac u ovoj igri gleda samo pojam.

                // Rezervne kontrole: bez njih se igra ne bi mogla demonstrirati na emulatoru.
                if (uiState.showManualControls) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f).heightIn(min = GameTouchTargetSize),
                        ) {
                            Text(stringResource(R.string.charades_skip))
                        }
                        Button(
                            onClick = onCorrect,
                            modifier = Modifier.weight(1f).heightIn(min = GameTouchTargetSize),
                        ) {
                            Text(stringResource(R.string.charades_correct))
                        }
                    }
                }
            }

            /*
             * Povratna informacija o pokretu: preko CELOG ekrana, zeleno za
             * pogodak i crveno za preskakanje, sa krupnom recju u sredini.
             * Telefon je na celu i igrac ga u tom trenutku samo krajem oka vidi,
             * pa mora da bude nemoguce promasiti.
             *
             * Jedan fizicki pokret se broji tacno jednom: za to se brine
             * TiltGestureRecognizer (histereza + debounce), a ne ovaj ekran.
             */
            AnimatedVisibility(
                visible = hasFeedback,
                enter = fadeIn(tween(FEEDBACK_FADE_IN_MILLIS)),
                exit = fadeOut(tween(FEEDBACK_FADE_OUT_MILLIS)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (lastFeedback == CharadesFeedback.CORRECT) CorrectGreen else WrongRed
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            if (lastFeedback == CharadesFeedback.CORRECT) {
                                R.string.charades_correct
                            } else {
                                R.string.charades_skip
                            }
                        ),
                        style = MaterialTheme.typography.displayLarge,
                        // Namerno bela: preko zelene/crvene potvrde pokreta, koje
                        // su znacenje a ne stil. Obe su tamne, pa je citljivo u
                        // obe teme.
                        color = Color.White,
                    )
                }
            }
        }
    }
}

private const val FEEDBACK_HOLD_MILLIS = 450L
private const val FEEDBACK_FADE_IN_MILLIS = 80
private const val FEEDBACK_FADE_OUT_MILLIS = 220
