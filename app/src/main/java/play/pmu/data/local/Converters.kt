package play.pmu.data.local

import androidx.room.TypeConverter
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.TriviaCategory
import play.pmu.domain.model.Winner


class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString(SEPARATOR)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(SEPARATOR)

    @TypeConverter
    fun fromGameType(value: GameType): String = value.name

    @TypeConverter
    fun toGameType(value: String): GameType = GameType.valueOf(value)

    @TypeConverter
    fun fromMiniGame(value: MiniGame): String = value.name

    @TypeConverter
    fun toMiniGame(value: String): MiniGame = MiniGame.valueOf(value)

    @TypeConverter
    fun fromWinner(value: Winner): String = value.name

    @TypeConverter
    fun toWinner(value: String): Winner = Winner.valueOf(value)

    @TypeConverter
    fun fromTriviaCategory(value: TriviaCategory): String = value.name

    @TypeConverter
    fun toTriviaCategory(value: String): TriviaCategory = TriviaCategory.valueOf(value)

    private companion object {
        const val SEPARATOR = "\n"
    }
}
