package play.pmu.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import play.pmu.data.translation.MlKitTriviaTranslator
import play.pmu.data.translation.TriviaTranslator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranslationModule {

    @Binds
    @Singleton
    abstract fun bindTriviaTranslator(
        impl: MlKitTriviaTranslator,
    ): TriviaTranslator
}
