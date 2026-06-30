package dev.lucasangelo.voxpopuli.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix

//val overlayTransparencyColor = Color.Black.copy(0.75f)
val overlayTransparencyColor = Color.Black

inline fun Color.darken(darkenBy: Float = 0.75f): Color {
    val multiplier = 1f - darkenBy
    return copy(
        red = (red * multiplier).coerceIn(0f, 1f),
        green = (green * multiplier).coerceIn(0f, 1f),
        blue = (blue * multiplier).coerceIn(0f, 1f),
        alpha = alpha
    )
}

inline fun Color.lighten(lightenBy: Float = 0.25f): Color {
    return copy(
        red = (red + (1f - red) * lightenBy).coerceIn(0f, 1f),
        green = (green + (1f - green) * lightenBy).coerceIn(0f, 1f),
        blue = (blue + (1f - blue) * lightenBy).coerceIn(0f, 1f),
        alpha = alpha
    )
}

val invertMatrix = ColorMatrix( values =
    floatArrayOf(
        -1f, 0f, 0f, 0f, 255f, // Red
        0f, -1f, 0f, 0f, 255f, // Green
        0f, 0f, -1f, 0f, 255f, // Blue
        0f, 0f, 0f, 1f, 0f  // Alpha
    )
)