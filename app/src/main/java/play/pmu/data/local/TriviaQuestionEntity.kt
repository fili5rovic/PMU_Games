package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.TriviaCategory

/**
 * Cache pitanja skinutih sa REST API-ja. Zahvaljujuci njemu kviz radi i bez
 * interneta, a WorkManager ga periodicno osvezava u pozadini.
 */
@Entity(tableName = "trivia_questions")
data class TriviaQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: TriviaCategory,
    val question: String,
    val correctAnswer: String,
    val answers: List<String>,
)
