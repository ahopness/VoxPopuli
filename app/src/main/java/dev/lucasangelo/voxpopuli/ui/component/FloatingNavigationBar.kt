package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.lucasangelo.voxpopuli.util.overlayTransparencyColor

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

            LaunchedEffect(selectedIndex) {
                listState.animateScrollToItem(selectedIndex+1, scrollOffset = -256)
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

                items(items) { item ->
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .animateContentSize()
            .clickable(onClick = onClick),
    ) {
        when (icon) {
            is String ->
                MonochromeAsyncImage(
                    model = icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            is Int ->
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(72.dp)
                )
            else -> {}
        }
        AnimatedVisibility(showTitle) {
            Text(
                text =
                    when (title) {
                        is String -> title
                        is Int -> stringResource(title)
                        else -> ""
                    },
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}