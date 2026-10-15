package com.example.revlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary, onPrimary = Color.Black,
    secondary = BrandSecondary, onSecondary = Color.White,
    tertiary = BrandTertiary, onTertiary = Color.Black,
    background = LightBackground, onBackground = LightTextPrimary,
    surface = LightSurface, onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated, onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerHighest = LightSurfaceElevated,
    outline = LightBorderDefault, outlineVariant = LightDivider,
    error = LightError, onError = Color.White,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary, onPrimary = Color.Black,
    secondary = BrandSecondary, onSecondary = Color.White,
    tertiary = BrandTertiary, onTertiary = Color.Black,
    background = DarkBackground, onBackground = DarkTextPrimary,
    surface = DarkSurface, onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated, onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkBorderDefault, outlineVariant = DarkDivider,
    error = DarkError, onError = Color.White,
    scrim = Color.Black,
)

@Composable
fun RevLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val custom = if (darkTheme) DarkRevLensColors else LightRevLensColors
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalRevLensColors provides custom) {
        MaterialTheme(
            colorScheme = scheme,
            typography = RevLensTypography,
            shapes = RevLensShapes,
            content = content,
        )
    }
}
