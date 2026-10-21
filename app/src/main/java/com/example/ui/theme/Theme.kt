package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NavyPrimary = Color(0xFF0D1B2A)
val NavySecondary = Color(0xFF1B263B)
val NavyCard = Color(0xFF162238)
val BurgundyAccent = Color(0xFF7A1C30)
val GoldAccent = Color(0xFFD4AF37)
val GoldLight = Color(0xFFF3C64F)
val SlateBackground = Color(0xFFF4F6F9)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    secondary = GoldAccent,
    onSecondary = NavyPrimary,
    tertiary = BurgundyAccent,
    background = SlateBackground,
    surface = Color.White,
    onSurface = NavyPrimary,
    surfaceVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyPrimary,
    secondary = GoldLight,
    onSecondary = NavyPrimary,
    tertiary = BurgundyAccent,
    background = NavyPrimary,
    surface = NavySecondary,
    onSurface = Color.White,
    surfaceVariant = NavyCard
)

@Composable
fun FrontCraftPROTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
