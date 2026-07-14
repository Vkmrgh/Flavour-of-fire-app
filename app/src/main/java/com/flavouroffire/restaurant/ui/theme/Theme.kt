package com.flavouroffire.restaurant.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Tomato = Color(0xFFB9382E)
private val Ivory = Color(0xFFFFF9F0)
private val Charcoal = Color(0xFF26231F)
private val Olive = Color(0xFF667046)
private val Sand = Color(0xFFEDE2D3)

private val colors = lightColorScheme(
    primary = Tomato, onPrimary = Ivory, secondary = Olive, onSecondary = Ivory,
    background = Ivory, onBackground = Charcoal, surface = Ivory, onSurface = Charcoal,
    surfaceVariant = Sand, onSurfaceVariant = Charcoal, outline = Color(0xFF8B8177)
)

private val typography = androidx.compose.material3.Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable fun FlavourOfFireTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
