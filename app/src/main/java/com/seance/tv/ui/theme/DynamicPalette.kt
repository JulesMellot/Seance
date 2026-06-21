package com.seance.tv.ui.theme

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DynamicColors(
    val dominant: Color = BackgroundDark,
    val accent: Color = AccentDefault,
    val dominantWashed: Color = BackgroundDark.copy(alpha = 0.18f)
)

suspend fun extractPalette(bitmap: Bitmap): DynamicColors = withContext(Dispatchers.Default) {
    val palette = Palette.from(bitmap).generate()
    val fallback = BackgroundDark.toArgb()
    val dominant = Color(palette.getDominantColor(fallback))
    val vibrant = Color(palette.getVibrantColor(dominant.toArgb()))
    DynamicColors(
        dominant = dominant,
        accent = vibrant,
        dominantWashed = dominant.copy(alpha = 0.18f)
    )
}

@Composable
fun rememberDynamicColors(bitmap: Bitmap?): MutableState<DynamicColors> {
    val colors = remember { mutableStateOf(DynamicColors()) }
    LaunchedEffect(bitmap) {
        if (bitmap != null) {
            colors.value = extractPalette(bitmap)
        } else {
            colors.value = DynamicColors()
        }
    }
    return colors
}
