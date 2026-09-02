package play.pmu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import play.pmu.ui.theme.PmuSpacing

/**
 * Red cipova za izbor JEDNE vrednosti (broj rundi, velicina table...).
 *
 * Komponenta je genericka po tipu opcije, pa istu koriste i podesavanja i ekran
 * pripreme partije - bez obzira na to da li se bira Int ili enum. `FlowRow`
 * prelama cipove u novi red kada ih ima previse za sirinu ekrana.
 */
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

/**
 * Red cipova za izbor VISE vrednosti (dozvoljene racunske operacije).
 *
 * Poslednja ukljucena opcija se ne moze ugasiti: racunski duel bez ijedne
 * operacije ne bi mogao da napravi pitanje. To pravilo je ovde, u komponenti,
 * pa se ne mora ponavljati na svakom ekranu koji je koristi.
 */
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
