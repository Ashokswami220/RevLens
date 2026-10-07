package com.example.revlens.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class RevLensColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val primaryAction: Color,
    val onPrimaryAction: Color,
    val accentBlue: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val borderDefault: Color,
    val divider: Color,
    // brand / chart
    val brandPrimary: Color,
    val brandSecondary: Color,
    val brandTertiary: Color,
    val brandPrimaryText: Color,
    val brandSecondaryText: Color,
    val brandTertiaryText: Color,
    val chart1: Color,
    val chart2: Color,
    val chart3: Color,
    val chartBaseline: Color,   // dashed baseline line
    val chartGrid: Color,
    val scrim: Color,
)

val LightRevLensColors = RevLensColors(
    background = LightBackground, surface = LightSurface, surfaceElevated = LightSurfaceElevated,
    primaryAction = LightPrimaryAction, onPrimaryAction = LightOnPrimaryAction,
    accentBlue = LightAccentBlue, success = LightSuccess, warning = LightWarning,
    error = LightError,
    textPrimary = LightTextPrimary, textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary, textDisabled = LightTextDisabled,
    borderDefault = LightBorderDefault, divider = LightDivider,
    brandPrimary = BrandPrimary, brandSecondary = BrandSecondary, brandTertiary = BrandTertiary,
    brandPrimaryText = LightBrandPrimaryText, brandSecondaryText = LightBrandSecondaryText,
    brandTertiaryText = LightBrandTertiaryText,
    chart1 = ChartSeries1, chart2 = ChartSeries2, chart3 = ChartSeries3,
    chartBaseline = LightTextTertiary, chartGrid = LightDivider,
    scrim = Color(0x66000000),
)

val DarkRevLensColors = RevLensColors(
    background = DarkBackground, surface = DarkSurface, surfaceElevated = DarkSurfaceElevated,
    primaryAction = DarkPrimaryAction, onPrimaryAction = DarkOnPrimaryAction,
    accentBlue = DarkAccentBlue, success = DarkSuccess, warning = DarkWarning, error = DarkError,
    textPrimary = DarkTextPrimary, textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary, textDisabled = DarkTextDisabled,
    borderDefault = DarkBorderDefault, divider = DarkDivider,
    brandPrimary = BrandPrimary, brandSecondary = BrandSecondary, brandTertiary = BrandTertiary,
    brandPrimaryText = DarkBrandPrimaryText, brandSecondaryText = DarkBrandSecondaryText,
    brandTertiaryText = DarkBrandTertiaryText,
    chart1 = ChartSeries1, chart2 = ChartSeries2, chart3 = ChartSeries3,
    chartBaseline = DarkTextTertiary, chartGrid = DarkDivider,
    scrim = Color(0x99000000),
)

val LocalRevLensColors = staticCompositionLocalOf<RevLensColors> {
    error("RevLensColors not provided. Wrap your UI in RevLensTheme.")
}

/** Usage: RevLensTheme.colors.textSecondary */
object RevLensTheme {
    val colors: RevLensColors
        @Composable @ReadOnlyComposable get() = LocalRevLensColors.current
}
