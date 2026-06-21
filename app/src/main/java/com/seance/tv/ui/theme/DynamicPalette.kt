package com.seance.tv.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
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

/**
 * Charge le poster via Coil et en extrait une couleur d'accent (vibrant) utilisable
 * comme accent dynamique. Retourne null si indisponible ou trop sombre/lavé — dans ce
 * cas l'appelant garde l'amber Séance par défaut.
 */
suspend fun loadAccentColor(context: Context, url: String?): Color? {
    if (url.isNullOrBlank()) return null
    return withContext(Dispatchers.IO) {
        runCatching {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)   // requis pour lire les pixels (Palette)
                .size(160)
                .build()
            val result = loader.execute(request)
            val bitmap = ((result as? SuccessResult)?.drawable as? BitmapDrawable)?.bitmap
                ?: return@runCatching null
            val palette = Palette.from(bitmap).generate()
            val argb = palette.getVibrantColor(palette.getLightVibrantColor(0))
            if (argb == 0) return@runCatching null
            val color = Color(argb)
            // On évite les accents trop sombres (illisibles) : on garde l'amber par défaut.
            if (color.luminance() < 0.12f) null else color
        }.getOrNull()
    }
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
