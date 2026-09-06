package play.pmu.ui.stepbystep

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.ErrorView
import play.pmu.ui.components.LoadingView
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.ScoreBadge
import play.pmu.ui.theme.CorrectGreen
import play.pmu.ui.theme.CorrectGreenLight
import play.pmu.ui.theme.WrongRed
import play.pmu.ui.theme.WrongRedLight

@Composable
fun StepByStepScreen(
    onNavigateBack: () -> Unit,
    onGameFinished: (Long) -> Unit,
    viewModel: StepByStepViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.game_step_by_step_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> LoadingView(
                message = stringResource(R.string.quiz_loading),
                modifier = Modifier.padding(padding),
            )

            uiState.puzzle == null -> ErrorView(
                message = stringResource(R.string.result_missing),
                modifier = Modifier.padding(padding),
            )

            else -> StepByStepContent(
                state = uiState,
                onGuessChanged = viewModel::onGuessChanged,
                onSubmitGuess = viewModel::submitGuess,
                onNextClue = viewModel::nextClue,
                onFinish = {
                    uiState.finishedResultId?.let { onGameFinished(it) }
                },
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun StepByStepContent(
    state: StepByStepUiState,
    onGuessChanged: (String) -> Unit,
    onSubmitGuess: () -> Unit,
    onNextClue: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    R.string.step_by_step_step_of,
                    state.currentStepNumber,
                    state.totalSteps,
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            ScoreBadge(
                text = stringResource(
                    R.string.step_by_step_points_available,
                    if (state.isFinished) state.score else state.currentPotentialPoints,
                )
            )
        }

        LinearProgressIndicator(
            progress = { state.currentStepNumber.toFloat() / state.totalSteps },
            modifier = Modifier.fillMaxWidth(),
        )

        // Clues List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            val clues = state.puzzle?.clues ?: emptyList()
            itemsIndexed(clues) { index, clueText ->
                val isRevealed = index <= state.currentStepIndex || state.isFinished
                val isCurrent = index == state.currentStepIndex && !state.isFinished

                ClueCard(
                    stepNumber = index + 1,
                    clue = if (isRevealed) clueText else "???",
                    isRevealed = isRevealed,
                    isCurrent = isCurrent,
                )
            }
        }

        if (state.isFinished) {
            FinishCard(
                isGuessedCorrectly = state.isGuessedCorrectly,
                solution = state.puzzle?.solution.orEmpty(),
                score = state.score,
                onFinish = onFinish,
            )
        } else {
            InputArea(
                inputGuess = state.inputGuess,
                isWrongGuess = state.isWrongGuess,
                currentStepIndex = state.currentStepIndex,
                onGuessChanged = onGuessChanged,
                onSubmitGuess = {
                    focusManager.clearFocus()
                    onSubmitGuess()
                },
                onNextClue = {
                    focusManager.clearFocus()
                    onNextClue()
                },
            )
        }
    }
}

@Composable
private fun ClueCard(
    stepNumber: Int,
    clue: String,
    isRevealed: Boolean,
    isCurrent: Boolean,
) {
    val containerColor = when {
        isCurrent -> MaterialTheme.colorScheme.primaryContainer
        isRevealed -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    val contentColor = when {
        isCurrent -> MaterialTheme.colorScheme.onPrimaryContainer
        isRevealed -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    }
    val border = if (isCurrent) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isRevealed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                contentColor = Color.White,
            ) {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = clue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun InputArea(
    inputGuess: String,
    isWrongGuess: Boolean,
    currentStepIndex: Int,
    onGuessChanged: (String) -> Unit,
    onSubmitGuess: () -> Unit,
    onNextClue: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isWrongGuess) {
            Text(
                text = stringResource(R.string.step_by_step_wrong_msg),
                style = MaterialTheme.typography.bodySmall,
                color = WrongRed,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        OutlinedTextField(
            value = inputGuess,
            onValueChange = onGuessChanged,
            placeholder = { Text(stringResource(R.string.step_by_step_input_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(PmuTestTags.STEP_BY_STEP_INPUT),
            singleLine = true,
            isError = isWrongGuess,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmitGuess() }),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onNextClue,
                modifier = Modifier
                    .weight(1f)
                    .testTag(PmuTestTags.STEP_BY_STEP_NEXT_BUTTON),
            ) {
                Text(
                    stringResource(
                        if (currentStepIndex < 6) R.string.step_by_step_action_next_clue
                        else R.string.step_by_step_action_reveal
                    )
                )
            }

            Button(
                onClick = onSubmitGuess,
                enabled = inputGuess.isNotBlank(),
                modifier = Modifier
                    .weight(1f)
                    .testTag(PmuTestTags.STEP_BY_STEP_GUESS_BUTTON),
            ) {
                Text(stringResource(R.string.step_by_step_action_guess))
            }
        }
    }
}

@Composable
private fun FinishCard(
    isGuessedCorrectly: Boolean,
    solution: String,
    score: Int,
    onFinish: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGuessedCorrectly) CorrectGreenLight.copy(alpha = 0.15f)
            else WrongRedLight.copy(alpha = 0.15f),
        ),
        border = BorderStroke(
            1.dp,
            if (isGuessedCorrectly) CorrectGreen else WrongRed,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(
                    if (isGuessedCorrectly) R.string.step_by_step_correct_title
                    else R.string.step_by_step_game_over_title
                ),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isGuessedCorrectly) CorrectGreen else WrongRed,
            )

            Text(
                text = stringResource(R.string.step_by_step_solution_was, solution),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            if (isGuessedCorrectly) {
                Text(
                    text = stringResource(R.string.step_by_step_points_available, score),
                    style = MaterialTheme.typography.titleMedium,
                    color = CorrectGreen,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onFinish,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(PmuTestTags.STEP_BY_STEP_FINISH_BUTTON),
            ) {
                Text(stringResource(R.string.step_by_step_action_finish))
            }
        }
    }
}
