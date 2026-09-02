package play.pmu.data.local

import androidx.room.TypeConverter
import play.pmu.domain.model.GameType
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.TriviaCategory
import play.pmu.domain.model.Winner

/**
 * Room ume da cuva samo primitivne tipove, pa mu ovde objasnjavamo kako da
 * upise enum-e i liste stringova.
 *
 * Liste se cuvaju kao jedan tekst sa razdvojnikom. To je dovoljno jer su elementi
 * pojmovi i odgovori bez znaka '\n'; alternativa (posebna tabela) bila bi
 * nesrazmerno komplikovana za ovu potrebu.
 *
 * Enum-i se cuvaju po IMENU konstante. `valueOf` puca na nepoznato ime, pa
 * uklanjanje ili preimenovanje konstante zahteva i podizanje verzije baze -
 * time stari redovi nestanu i ne moze se procitati ime koje vise ne postoji.
 * (Tako je i uradjeno kada su Brzina reakcije i Memorija prestale da budu igre
 * za jednog igraca.)
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
