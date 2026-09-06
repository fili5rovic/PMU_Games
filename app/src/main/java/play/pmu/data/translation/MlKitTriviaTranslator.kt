package play.pmu.data.translation

import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import play.pmu.domain.model.TriviaQuestion
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitTriviaTranslator @Inject constructor() : TriviaTranslator {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.ENGLISH)
        .setTargetLanguage(TranslateLanguage.CROATIAN)
        .build()

    private val translator: Translator by lazy {
        Translation.getClient(options)
    }

    override suspend fun translateQuestions(questions: List<TriviaQuestion>): List<TriviaQuestion> {
        if (questions.isEmpty()) return questions

        return withContext(Dispatchers.IO) {
            try {
                val conditions = DownloadConditions.Builder().build()
                translator.downloadModelIfNeeded(conditions).await()

                val uniqueTexts = questions.flatMap { q ->
                    listOf(q.question, q.correctAnswer) + q.answers
                }.distinct()

                val translationMap = mutableMapOf<String, String>()
                for (text in uniqueTexts) {
                    translationMap[text] = try {
                        translator.translate(text).await()
                    } catch (e: Exception) {
                        text
                    }
                }

                questions.map { q ->
                    TriviaQuestion(
                        question = translationMap[q.question] ?: q.question,
                        correctAnswer = translationMap[q.correctAnswer] ?: q.correctAnswer,
                        answers = q.answers.map { translationMap[it] ?: it },
                    )
                }
            } catch (e: Exception) {
                // Ako preuzimanje modela ili prevod ne uspe, vracamo originalna pitanja
                // bez prekida (crash) rada aplikacije.
                questions
            }
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
        addOnSuccessListener { result ->
            if (cont.isActive) cont.resume(result)
        }
        addOnFailureListener { exception ->
            if (cont.isActive) cont.resumeWithException(exception)
        }
        addOnCanceledListener {
            if (cont.isActive) cont.cancel()
        }
    }
}
