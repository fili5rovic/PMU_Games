package play.pmu.ui.charades

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.service.CharadesTimerService
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.components.TimerBar
import play.pmu.ui.theme.CorrectGreen
import play.pmu.ui.theme.WrongRed

@Composable
fun CharadesScreen(
    onNavigateBack: () -> Unit,
    onRoundFinished: (Long) -> Unit,
    viewModel: CharadesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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

    // Servis se pokrece kada je ViewModel procitao trajanje runde iz podesavanja,
    // a zaustavlja kada ekran nestane (ili kada se runda zavrsi sama).
    val duration = uiState.roundDurationSeconds
    LaunchedEffect(duration) {
        if (duration > 0) CharadesTimerService.start(context, duration)
    }
    LaunchedEffect(uiState.isRoundOver) {
        if (uiState.isRoundOver) CharadesTimerService.stop(context)
    }

    LaunchedEffect(uiState.finishedResultId) {
        uiState.finishedResultId?.let(onRoundFinished)
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
    // Kratak zeleni/crveni bljesak preko celog ekrana kao potvrda pokreta.
    val feedbackColor by animateColorAsState(
        targetValue = when (uiState.feedback) {
            CharadesFeedback.CORRECT -> CorrectGreen
            CharadesFeedback.SKIPPED -> WrongRed
            CharadesFeedback.NONE -> MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = FEEDBACK_FADE_MILLIS),
        label = "feedbackColor",
    )
    LaunchedEffect(uiState.feedback) {
        if (uiState.feedback != CharadesFeedback.NONE) onFeedbackShown()
    }

    val hasFeedback = uiState.feedback != CharadesFeedback.NONE
    val contentColor =
        if (hasFeedback) Color.White else MaterialTheme.colorScheme.onSurface

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_charades_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(feedbackColor)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
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
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = contentColor,
                    )
                }
            }

            Text(
                text = stringResource(
                    if (uiState.showManualControls) {
                        R.string.charades_manual_hint
                    } else {
                        R.string.charades_tilt_hint
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                textAlign = TextAlign.Center,
            )

            // Rezervne kontrole: bez njih se igra ne bi mogla demonstrirati na emulatoru.
            if (uiState.showManualControls) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.charades_skip))
                    }
                    Button(onClick = onCorrect, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.charades_correct))
                    }
                }
            }
        }
    }
}

private const val FEEDBACK_FADE_MILLIS = 180
