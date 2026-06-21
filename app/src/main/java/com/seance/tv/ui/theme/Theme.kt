package com.seance.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

// Alias de compatibilité — pointent vers les nouveaux tokens (voir DesignTokens.kt)
val BackgroundDark: Color = BackgroundBase
val SurfaceDark: Color = Surface
val OnSurface: Color = TextPrimary
val AccentDefault: Color = Accent

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeanceTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        background = BackgroundBase,
        surface = Surface,
        surfaceVariant = SurfaceFocused,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        primary = Accent,
        onPrimary = Color.Black,
        border = BorderSubtle
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeanceTypography,
        content = content
    )
}
