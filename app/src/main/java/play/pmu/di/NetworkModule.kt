package play.pmu.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import play.pmu.data.remote.TriviaApi
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Retrofit klijent se pravi jednom i deli kao @Singleton - pravljenje novog
 * klijenta po pozivu bilo bi skupo (svaki nosi svoj thread pool i connection pool).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://opentdb.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // API s vremenom dodaje nova polja; bez ovoga bi parsiranje pucalo na njima.
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTriviaApi(retrofit: Retrofit): TriviaApi = retrofit.create(TriviaApi::class.java)
}
