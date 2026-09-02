package play.pmu.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import play.pmu.domain.model.Winner

/**
 * Jedna odigrana partija (niz mini igara za dva igraca).
 *
 * Cuva se samo ono sto se prikazuje u istoriji: kada je odigrana, koliko je
 * rundi imala, koliko je koji igrac osvojio i ko je pobedio. Pojedinacne runde
 * se NE cuvaju - istorija po rundama nigde se ne prikazuje, pa bi druga tabela i
 * relacija bile suvisna komplikacija.
 *
 * [winner] se moze izracunati iz skorova, ali se cuva i eksplicitno: tako upit
 * za istoriju ne mora nista da racuna, a znacenje reda je citljivo i iz baze.
 */
@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playedAt: Long,
    val scoreOne: Int,
    val scoreTwo: Int,
    val winner: Winner,
    val gamesPlayed: Int,
)
