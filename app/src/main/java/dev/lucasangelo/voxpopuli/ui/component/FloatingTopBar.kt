package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.ui.overlayTransparencyColor

val floatingTopBarPadding = 120.dp
val floatingTopBarButtonSize = 72.dp

@Composable
fun BoxScope.FloatingTopBar(
    title: String,
    canGoBack: Boolean,
    onGoBackRequest: () -> Unit = { },
) {
    Box(
        modifier = Modifier
            .background(Brush.verticalGradient(
                colors = listOf(overlayTransparencyColor, Color.Transparent)
            ))
            .safeDrawingPadding()
            .height(floatingTopBarButtonSize)
            .align(Alignment.TopCenter)
            .fillMaxWidth()
    ) {
        if (canGoBack) {
            Icon(
                painter = painterResource(R.drawable.icon_back),
                contentDescription = stringResource(R.string.go_back),
                modifier = Modifier
                    .size(floatingTopBarButtonSize)
                    .align(Alignment.CenterStart)
                    .clickable(onClick = onGoBackRequest)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}