package com.retrivedmods.luxclient.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// LuxClient custom color palette
val LuxClientGradient1 = Color(0xFF6A4C93)
val LuxClientGradient2 = Color(0xFF9B59B6)

val DeepBackground = Color(0xFF08080C)
val CardBackground = Color(0xFF0A0A10)
val ModuleBackground = Color(0xFF060609)
val SidebarBackground = Color(0xFF0B0B11)
val TextPrimary = Color(0xFFE5E5E8)
val TextSecondary = Color(0xFF9A9AA5)
val BorderPrimary = Color(0x306A4C93)

private val LuxDarkColorScheme = darkColorScheme(
    primary = LuxClientGradient1,
    onPrimary = Color.White,
    secondary = LuxClientGradient2,
    onSecondary = Color.White,
    background = DeepBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SidebarBackground,
    onSurfaceVariant = TextSecondary,
    outline = BorderPrimary
)

@Composable
fun MuCuteClientTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LuxDarkColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
