package com.seance.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

val BackgroundDark = androidx.compose.ui.graphics.Color(0xFF0C0C0C)
val SurfaceDark = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
val OnSurface = androidx.compose.ui.graphics.Color(0xFFEEEEEE)
val AccentDefault = androidx.compose.ui.graphics.Color(0xFFE5A00D)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SeanceTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        background = BackgroundDark,
        surface = SurfaceDark,
        onBackground = OnSurface,
        onSurface = OnSurface,
        primary = AccentDefault,
        onPrimary = androidx.compose.ui.graphics.Color.Black
    )
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeanceTypography,
        content = content
    )
}
