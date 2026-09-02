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
import play.pmu.ui.party.PartyStartScreen
import play.pmu.ui.party.PartyViewModel
import play.pmu.ui.quiz.QuizCategoriesScreen
import play.pmu.ui.quiz.QuizScreen
import play.pmu.ui.result.ResultScreen
import play.pmu.ui.settings.SettingsScreen
import play.pmu.ui.solo.SoloGameScreen
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
                onPartyClick = { navController.navigate(PartyGraph) },
                onMiniGameClick = { miniGame -> navController.navigate(miniGame.soloRoute()) },
                onGameClick = { gameType -> navController.navigate(gameType.startRoute()) },
                onStatisticsClick = { navController.navigate(StatisticsRoute) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
        }

        // --- Partija ---------------------------------------------------------
        // Ugnjezdeni graf: PartyViewModel se vezuje za graf, pa jedna instanca
        // vodi celu partiju, a svaka runda je i dalje svoja destinacija sa
        // svojim ViewModel-om mini igre.
        navigation<PartyGraph>(startDestination = PartyStartRoute) {

            composable<PartyStartRoute> { entry ->
                val viewModel = entry.partyViewModel(navController)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PartyStartScreen(
                    uiState = uiState,
                    onBoardSizeChange = viewModel::setTicTacToeBoardSize,
                    onMathOperationsChange = viewModel::setMathOperations,
                    // Nastavlja se od runde koja je na redu. Za novu partiju to je
                    // runda 0, a ako su se igraci vratili "nazad" iz partije,
                    // nastavlja se tamo gde su stali - odigrana runda se nikada ne
                    // otvara ponovo.
                    onStart = {
                        if (uiState.isFinished) {
                            navController.navigate(PartyResultRoute)
                        } else {
                            navController.navigate(PartyRoundRoute(round = uiState.roundIndex))
                        }
                    },
                    onNavigateBack = navController::popBackStack,
                )
            }

            composable<PartyRoundRoute> { entry ->
                val round = entry.toRoute<PartyRoundRoute>().round
                val viewModel = entry.partyViewModel(navController)
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                PartyRoundScreen(
                    round = round,
                    uiState = uiState,
                    onRoundFinished = { outcome -> viewModel.onRoundFinished(round, outcome) },
                    // popUpTo skida odigranu rundu sa steka, pa stek ne raste i
                    // "nazad" iz runde vodi na pocetak partije, a ne u vec
                    // odigranu rundu.
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
                    // Nova partija se pokrece kao nov ulaz u graf: stari graf se
                    // skida sa steka, pa se sa njim brise i PartyViewModel -
                    // nova partija tako pocinje od nule bez ijedne reset metode.
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

        // --- Pojedinacna mini igra -------------------------------------------
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
                            // Sledeca runda pocinje obrnutim redom.
                            startsWithPlayerOne = !route.startsWithPlayerOne,
                        )
                    ) {
                        popUpTo<SoloGameRoute> { inclusive = true }
                    }
                },
                onNavigateBack = navController::popBackStack,
            )
        }

        // --- Pantomima i kviz -------------------------------------------------
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
 * PartyViewModel vezan za ugnjezdeni graf partije, a ne za pojedinacnu rundu.
 *
 * `getBackStackEntry` vraca ulaz samog grafa; posto sve runde traze ViewModel
 * preko TOG ulaza, sve dobijaju istu instancu. `remember` je tu da se ulaz ne
 * trazi pri svakoj rekompoziciji.
 */
@Composable
private fun NavBackStackEntry.partyViewModel(navController: NavHostController): PartyViewModel {
    val graphEntry = remember(this) { navController.getBackStackEntry<PartyGraph>() }
    return hiltViewModel(graphEntry)
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
