package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.lucasangelo.voxpopuli.ui.overlayTransparencyColor

// NOTE: inspired by https://github.com/elyesmansour/compose-floating-tab-bar

val floatingNavigationBarPadding = 130.dp

sealed interface FloatingNavigationItem {
    val icon: Any
    val title: Any
    val showTitle: Boolean
}
data class FloatingNavigationActionItem(
    override val icon: Any,
    override val title: Any,
    override val showTitle: Boolean,
    val action: () -> Unit,
) : FloatingNavigationItem

@Composable
fun BoxScope.FloatingNavigationBar(
    selectedIndex: Int,
    items: List<FloatingNavigationActionItem>,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, overlayTransparencyColor)
                    )
                )
        ) {
            val listState = rememberLazyListState()

            val scrollOffsetPx = with(LocalDensity.current) {
                (-96).dp.roundToPx()
            }
            LaunchedEffect(selectedIndex, scrollOffsetPx) {
                listState.animateScrollToItem(
                    index = selectedIndex + 1,
                    scrollOffset = scrollOffsetPx
                )
            }

            LazyRow(
                state = listState,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .safeDrawingPadding()
                    .padding(vertical = 12.dp)
                    .heightIn(92.dp)
            ) {
                item { Spacer(Modifier.width(16.dp)) }

                items(
                    items,
                    key = { it.title.toString() }
                ) { item ->
                    FloatingNavigationButton(
                        icon = item.icon,
                        title = item.title,
                        showTitle = item.showTitle,
                        onClick = item.action,
                    )
                }

                item { Spacer(Modifier.width(16.dp)) }
            }
        }
    }
}

@Composable
fun FloatingNavigationButton(
    icon: Any,
    title: Any,
    showTitle: Boolean,
    onClick: () -> Unit,
) {
    /* NOTE: really wanted this but the animation is super janky
       TODO: refactor navigation bar animated focus scroll
    val horizontalPadding by animateDpAsState(
        targetValue = if (showTitle) 24.dp else 0.dp,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
    )
     */

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp)
    ) {
        val topOffset by animateDpAsState(
            targetValue = if (showTitle) (-12).dp else 0.dp,
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        )
        val iconModifier = Modifier
            .size(72.dp)
            .offset {
                IntOffset(x = 0, y = topOffset.roundToPx())
            }

        when (icon) {
            is String ->
                MonochromeAsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = iconModifier.scale(0.6f)

                )
            is Int ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = iconModifier
                )
            else -> {}
        }
    }
}