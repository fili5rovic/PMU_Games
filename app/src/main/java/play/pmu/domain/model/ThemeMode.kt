package play.pmu.domain.model

import androidx.annotation.StringRes
import play.pmu.R

enum class ThemeMode(@StringRes val titleRes: Int) {
    SYSTEM(R.string.settings_theme_system),
    LIGHT(R.string.settings_theme_light),
    DARK(R.string.settings_theme_dark),
}
