package play.pmu.ui.settings

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import play.pmu.R
import play.pmu.data.repository.AppSettings
import play.pmu.domain.model.BoardSizeOption
import play.pmu.domain.model.MathOperation
import play.pmu.domain.model.ThemeMode
import play.pmu.ui.PmuTestTags
import play.pmu.ui.components.MultiChoiceChips
import play.pmu.ui.components.PmuTopAppBar
import play.pmu.ui.components.SingleChoiceChips
import play.pmu.ui.theme.PmuSpacing
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsContent(
        settings = settings,
        onThemeModeChange = viewModel::setThemeMode,
        onDynamicColorChange = viewModel::setDynamicColor,
        onPartyRoundsChange = viewModel::setPartyRounds,
        onBoardSizeChange = viewModel::setTicTacToeBoardSize,
        onMathOperationsChange = viewModel::setMathOperations,
        onRoundDurationChange = viewModel::setRoundDuration,
        onQuestionCountChange = viewModel::setQuestionCount,
        onManualControlsChange = viewModel::setManualControls,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun SettingsContent(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onPartyRoundsChange: (Int) -> Unit,
    onBoardSizeChange: (BoardSizeOption) -> Unit,
    onMathOperationsChange: (Set<MathOperation>) -> Unit,
    onRoundDurationChange: (Int) -> Unit,
    onQuestionCountChange: (Int) -> Unit,
    onManualControlsChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            PmuTopAppBar(
                title = stringResource(R.string.settings_title),
                onNavigateBack = onNavigateBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PmuSpacing.medium)
                .testTag(PmuTestTags.SETTINGS_SCREEN),
            verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
        ) {
            SectionTitle(stringResource(R.string.settings_appearance))

            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            modifier = Modifier.testTag("theme_" + mode.name),
                        )
                        Text(stringResource(mode.titleRes))
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SwitchRow(
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_desc),
                    checked = settings.dynamicColor,
                    onCheckedChange = onDynamicColorChange,
                )
            }

            HorizontalDivider(Modifier.padding(vertical = PmuSpacing.small))
            SectionTitle(stringResource(R.string.settings_language))

            SingleChoiceChips(
                options = listOf("en", "sr"),
                selected = AppCompatDelegate.getApplicationLocales()
                    .get(0)?.language ?: "en",
                label = { language ->
                    when (language) {
                        "sr" -> stringResource(R.string.language_serbian)
                        else -> stringResource(R.string.language_english)
                    }
                },
                onSelect = ::setLanguage,
            )

            HorizontalDivider(Modifier.padding(vertical = PmuSpacing.small))
            SectionTitle(
                text = stringResource(R.string.settings_game_rules),
                modifier = Modifier.testTag(PmuTestTags.GAME_RULES),
            )

            GameRule(
                gameRes = R.string.party_title,
                optionRes = R.string.settings_party_rounds,
            ) {
                SingleChoiceChips(
                    options = PARTY_ROUNDS,
                    selected = settings.partyRounds,
                    label = { it.toString() },
                    onSelect = onPartyRoundsChange,
                )
            }

            GameRule(
                gameRes = R.string.game_tictactoe_title,
                optionRes = R.string.settings_board_size,
            ) {
                SingleChoiceChips(
                    options = BoardSizeOption.entries,
                    selected = settings.games.ticTacToeBoardSize,
                    label = { stringResource(it.titleRes) },
                    onSelect = onBoardSizeChange,
                )
            }

            GameRule(
                gameRes = R.string.game_math_duel_title,
                optionRes = R.string.settings_math_operations,
            ) {
                MultiChoiceChips(
                    options = MathOperation.entries,
                    selected = settings.games.mathOperations,
                    label = { it.symbol },
                    onSelectionChange = onMathOperationsChange,
                )
            }

            GameRule(
                gameRes = R.string.game_charades_title,
                optionRes = R.string.settings_round_duration,
            ) {
                SingleChoiceChips(
                    options = ROUND_DURATIONS,
                    selected = settings.roundDurationSeconds,
                    label = { stringResource(R.string.settings_seconds, it) },
                    onSelect = onRoundDurationChange,
                )
                SwitchRow(
                    title = stringResource(R.string.settings_manual_controls),
                    subtitle = stringResource(R.string.settings_manual_controls_desc),
                    checked = settings.manualCharadesControls,
                    onCheckedChange = onManualControlsChange,
                )
            }

            GameRule(
                gameRes = R.string.game_quiz_title,
                optionRes = R.string.settings_question_count,
            ) {
                SingleChoiceChips(
                    options = QUESTION_COUNTS,
                    selected = settings.questionCount,
                    label = { it.toString() },
                    onSelect = onQuestionCountChange,
                )
            }
        }
    }
}

@Composable
private fun GameRule(
    @StringRes gameRes: Int,
    @StringRes optionRes: Int,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = PmuSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        Text(
            text = stringResource(gameRes),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(optionRes),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = PmuSpacing.small),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.medium),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun setLanguage(language: String) {
    val locales = if (language == "sr") {
        LocaleListCompat.forLanguageTags("sr")
    } else {
        LocaleListCompat.forLanguageTags("en")
    }

    AppCompatDelegate.setApplicationLocales(locales)
}

private val PARTY_ROUNDS = listOf(5, 7, 9)
private val ROUND_DURATIONS = listOf(30, 60, 90)
private val QUESTION_COUNTS = listOf(5, 10, 15)
