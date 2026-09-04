package play.pmu.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") categoryId: Int,
        @Query("type") type: String = "multiple",
    ): TriviaResponseDto
}
