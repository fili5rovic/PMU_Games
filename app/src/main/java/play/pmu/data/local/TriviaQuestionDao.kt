package play.pmu.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import play.pmu.domain.model.TriviaCategory

@Dao
interface TriviaQuestionDao {

    @Query("SELECT * FROM trivia_questions WHERE category = :category LIMIT :limit")
    suspend fun findByCategory(category: TriviaCategory, limit: Int): List<TriviaQuestionEntity>

    @Query("DELETE FROM trivia_questions WHERE category = :category")
    suspend fun deleteByCategory(category: TriviaCategory)

    @Insert
    suspend fun insertAll(questions: List<TriviaQuestionEntity>)

    /**
     * Zamenjuje cache jedne kategorije. @Transaction garantuje da brisanje i
     * upis idu kao jedna operacija, pa cache nikada ne ostane prazan ako
     * upis pukne u sredini.
     */
    @Transaction
    suspend fun replaceCategory(category: TriviaCategory, questions: List<TriviaQuestionEntity>) {
        deleteByCategory(category)
        insertAll(questions)
    }
}
