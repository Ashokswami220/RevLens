package com.example.revlens.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// ───────────── LIGHT mode ─────────────
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceElevated = Color(0xFFF6F6F6)
val LightPrimaryAction = Color(0xFF000000)
val LightOnPrimaryAction = Color(0xFFFFFFFF)
val LightAccentBlue = Color(0xFF276EF1)
val LightSuccess = Color(0xFF048848)
val LightWarning = Color(0xFFFFC043)
val LightError = Color(0xFFE11900)
val LightTextPrimary = Color(0xFF000000)
val LightTextSecondary = Color(0xFF545454)
val LightTextTertiary = Color(0xFF757575)
val LightTextDisabled = Color(0xFFAFAFAF)
val LightBorderDefault = Color(0xFFE5E5E5)
val LightDivider = Color(0xFFE5E5E5)

// ───────────── DARK mode ─────────────
val DarkBackground = Color(0xFF1C1C1C)
val DarkSurface = Color(0xFF1C1C1C)
val DarkSurfaceElevated = Color(0xFF272727)
val DarkPrimaryAction = Color(0xFFFFFFFF)
val DarkOnPrimaryAction = Color(0xFF000000)
val DarkAccentBlue = Color(0xFF276EF1)
val DarkSuccess = Color(0xFF048848)
val DarkWarning = Color(0xFFFFC043)
val DarkError = Color(0xFFE11900)
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFCBCBCB)
val DarkTextTertiary = Color(0xFF999999)
val DarkTextDisabled = Color(0xFF666666)
val DarkBorderDefault = Color(0xFF3D3D3D)
val DarkDivider = Color(0xFF333333)

// ───────────── BRAND (same in both modes) ─────────────
val BrandPrimary = Color(0xFF06B6D4)    // cyan    -> M3 primary
val BrandSecondary = Color(0xFF6366F1)  // indigo  -> M3 secondary
val BrandTertiary = Color(0xFF10B981)   // emerald -> M3 tertiary

// Soft tints (12% alpha) for selected backgrounds / chart fills
val BrandPrimarySoft = Color(0x1F06B6D4)
val BrandSecondarySoft = Color(0x1F6366F1)
val BrandTertiarySoft = Color(0x1F10B981)

// Text-safe variants (optional, small coloured text only)
val LightBrandPrimaryText = Color(0xFF0E7490)
val LightBrandSecondaryText = Color(0xFF4F46E5)
val LightBrandTertiaryText = Color(0xFF047857)
val DarkBrandPrimaryText = BrandPrimary
val DarkBrandSecondaryText = Color(0xFF818CF8)
val DarkBrandTertiaryText = BrandTertiary

// ───────────── CHART series ─────────────
val ChartSeries1 = BrandPrimary
val ChartSeries2 = BrandSecondary
val ChartSeries3 = BrandTertiary
