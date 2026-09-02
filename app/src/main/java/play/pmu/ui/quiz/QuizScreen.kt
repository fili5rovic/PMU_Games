package play.pmu.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.data.repository.QuestionSource
import play.pmu.ui.components.ErrorView
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.theme.CorrectGreenLight
import play.pmu.ui.theme.WrongRedLight

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    onQuizFinished: (Long) -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is QuizUiState.Finished) onQuizFinished(state.resultId)
    }

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_quiz_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        // Tri jasna stanja mreznog poziva: ucitavanje, greska, uspeh.
        when (val state = uiState) {
            QuizUiState.Loading -> LoadingView(
                message = stringResource(R.string.quiz_loading),
                modifier = Modifier.padding(padding),
            )

            QuizUiState.Error -> ErrorView(
                message = stringResource(R.string.quiz_error_generic),
                modifier = Modifier.padding(padding),
                onRetry = viewModel::loadQuestions,
            )

            is QuizUiState.Playing -> QuizContent(
                state = state,
                onAnswerSelected = viewModel::selectAnswer,
                onNext = viewModel::nextQuestion,
                modifier = Modifier.padding(padding),
            )

            is QuizUiState.Finished -> LoadingView(
                message = stringResource(R.string.quiz_loading),
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun QuizContent(
    state: QuizUiState.Playing,
    onAnswerSelected: (String) -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LinearProgressIndicator(
            progress = { (state.questionIndex + 1).toFloat() / state.totalQuestions },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(
                R.string.quiz_progress,
                state.questionIndex + 1,
                state.totalQuestions,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // Kada pitanja ne dolaze sa mreze, korisnik dobija diskretno objasnjenje zasto.
        if (state.source != QuestionSource.NETWORK) {
            ScoreBadge(
                text = stringResource(
                    if (state.source == QuestionSource.CACHE) {
                        R.string.quiz_source_cache
                    } else {
                        R.string.quiz_error_offline
                    }
                )
            )
        }

        // Prelaz izmedju pitanja je blago pretapanje, da promena ne bude nagla.
        AnimatedContent(
            targetState = state.questionIndex,
            transitionSpec = {
                fadeIn(tween(250)) togetherWith fadeOut(tween(150))
            },
            label = "questionTransition",
        ) { index ->
            val question = state.questions[index]
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.titleLarge,
                )
                question.answers.forEach { answer ->
                    AnswerCard(
                        answer = answer,
                        selectedAnswer = state.selectedAnswer,
                        correctAnswer = question.correctAnswer,
                        onClick = { onAnswerSelected(answer) },
                    )
                }
            }
        }

        if (state.selectedAnswer != null) {
            Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(
                    stringResource(
                        if (state.isLastQuestion) R.string.result_title else R.string.action_play
                    )
                )
            }
        }
    }
}

/**
 * Kartica jednog odgovora. Dok nije odgovoreno, sve su neutralne; posle izbora
 * tacan odgovor postaje zelen, a pogresan izbor crven.
 */
@Composable
private fun AnswerCard(
    answer: String,
    selectedAnswer: String?,
    correctAnswer: String,
    onClick: () -> Unit,
) {
    val isAnswered = selectedAnswer != null
    val containerColor = when {
        !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
        answer == correctAnswer -> CorrectGreenLight
        answer == selectedAnswer -> WrongRedLight
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    // Bela je namerna samo preko zelene/crvene kartice (tacno/netacno); sve
    // ostalo uzima boju iz teme.
    val contentColor = when {
        !isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
        answer == correctAnswer || answer == selectedAnswer -> Color.White
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        enabled = !isAnswered,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = contentColor,
        ),
    ) {
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}
