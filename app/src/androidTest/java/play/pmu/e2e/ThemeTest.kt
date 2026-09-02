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

/**
 * Provera da izbor teme stize i do EKRANA IGARA, a ne samo do menija.
 *
 * Ne uporedjuju se piksele ni snimci ekrana. Umesto toga se ista tri izvora boja
 * koje koriste svi ekrani igara - `MaterialTheme.colorScheme`, `accentColor` i
 * `areaColor` - iscitaju u svetloj i u tamnoj temi i uporede. Ako bi neki ekran
 * ostao "beo" u tamnoj temi, to bi bilo zato sto boju NE uzima iz ovih izvora,
 * a to se u kodu vidi odmah.
 *
 * Obe teme se prikazuju u jednom prolazu, pa test ne zavisi od redosleda ni od
 * ponovnog pokretanja Activity-ja.
 */
@RunWith(AndroidJUnit4::class)
class ThemeTest {

    @get:Rule
    val composeRule = createComposeRule()

    /** Ispisuje vrednosti boja kao tekst, da ih test moze procitati. */
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
            // Prava komponenta sa ekrana igre, da se vidi da se i ona iscrtava
            // u obe teme.
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

        // Tema se prepoznaje iz same seme, pa vazi i za rucni izbor u podesavanjima.
        assertEquals("false", composeRule.textOfTag(LIGHT + "_dark"))
        assertEquals("true", composeRule.textOfTag(DARK + "_dark"))

        // Podloga ekrana igre se razlikuje - u tamnoj temi nije bela.
        assertNotEquals(
            composeRule.textOfTag(LIGHT + "_surface"),
            composeRule.textOfTag(DARK + "_surface"),
        )

        // Boja igraca ima svoju varijantu po temi, pa tekst ostaje citljiv...
        assertNotEquals(
            composeRule.textOfTag(LIGHT + "_accent"),
            composeRule.textOfTag(DARK + "_accent"),
        )
        // ...a i podloga polovine ekrana koja se racuna iz nje.
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
