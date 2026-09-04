package play.pmu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import play.pmu.ui.theme.PmuSpacing

@Composable
fun <T> SingleChoiceChips(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
fun <T> MultiChoiceChips(
    options: List<T>,
    selected: Set<T>,
    label: @Composable (T) -> String,
    onSelectionChange: (Set<T>) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PmuSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        options.forEach { option ->
            val isSelected = option in selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    val next = if (isSelected) selected - option else selected + option
                    if (next.isNotEmpty()) onSelectionChange(next)
                },
                label = { Text(label(option)) },
            )
        }
    }
}
