package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.MiniGame
import play.pmu.domain.model.Winner

/**
 * Jedna odigrana runda mini igre.
 *
 * Cuvaju se SAMO podaci koje sve mini igre imaju: koja igra, ko je pobedio i
 * kada. Zahvaljujuci tome dodavanje nove mini igre ne menja bazu i ne dodaje
 * prazne kolone - nova igra prosto upisuje svoje ime u [game].
 *
 * Unutrasnji detalji pojedinih igara (ciljno vreme kod "Stani na vreme",
 * odstupanja igraca, broj tapkanja) se NE cuvaju: to bi znacilo ili kolonu po
 * igri, ili tabelu vise, a statistika ih nigde ne prikazuje. Detalji zive samo u
 * toku runde, na ekranu rezultata te runde.
 */
@Entity(tableName = "round_results")
data class RoundResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game: MiniGame,
    val winner: Winner,
    val playedAt: Long,
)

/**
 * Sazetak jedne mini igre, koji Room sklapa iz GROUP BY upita (vidi
 * [RoundResultDao.observePerGame]). Nije @Entity - to je samo oblik rezultata.
 */
data class MiniGameStats(
    val game: MiniGame,
    val played: Int,
    val winsOne: Int,
    val winsTwo: Int,
)
