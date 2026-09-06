package play.pmu.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import play.pmu.ui.charades.CharadesCategoriesScreen
import play.pmu.ui.charades.CharadesScreen
import play.pmu.ui.home.HomeScreen
import play.pmu.ui.party.PartyResultScreen
import play.pmu.ui.party.PartyRoundScreen
import play.pmu.ui.party.PartyViewModel
import play.pmu.ui.quiz.QuizCategoriesScreen
import play.pmu.ui.quiz.QuizScreen
import play.pmu.ui.result.ResultScreen
import play.pmu.ui.settings.SettingsScreen
import play.pmu.ui.solo.SoloGameScreen
import play.pmu.ui.statistics.StatisticsScreen
import play.pmu.ui.stepbystep.StepByStepScreen

@Composable
fun PmuNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = HomeRoute) {

        composable<HomeRoute> {
            HomeScreen(
                onPartyClick = { navController.navigate(PartyGraph) },
                onMiniGameClick = { miniGame -> navController.navigate(miniGame.soloRoute()) },
                onGameClick = { gameType -> navController.navigate(gameType.startRoute()) },
                onStatisticsClick = { navController.navigate(StatisticsRoute) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }

        navigation<PartyGraph>(startDestination = PartyRoundRoute(round = 0)) {

            composable<PartyRoundRoute> { entry ->
                val round = entry.toRoute<PartyRoundRoute>().round
                val viewModel = entry.partyViewModel(navController)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PartyRoundScreen(
                    round = round,
                    uiState = uiState,
                    onRoundFinished = { outcome -> viewModel.onRoundFinished(round, outcome) },
                    onNextRound = {
                        navController.navigate(PartyRoundRoute(round + 1)) {
                            popUpTo<PartyRoundRoute> { inclusive = true }
                        }
                    },
                    onPartyFinished = {
                        navController.navigate(PartyResultRoute) {
                            popUpTo<PartyRoundRoute> { inclusive = true }
                        }
                    },
                )
            }

            composable<PartyResultRoute> { entry ->
                val viewModel = entry.partyViewModel(navController)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PartyResultScreen(
                    uiState = uiState,
                    onSaveMatch = viewModel::saveMatch,
                    onNewParty = {
                        navController.navigate(PartyGraph) {
                            popUpTo<PartyGraph> { inclusive = true }
                        }
                    },
                    onHome = {
                        navController.navigate(HomeRoute) {
                            popUpTo<HomeRoute> { inclusive = true }
                        }
                    },
                )
            }
        }

        composable<SoloGameRoute> { entry ->
            val route = entry.toRoute<SoloGameRoute>()

            SoloGameScreen(
                attempt = route.attempt,
                winsOne = route.winsOne,
                winsTwo = route.winsTwo,
                onPlayAgain = { winsOne, winsTwo ->
                    navController.navigate(
                        route.copy(
                            attempt = route.attempt + 1,
                            winsOne = winsOne,
                            winsTwo = winsTwo,
                            startsWithPlayerOne = !route.startsWithPlayerOne,
                        )
                    ) {
                        popUpTo<SoloGameRoute> { inclusive = true }
                    }
                },
                onNavigateBack = navController::popBackStack,
            )
        }

        composable<CharadesCategoriesRoute> {
            CharadesCategoriesScreen(
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

        composable<QuizCategoriesRoute> {
            QuizCategoriesScreen(
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

        composable<StepByStepGameRoute> {
            StepByStepScreen(
                onNavigateBack = navController::popBackStack,
                onGameFinished = { resultId -> navController.toResult(resultId) },
            )
        }

        composable<ResultRoute> {
            ResultScreen(
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

@Composable
private fun NavBackStackEntry.partyViewModel(navController: NavHostController): PartyViewModel {
    val graphEntry = remember(this) { navController.getBackStackEntry<PartyGraph>() }
    return hiltViewModel(graphEntry)
}

private fun NavHostController.toResult(resultId: Long) {
    navigate(ResultRoute(resultId)) {
        popUpTo<HomeRoute>()
    }
}
