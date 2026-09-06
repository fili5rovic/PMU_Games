package play.pmu.ui.quiz

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import android.content.Context
import android.content.ContextWrapper
import play.pmu.data.local.GameResultDao
import play.pmu.data.local.GameResultEntity
import play.pmu.data.local.TriviaQuestionDao
import play.pmu.data.local.TriviaQuestionEntity
import play.pmu.data.remote.TriviaApi
import play.pmu.data.remote.TriviaQuestionDto
import play.pmu.data.remote.TriviaResponseDto
import play.pmu.data.repository.GameResultsRepository
import play.pmu.data.repository.QuestionSource
import play.pmu.data.repository.SettingsRepository
import play.pmu.data.repository.TriviaRepository
import play.pmu.data.translation.TriviaTranslator
import play.pmu.domain.model.GameType
import play.pmu.domain.model.TriviaCategory
import play.pmu.domain.model.TriviaQuestion
import java.io.File
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val dispatcher = StandardTestDispatcher()

    private val testQuestion = TriviaQuestion(
        question = "What is the capital of France?",
        correctAnswer = "Paris",
        answers = listOf("Paris", "London", "Berlin", "Rome"),
    )

    private val translatedQuestion = TriviaQuestion(
        question = "Koji je glavni grad Francuske?",
        correctAnswer = "Pariz",
        answers = listOf("Pariz", "London", "Berlin", "Rim"),
    )

    private class FakeTranslator(
        private val textMapping: Map<String, String> = mapOf(
            "What is the capital of France?" to "Koji je glavni grad Francuske?",
            "Paris" to "Pariz",
            "London" to "London",
            "Berlin" to "Berlin",
            "Rome" to "Rim",
        ),
    ) : TriviaTranslator {
        var translateCallsCount = 0

        override suspend fun translateQuestions(questions: List<TriviaQuestion>): List<TriviaQuestion> {
            translateCallsCount++
            return questions.map { q ->
                TriviaQuestion(
                    question = textMapping[q.question] ?: q.question,
                    correctAnswer = textMapping[q.correctAnswer] ?: q.correctAnswer,
                    answers = q.answers.map { textMapping[it] ?: it },
                )
            }
        }
    }

    private class FakeTriviaApi(
        var responseDto: TriviaResponseDto = TriviaResponseDto(
            responseCode = 0,
            results = listOf(
                TriviaQuestionDto(
                    question = "What is the capital of France?",
                    correctAnswer = "Paris",
                    incorrectAnswers = listOf("London", "Berlin", "Rome"),
                )
            ),
        ),
    ) : TriviaApi {
        override suspend fun getQuestions(amount: Int, categoryId: Int, type: String): TriviaResponseDto {
            return responseDto
        }
    }

    private class FakeTriviaDao : TriviaQuestionDao {
        val cached = mutableListOf<TriviaQuestionEntity>()

        override suspend fun findByCategory(category: TriviaCategory, limit: Int): List<TriviaQuestionEntity> {
            return cached.filter { it.category == category }.take(limit)
        }

        override suspend fun insertAll(questions: List<TriviaQuestionEntity>) {
            cached.addAll(questions)
        }

        override suspend fun deleteByCategory(category: TriviaCategory) {
            cached.removeAll { it.category == category }
        }
    }

    private class FakeGameResultDao : GameResultDao {
        val results = mutableListOf<GameResultEntity>()

        override suspend fun insert(result: GameResultEntity): Long {
            results.add(result)
            return results.size.toLong()
        }

        override suspend fun findById(id: Long): GameResultEntity? = results.getOrNull(id.toInt() - 1)
        override fun observeRecent(limit: Int): Flow<List<GameResultEntity>> = flowOf(results.takeLast(limit))
        override fun observePlayCount(gameType: GameType): Flow<Int> = flowOf(results.count { it.gameType == gameType })
        override fun observeMaxScore(gameType: GameType): Flow<Int?> = flowOf(results.filter { it.gameType == gameType }.maxOfOrNull { it.score })
        override suspend fun deleteAll() { results.clear() }
    }

    private class FakeDataStore(
        private val preferences: androidx.datastore.preferences.core.Preferences = androidx.datastore.preferences.core.emptyPreferences(),
    ) : androidx.datastore.core.DataStore<androidx.datastore.preferences.core.Preferences> {
        override val data: Flow<androidx.datastore.preferences.core.Preferences> = flowOf(preferences)
        override suspend fun updateData(
            transform: suspend (t: androidx.datastore.preferences.core.Preferences) -> androidx.datastore.preferences.core.Preferences
        ): androidx.datastore.preferences.core.Preferences {
            return transform(preferences)
        }
    }

    private var originalLocale: Locale = Locale.getDefault()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        originalLocale = Locale.getDefault()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(originalLocale)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    fun `kada je jezik engleski pitanja se ne prevode`() = runTest(dispatcher) {
        Locale.setDefault(Locale.ENGLISH)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))

        val translator = FakeTranslator()
        val fakeApi = FakeTriviaApi()
        val fakeDao = FakeTriviaDao()
        val context = ContextWrapper(null)
        val triviaRepo = TriviaRepository(context, fakeApi, fakeDao)
        val settingsRepo = SettingsRepository(FakeDataStore())
        val resultsRepo = GameResultsRepository(FakeGameResultDao())

        val savedStateHandle = SavedStateHandle(mapOf("categoryApiId" to 9))

        val viewModel = QuizViewModel(
            savedStateHandle = savedStateHandle,
            triviaRepository = triviaRepo,
            settingsRepository = settingsRepo,
            resultsRepository = resultsRepo,
            triviaTranslator = translator,
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is QuizUiState.Playing)
        val playingState = state as QuizUiState.Playing
        assertEquals(1, playingState.questions.size)
        assertEquals("What is the capital of France?", playingState.currentQuestion.question)
        assertEquals("Paris", playingState.currentQuestion.correctAnswer)
        assertEquals(QuestionSource.NETWORK, playingState.source)
        assertEquals(0, translator.translateCallsCount)
    }

    @Test
    fun `kada je jezik srpski pitanja se prevode preko translatora`() = runTest(dispatcher) {
        Locale.setDefault(Locale.forLanguageTag("sr"))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("sr"))

        val translator = FakeTranslator()
        val fakeApi = FakeTriviaApi()
        val fakeDao = FakeTriviaDao()
        val context = ContextWrapper(null)
        val triviaRepo = TriviaRepository(context, fakeApi, fakeDao)
        val settingsRepo = SettingsRepository(FakeDataStore())
        val resultsRepo = GameResultsRepository(FakeGameResultDao())

        val savedStateHandle = SavedStateHandle(mapOf("categoryApiId" to 9))

        val viewModel = QuizViewModel(
            savedStateHandle = savedStateHandle,
            triviaRepository = triviaRepo,
            settingsRepository = settingsRepo,
            resultsRepository = resultsRepo,
            triviaTranslator = translator,
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is QuizUiState.Playing)
        val playingState = state as QuizUiState.Playing
        assertEquals(1, playingState.questions.size)
        assertEquals("Koji je glavni grad Francuske?", playingState.currentQuestion.question)
        assertEquals("Pariz", playingState.currentQuestion.correctAnswer)
        assertEquals(QuestionSource.NETWORK, playingState.source)
        assertEquals(1, translator.translateCallsCount)

        // Potvrda da cache cuva originalno englesko pitanje a ne prevod
        val cachedEntities = fakeDao.cached
        assertEquals(1, cachedEntities.size)
        assertEquals("What is the capital of France?", cachedEntities[0].question)
        assertEquals("Paris", cachedEntities[0].correctAnswer)
    }

    @Test
    fun `kada se pitanje ucita iz kesa prevodi se na srpski ako je jezik sr`() = runTest(dispatcher) {
        Locale.setDefault(Locale.forLanguageTag("sr"))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("sr"))

        val translator = FakeTranslator()
        // API ne vraca nista (simulacija offline rada)
        val fakeApi = FakeTriviaApi(TriviaResponseDto(responseCode = 1, results = emptyList()))
        val fakeDao = FakeTriviaDao()
        // Keš ima originalno englesko pitanje
        fakeDao.cached.add(
            TriviaQuestionEntity(
                category = TriviaCategory.GENERAL,
                question = "What is the capital of France?",
                correctAnswer = "Paris",
                answers = listOf("Paris", "London", "Berlin", "Rome"),
            )
        )

        val context = ContextWrapper(null)
        val triviaRepo = TriviaRepository(context, fakeApi, fakeDao)
        val settingsRepo = SettingsRepository(FakeDataStore())
        val resultsRepo = GameResultsRepository(FakeGameResultDao())

        val savedStateHandle = SavedStateHandle(mapOf("categoryApiId" to 9))

        val viewModel = QuizViewModel(
            savedStateHandle = savedStateHandle,
            triviaRepository = triviaRepo,
            settingsRepository = settingsRepo,
            resultsRepository = resultsRepo,
            triviaTranslator = translator,
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is QuizUiState.Playing)
        val playingState = state as QuizUiState.Playing
        assertEquals(QuestionSource.CACHE, playingState.source)
        assertEquals("Koji je glavni grad Francuske?", playingState.currentQuestion.question)
        assertEquals("Pariz", playingState.currentQuestion.correctAnswer)
        assertEquals(1, translator.translateCallsCount)

        // Provera da je u kešu i dalje originalno englesko pitanje
        assertEquals("What is the capital of France?", fakeDao.cached[0].question)
    }
}
