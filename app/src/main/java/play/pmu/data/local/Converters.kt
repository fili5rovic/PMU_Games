package play.pmu.data.local

import androidx.room.TypeConverter
import play.pmu.domain.model.GameType
import play.pmu.domain.model.TriviaCategory

/**
 * Room ume da cuva samo primitivne tipove, pa mu ovde objasnjavamo kako da
 * upise enum-e i liste stringova.
 *
 * Liste se cuvaju kao jedan tekst sa razdvojnikom. To je dovoljno jer su elementi
 * pojmovi i odgovori bez znaka '\n'; alternativa (posebna tabela) bila bi
 * nesrazmerno komplikovana za ovu potrebu.
 */
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
    fun fromTriviaCategory(value: TriviaCategory): String = value.name

    @TypeConverter
    fun toTriviaCategory(value: String): TriviaCategory = TriviaCategory.valueOf(value)

    private companion object {
        const val SEPARATOR = "\n"
    }
}
