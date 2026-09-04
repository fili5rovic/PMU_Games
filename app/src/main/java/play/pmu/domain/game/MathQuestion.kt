package play.pmu.domain.game

import play.pmu.domain.model.MathOperation
import kotlin.random.Random

data class MathQuestion(
    val text: String,
    val correctAnswer: Int,
    val answers: List<Int>,
)

fun randomMathQuestion(
    operations: Set<MathOperation> = MathOperation.DEFAULT,
    random: Random = Random.Default,
): MathQuestion {
    val operation = operations.ifEmpty { MathOperation.DEFAULT }.random(random)

    val left: Int
    val right: Int
    val correct: Int
    when (operation) {
        MathOperation.PLUS -> {
            left = random.nextInt(2, 40)
            right = random.nextInt(2, 40)
            correct = left + right
        }

        MathOperation.MINUS -> {
            left = random.nextInt(10, 60)
            right = random.nextInt(2, left)
            correct = left - right
        }

        MathOperation.TIMES -> {
            left = random.nextInt(2, 10)
            right = random.nextInt(2, 10)
            correct = left * right
        }

        MathOperation.DIVIDE -> {
            correct = random.nextInt(2, 13)
            right = random.nextInt(2, 10)
            left = correct * right
        }
    }

    return MathQuestion(
        text = "$left ${operation.symbol} $right = ?",
        correctAnswer = correct,
        answers = plausibleAnswers(correct, random),
    )
}

private fun plausibleAnswers(correct: Int, random: Random): List<Int> {
    val answers = mutableSetOf(correct)
    while (answers.size < ANSWER_COUNT) {
        val offset = random.nextInt(-6, 7)
        val candidate = correct + offset
        if (offset != 0 && candidate >= 0) answers += candidate
    }
    return answers.shuffled(random)
}

const val ANSWER_COUNT = 4
