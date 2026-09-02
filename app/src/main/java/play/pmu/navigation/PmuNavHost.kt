package play.pmu.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import play.pmu.ui.charades.CharadesCategoriesScreen
import play.pmu.ui.charades.CharadesScreen
import play.pmu.ui.home.HomeScreen
import play.pmu.ui.memory.MemoryScreen
import play.pmu.ui.quiz.QuizCategoriesScreen
import play.pmu.ui.quiz.QuizScreen
import play.pmu.ui.reaction.ReactionScreen
import play.pmu.ui.result.ResultScreen
import play.pmu.ui.settings.SettingsScreen
import play.pmu.ui.statistics.StatisticsScreen

/**
 * Jedini graf navigacije u aplikaciji.
 *
 * Ekrani ne primaju NavController - dobijaju obicne lambde (`onNavigateBack`,
 * `onRoundFinished`...). Tako ni jedan ekran ne zna gde vodi klik, sto ih cini
 * nezavisnim i lakim za @Preview.
 */
@Composable
fun PmuNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HomeRoute) {

        composable<HomeRoute> {
            HomeScreen(
                onGameClick = { gameType -> navController.navigate(gameType.startRoute()) },
                onStatisticsClick = { navController.navigate(StatisticsRoute) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }

        composable<CharadesCategoriesRoute> {
            CharadesCategoriesScreen(
                // Prosledjuje se samo ime enum konstante.
                onCategorySelected = { category ->
                    navController.navigate(CharadesGameRoute(category.name))
                },
                onNavigateBack = navController::popBackStack,
            )
        }

        composable<CharadesGameRoute> {
            CharadesScreen(
                onNavigateBack = navController::popBackStack,
                onRoundFinished = { resultId -> navController.toResult(resultId) },
            )
        }

        composable<ReactionGameRoute> {
            ReactionScreen(
                onNavigateBack = navController::popBackStack,
                onRoundFinished = { resultId -> navController.toResult(resultId) },
            )
        }

        composable<MemoryGameRoute> {
            MemoryScreen(
                onNavigateBack = navController::popBackStack,
                onGameFinished = { resultId -> navController.toResult(resultId) },
            )
        }

        composable<QuizCategoriesRoute> {
            QuizCategoriesScreen(
                // Kroz navigaciju ide samo id kategorije, ne cela kategorija.
                onCategorySelected = { category ->
                    navController.navigate(QuizGameRoute(category.apiId))
                },
                onNavigateBack = navController::popBackStack,
            )
        }

        composable<QuizGameRoute> {
            QuizScreen(
                onNavigateBack = navController::popBackStack,
                onQuizFinished = { resultId -> navController.toResult(resultId) },
            )
        }

        composable<ResultRoute> {
            ResultScreen(
                // Nova partija se pokrece kao nova destinacija, a rezultat se skida
                // sa steka. Vracanje na zavrsenu igru ne bi radilo: njen ViewModel
                // je jos u zavrsnom stanju i odmah bi ponovo otvorio rezultat.
                onPlayAgain = { gameType ->
                    navController.navigate(gameType.startRoute()) {
                        popUpTo<ResultRoute> { inclusive = true }
                    }
                },
                onHome = navController::popBackStack,
            )
        }

        composable<StatisticsRoute> {
            StatisticsScreen(onNavigateBack = navController::popBackStack)
        }

        composable<SettingsRoute> {
            SettingsScreen(onNavigateBack = navController::popBackStack)
        }
    }
}

/**
 * Prelaz na ekran rezultata. Ekran zavrsene igre se izbacuje sa steka, pa
 * "nazad" sa rezultata vodi na pocetni ekran, a ne u vec zavrsenu partiju.
 */
private fun NavHostController.toResult(resultId: Long) {
    navigate(ResultRoute(resultId)) {
        popUpTo<HomeRoute>()
    }
}
