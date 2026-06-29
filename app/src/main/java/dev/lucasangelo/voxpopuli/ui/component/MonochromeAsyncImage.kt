package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import dev.lucasangelo.voxpopuli.util.invertMatrix

@Composable
fun MonochromeAsyncImage(
    model: Any,
    contentDescription: String?,
    contentScale: ContentScale = ContentScale.Fit,
    modifier: Modifier,
) {
    AsyncImage(
        model,
        contentDescription,
        contentScale = contentScale,
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
            timesAssign(ColorMatrix().apply { setToSaturation(0f) })
            timesAssign(invertMatrix)
        }),
        modifier = modifier
    )
}