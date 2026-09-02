package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.GameType

/**
 * Jedna odigrana partija. Tabela je zajednicka za sve igre, a znacenje polja
 * [score]/[total] zavisi od [gameType]:
 *
 * - CHARADES: pogodjeni pojmovi / ukupno prikazanih pojmova
 * - QUIZ:     tacni odgovori / ukupno pitanja
 * - REACTION: prosecno vreme u ms / broj rundi  (manje je bolje)
 * - MEMORY:   broj poteza / broj parova         (manje je bolje po potezima)
 *
 * [correctItems] i [skippedItems] popunjava samo pantomima - to su liste pojmova
 * koje se prikazuju na ekranu rezultata. Cuvaju se u bazi (a ne prosledjuju kroz
 * navigaciju) da bi navigacioni argument ostao samo jedan Long id.
 */
@Entity(tableName = "game_results")
data class GameResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameType: GameType,
    val score: Int,
    val total: Int,
    val durationSeconds: Int,
    val playedAt: Long,
    val correctItems: List<String> = emptyList(),
    val skippedItems: List<String> = emptyList(),
)
