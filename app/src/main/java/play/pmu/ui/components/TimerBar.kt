package play.pmu.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import play.pmu.ui.theme.PmuSpacing
import play.pmu.ui.theme.WrongRedLight

@Composable
fun TimerBar(
    secondsLeft: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f
    val animatedFraction by animateFloatAsState(targetValue = fraction, label = "timerFraction")
    val isRunningOut = secondsLeft <= WARNING_THRESHOLD_SECONDS
    val barColor by animateColorAsState(
        targetValue = if (isRunningOut) WrongRedLight else MaterialTheme.colorScheme.primary,
        label = "timerColor",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PmuSpacing.small),
    ) {
        Text(
            text = secondsLeft.toString(),
            style = MaterialTheme.typography.headlineSmall,
            color = barColor,
        )
        LinearProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.fillMaxWidth(),
            color = barColor,
        )
    }
}

private const val WARNING_THRESHOLD_SECONDS = 5
