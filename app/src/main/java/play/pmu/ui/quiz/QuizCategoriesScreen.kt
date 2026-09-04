package play.pmu.ui.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import play.pmu.R
import play.pmu.domain.model.TriviaCategory
import play.pmu.ui.components.CategoryPickerScreen

@Composable
fun QuizCategoriesScreen(
    onCategorySelected: (TriviaCategory) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val categories = TriviaCategory.entries
    CategoryPickerScreen(
        title = stringResource(R.string.game_quiz_title),
        subtitle = stringResource(R.string.quiz_pick_category),
        labels = categories.map { stringResource(it.titleRes) },
        onSelect = { index -> onCategorySelected(categories[index]) },
        onNavigateBack = onNavigateBack,
    )
}
