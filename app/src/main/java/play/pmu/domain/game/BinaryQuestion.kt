package play.pmu.domain.game

import kotlin.random.Random

data class BinaryQuestion(
    val binary: String,
    val correctAnswer: Int,
    val answers: List<Int>,
)

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

private fun isTrivial(value: Int, bits: Int): Boolean =
    value.countOneBits() <= 1 || value.countOneBits() == bits

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
