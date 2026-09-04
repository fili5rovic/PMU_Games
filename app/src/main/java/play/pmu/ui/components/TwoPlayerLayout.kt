package play.pmu.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate

@Composable
fun TwoPlayerLayout(
    topContent: @Composable BoxScope.() -> Unit,
    bottomContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    topModifier: Modifier = Modifier,
    bottomModifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(180f)
                .then(topModifier),
            contentAlignment = Alignment.Center,
            content = topContent,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(bottomModifier),
            contentAlignment = Alignment.Center,
            content = bottomContent,
        )
    }
}

@Composable
fun SharedBoardLayout(
    topPanel: @Composable BoxScope.() -> Unit,
    bottomPanel: @Composable BoxScope.() -> Unit,
    centerContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    topPanelModifier: Modifier = Modifier,
    bottomPanelModifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .rotate(180f)
                .then(topPanelModifier),
            contentAlignment = Alignment.Center,
            content = topPanel,
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            content = centerContent,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(bottomPanelModifier),
            contentAlignment = Alignment.Center,
            content = bottomPanel,
        )
    }
}
