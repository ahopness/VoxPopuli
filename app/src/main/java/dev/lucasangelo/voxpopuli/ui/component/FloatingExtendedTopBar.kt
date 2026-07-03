package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.ui.overlayTransparencyColor
import kotlin.math.roundToInt

val floatingExtendedTopBarPadding = 420.dp

data class FloatingExtendedTopBarActionItem(
    val name: String,
    val icon: Int,
    val onClick: () -> Unit
)

@Composable
fun BoxScope.FloatingExtendedTopBar(
    title: String,
    canGoBack: Boolean,
    onGoBackRequest: () -> Unit = { },
    iconContent: @Composable (Modifier, () -> Float) -> Unit,
    actions: List<FloatingExtendedTopBarActionItem>,
    listState: LazyListState,
) {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(overlayTransparencyColor, Color.Transparent)
                )
            )
    ) {
        val collapseRangePx = with(LocalDensity.current) { floatingExtendedTopBarPadding.toPx() / 1.5f }
        val collapsedFraction = remember(collapseRangePx) {
            derivedStateOf {
                when {
                    (listState.firstVisibleItemIndex > 0) -> 1f
                    else -> (listState.firstVisibleItemScrollOffset / collapseRangePx).coerceIn(0f, 1f)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .layout { measurable, constraints ->
                    val currentHeight = lerp(
                        floatingExtendedTopBarPadding.toPx(),
                        floatingTopBarButtonSize.toPx(),
                        collapsedFraction.value
                    ).roundToInt()

                    val placeable = measurable.measure(
                        constraints.copy(minHeight = currentHeight, maxHeight = currentHeight)
                    )
                    layout(placeable.width, placeable.height) {
                        placeable.placeRelative(0, 0)
                    }
                }
        ) {
            if (canGoBack) {
                Icon(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = stringResource(R.string.go_back),
                    modifier = Modifier
                        .size(floatingTopBarButtonSize)
                        .align(Alignment.TopStart)
                        .clickable(onClick = onGoBackRequest)
                )
            }

            iconContent(
                Modifier
                    .alpha(lerp(1f, 0f, collapsedFraction.value))
                    .size(floatingTopBarButtonSize)
                    .align(Alignment.Center),
                { collapsedFraction.value }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = lerp(
                                84.dp.toPx(),
                                0.dp.toPx(),
                                collapsedFraction.value
                            ).toInt()
                        )
                    }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy((-24).dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
            ) {
                actions.forEach {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = it.name,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(floatingTopBarButtonSize)
                            .clickable(onClick = it.onClick)
                    )
                }
            }
        }
    }
}
