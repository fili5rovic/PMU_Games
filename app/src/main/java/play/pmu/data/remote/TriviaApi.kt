package play.pmu.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Mrezni sloj je namerno mali: jedan endpoint, jedna metoda.
 *
 * Retrofit iz ovog interface-a sam generise implementaciju, a suspend znaci
 * da poziv ne blokira nit - coroutine se prekida dok se ceka odgovor.
 */
interface TriviaApi {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") categoryId: Int,
        @Query("type") type: String = "multiple",
    ): TriviaResponseDto
}
