package play.pmu.e2e

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import play.pmu.domain.model.Player
import play.pmu.domain.model.ThemeMode
import play.pmu.ui.components.PlayerAreaLabel
import play.pmu.ui.theme.PmuGamesTheme
import play.pmu.ui.theme.accentColor
import play.pmu.ui.theme.areaColor
import play.pmu.ui.theme.isDarkTheme

@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Composable
    private fun ThemeProbe(prefix: String) {
        Column {
            Text(text = isDarkTheme.toString(), modifier = Modifier.testTag(prefix + "_dark"))
            Text(
                text = MaterialTheme.colorScheme.surface.value.toString(),
                modifier = Modifier.testTag(prefix + "_surface"),
            )
            Text(
                text = Player.ONE.accentColor.value.toString(),
                modifier = Modifier.testTag(prefix + "_accent"),
            )
            Text(
                text = Player.ONE.areaColor().value.toString(),
                modifier = Modifier.testTag(prefix + "_area"),
            )
            PlayerAreaLabel(player = Player.ONE, isActive = true)
        }
    }

    @Test
    fun tema_menja_boje_koje_koriste_ekrani_igara() {
        composeRule.setContent {
            Column {
                PmuGamesTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    ThemeProbe(LIGHT)
                }
                PmuGamesTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                    ThemeProbe(DARK)
                }
            }
        }

        assertEquals("false", composeRule.textOfTag(LIGHT + "_dark"))
        assertEquals("true", composeRule.textOfTag(DARK + "_dark"))

        assertNotEquals(
            composeRule.textOfTag(LIGHT + "_surface"),
            composeRule.textOfTag(DARK + "_surface"),
        )

        assertNotEquals(
            composeRule.textOfTag(LIGHT + "_accent"),
            composeRule.textOfTag(DARK + "_accent"),
        )
        assertNotEquals(
            composeRule.textOfTag(LIGHT + "_area"),
            composeRule.textOfTag(DARK + "_area"),
        )
    }

    @Test
    fun komponenta_ekrana_igre_se_iscrtava_u_obe_teme() {
        composeRule.setContent {
            Column {
                PmuGamesTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    PlayerAreaLabel(player = Player.TWO, isActive = true)
                }
                PmuGamesTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                    PlayerAreaLabel(player = Player.TWO, isActive = true)
                }
            }
        }

        // Ista komponenta, dva puta - jednom u svakoj temi.
        composeRule.onAllNodesWithText(
            InstrumentationText.playerTwo,
        ).assertCountEquals(2)
    }

    private object InstrumentationText {
        val playerTwo: String
            get() = androidx.test.platform.app.InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .getString(play.pmu.R.string.player_two)
    }

    private companion object {
        const val LIGHT = "light"
        const val DARK = "dark"
    }
}
