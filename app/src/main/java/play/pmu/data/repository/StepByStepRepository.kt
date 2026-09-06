package play.pmu.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import play.pmu.R
import play.pmu.domain.model.StepByStepPuzzle
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
open class StepByStepRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    open fun loadPuzzles(): List<StepByStepPuzzle> {
        val array = try {
            context.resources.getStringArray(R.array.step_by_step_puzzles)
        } catch (e: Exception) {
            emptyArray()
        }
        return array.mapNotNull { line ->
            val parts = line.split("|").map { it.trim() }
            if (parts.size >= 8) {
                StepByStepPuzzle(
                    solution = parts[0],
                    clues = parts.subList(1, 8),
                )
            } else null
        }
    }

    open fun getRandomPuzzle(random: Random = Random.Default): StepByStepPuzzle? {
        val puzzles = loadPuzzles()
        return if (puzzles.isNotEmpty()) puzzles.random(random) else null
    }
}
