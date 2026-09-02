package play.pmu.domain.game

import kotlin.random.Random

/**
 * Binarni broj i ponudjeni decimalni odgovori. [answers] sadrzi tacno jedan
 * tacan odgovor ([correctAnswer]), na slucajnom mestu.
 */
data class BinaryQuestion(
    val binary: String,
    val correctAnswer: Int,
    val answers: List<Int>,
)

/**
 * Pravi pitanje "koliko je ovaj binarni broj u decimalnom".
 *
 * Broj ima 4-8 bita i uvek pocinje jedinicom (zato se izvlaci iz gornje
 * polovine opsega), pa binarni zapis ima tacno toliko cifara koliko je bita.
 * Odbacuju se trivijalni brojevi - stepeni dvojke (100000) i sami jedinice
 * (11111) - da pitanje ne bi bilo "napamet".
 *
 * Pretvaranje ide obicnim Kotlin funkcijama: [Int.toString] sa osnovom 2 u jednu
 * stranu, a `String.toInt(2)` u drugu (koristi se u testu za proveru).
 */
fun randomBinaryQuestion(random: Random = Random.Default): BinaryQuestion {
    val bits = random.nextInt(MIN_BITS, MAX_BITS + 1)
    val lowest = 1 shl (bits - 1)
    val highestExclusive = 1 shl bits

    var value: Int
    do {
        value = random.nextInt(lowest, highestExclusive)
    } while (isTrivial(value, bits))

    return BinaryQuestion(
        binary = value.toString(2),
        correctAnswer = value,
        answers = plausibleDecimalAnswers(value, bits, random),
    )
}

/** Stepen dvojke ili sve jedinice - takav broj se cita bez racunanja. */
private fun isTrivial(value: Int, bits: Int): Boolean =
    value.countOneBits() <= 1 || value.countOneBits() == bits

/**
 * Tacan odgovor i tri netacna, izmesani.
 *
 * Netacni se prave OKRETANJEM JEDNOG BITA, jer je to i najcesca prava greska pri
 * rucnom pretvaranju - dobijeni brojevi su zato uverljivi, a ne slucajni. Ako
 * okretanje bita ne da dovoljno razlicitih vrednosti, dopunjuje se malim
 * pomerajima.
 */
private fun plausibleDecimalAnswers(value: Int, bits: Int, random: Random): List<Int> {
    val answers = mutableSetOf(value)

    val bitOrder = (0 until bits).shuffled(random)
    for (bit in bitOrder) {
        if (answers.size == ANSWER_COUNT) break
        val candidate = value xor (1 shl bit)
        if (candidate > 0) answers += candidate
    }

    var offset = 1
    while (answers.size < ANSWER_COUNT) {
        if (value + offset > 0) answers += value + offset
        if (answers.size < ANSWER_COUNT && value - offset > 0) answers += value - offset
        offset++
    }

    return answers.shuffled(random)
}

private const val MIN_BITS = 4
private const val MAX_BITS = 8
