package play.pmu.ui.charades

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import play.pmu.R
import play.pmu.domain.model.CharadesCategory
import play.pmu.ui.components.CategoryPickerScreen

/** Izbor kategorije pojmova za pantomimu. */
@Composable
fun CharadesCategoriesScreen(
    onCategorySelected: (CharadesCategory) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val categories = CharadesCategory.entries
    CategoryPickerScreen(
        title = stringResource(R.string.game_charades_title),
        subtitle = stringResource(R.string.charades_pick_category),
        labels = categories.map { stringResource(it.titleRes) },
        onSelect = { index -> onCategorySelected(categories[index]) },
        onNavigateBack = onNavigateBack,
    )
}
