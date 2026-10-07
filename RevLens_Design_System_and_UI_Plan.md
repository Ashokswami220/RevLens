# RevLens — Design System & Screen UI Plan

Kotlin + Jetpack Compose + Material 3 (customised). Light and dark mode.

---

## 0. Design Direction (the "vibe")

A flat, bold, high-contrast, monochrome-first UI. The interface itself is black / white / grey. **Colour appears mostly in the data**, so charts, sliders and progress pop against a calm shell.

| Principle | What it means in RevLens |
|-----------|--------------------------|
| **Ink buttons** | Main CTA = `primaryAction` (black in light, white in dark), pill shape, 52dp tall |
| **Flat surfaces** | Cards are filled with `surfaceElevated`, no shadows, no outlines. Depth = tone, not shadow |
| **Filled inputs** | Inputs are grey-filled (`surfaceElevated`), no border until focused (2dp ink border) |
| **Big type, big numbers** | Large bold headings and hero numbers (MRR) with tabular figures |
| **Pill chips** | Filters are pills. Selected = ink fill with inverted text |
| **Bottom-sheet-first** | Secondary actions and details open in bottom sheets, not new screens |
| **Colour = data** | Cyan / indigo / emerald are used for chart lines, slider tracks, switches, progress, active indicators |
| **Thumb-friendly** | Sticky bottom CTA, 48dp+ touch targets, 20dp screen padding |

**Vocabulary used below**
- **ink** = `primaryAction` / `onPrimaryAction` (black ↔ white depending on mode)
- **soft** = brand colour at 12% alpha (used for selected backgrounds and chart fills)

---

## 1. Colour Tokens

### 1.1 Neutral palette (your palette — backgrounds and components)

| Token | Light | Dark | Used for |
|-------|-------|------|----------|
| `background` | `#FFFFFF` | `#1C1C1C` | Screen background |
| `surface` | `#FFFFFF` | `#1C1C1C` | App bars, sheets, dialogs, bottom bar |
| `surfaceElevated` | `#F6F6F6` | `#272727` | Cards, inputs, chips, tiles, skeletons |
| `primaryAction` (ink) | `#000000` | `#FFFFFF` | CTA buttons, selected chips, FAB, tooltips, snackbar |
| `onPrimaryAction` | `#FFFFFF` | `#000000` | Text/icons on ink |
| `accentBlue` | `#276EF1` | `#276EF1` | Links, text buttons, info |
| `success` | `#048848` | `#048848` | Positive deltas, "On track", "Best" |
| `warning` | `#FFC043` | `#FFC043` | "Stretch", caution (text on it = black) |
| `error` | `#E11900` | `#E11900` | Negative deltas, validation, destructive |
| `textPrimary` | `#000000` | `#FFFFFF` | Titles, values, body |
| `textSecondary` | `#545454` | `#CBCBCB` | Subtitles, labels |
| `textTertiary` | `#757575` | `#999999` | Captions, axis labels, hints |
| `textDisabled` | `#AFAFAF` | `#666666` | Disabled text and icons |
| `borderDefault` | `#E5E5E5` | `#3D3D3D` | Focus-less borders, slider inactive track, handles |
| `divider` | `#E5E5E5` | `#333333` | List dividers, chart grid lines |

### 1.2 Brand / data colours (identical in both modes)

| Token | Hex | M3 role | Used for |
|-------|-----|---------|----------|
| `brandPrimary` (cyan) | `#06B6D4` | `primary` | Chart series 1, slider active track, switch ON, progress fill, active-tab indicator, focus accents |
| `brandSecondary` (indigo) | `#6366F1` | `secondary` | Chart series 2, comparison scenario B, retention metrics |
| `brandTertiary` (emerald) | `#10B981` | `tertiary` | Chart series 3, comparison scenario C, unit-economics metrics, profit zone |

**Soft tints (12% alpha):** `brandPrimarySoft #1F06B6D4`, `brandSecondarySoft #1F6366F1`, `brandTertiarySoft #1F10B981`

**Text-safe variants (optional)** — only for small coloured *text* on light backgrounds, where the pure brand colours are too light:

| Token | Light | Dark |
|-------|-------|------|
| `brandPrimaryText` | `#0E7490` | `#06B6D4` |
| `brandSecondaryText` | `#4F46E5` | `#818CF8` |
| `brandTertiaryText` | `#047857` | `#10B981` |

> Chart lines, fills, sliders and indicators always use the exact brand hex values. The text-safe variants are never used for graphics.

### 1.3 Text on brand fills

| Fill | Text/icon colour | Reason |
|------|------------------|--------|
| Cyan `#06B6D4` | **Black** `#000000` | ≈8.6:1 (white would be ≈2.4:1) |
| Indigo `#6366F1` | **White** `#FFFFFF` | ≈4.5:1 |
| Emerald `#10B981` | **Black** `#000000` | ≈8.3:1 (white would be ≈2.5:1) |
| Warning `#FFC043` | **Black** | high contrast |
| Success / Error | **White** | ≈4.5:1 / ≈4.8:1 |

### 1.4 Contrast cheat-sheet (approximate)

| Pair | Light | Dark |
|------|-------|------|
| `textPrimary` on `background` | 21:1 | 17:1 |
| `textSecondary` on `background` | 7.6:1 | 10.5:1 |
| `textTertiary` on `background` | 4.6:1 | 6.0:1 |
| Cyan on `background` | 2.4:1 ✗ for text | 7.0:1 ✓ |
| Indigo on `background` | 4.5:1 | 3.8:1 (graphics/large only) |
| Emerald on `background` | 2.5:1 ✗ for text | 6.7:1 ✓ |
| `accentBlue` on `background` | 4.6:1 | 3.7:1 (keep links bold, ≥16sp) |

**Consequence for charts in light mode:** cyan and emerald lines are under 3:1 against white. Compensate with line width ≥ 3dp, direct end-labels, markers and dashes (never rely on colour alone).

---

## 2. Kotlin — `Color.kt`

```kotlin
package com.revlens.app.core.designsystem.theme

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
```

---

## 3. Kotlin — `RevLensColors.kt` and `Theme.kt`

Material 3's `ColorScheme` doesn't have slots for things like `success`, `textTertiary` or `primaryAction`, so we add our own holder.

```kotlin
package com.revlens.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
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
    accentBlue = LightAccentBlue, success = LightSuccess, warning = LightWarning, error = LightError,
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

/** Usage: RevLens.colors.textSecondary */
object RevLens {
    val colors: RevLensColors
        @Composable @ReadOnlyComposable get() = LocalRevLensColors.current
}
```

```kotlin
package com.revlens.app.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,       onPrimary = Color.Black,
    secondary = BrandSecondary,   onSecondary = Color.White,
    tertiary = BrandTertiary,     onTertiary = Color.Black,
    background = LightBackground, onBackground = LightTextPrimary,
    surface = LightSurface,       onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceElevated, onSurfaceVariant = LightTextSecondary,
    surfaceTint = Color.Transparent,                 // no purple tonal tint
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightSurfaceElevated,
    surfaceContainerHigh = LightSurfaceElevated,
    surfaceContainerHighest = LightSurfaceElevated,
    outline = LightBorderDefault, outlineVariant = LightDivider,
    error = LightError,           onError = Color.White,
    scrim = Color.Black,
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimary,       onPrimary = Color.Black,
    secondary = BrandSecondary,   onSecondary = Color.White,
    tertiary = BrandTertiary,     onTertiary = Color.Black,
    background = DarkBackground,  onBackground = DarkTextPrimary,
    surface = DarkSurface,        onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated, onSurfaceVariant = DarkTextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = DarkSurface,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceElevated,
    surfaceContainerHighest = DarkSurfaceElevated,
    outline = DarkBorderDefault,  outlineVariant = DarkDivider,
    error = DarkError,            onError = Color.White,
    scrim = Color.Black,
)

@Composable
fun RevLensTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),   // Settings: System / Light / Dark
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
```

**Rules**
- **No dynamic colour** (`dynamicColorScheme`) — it would override the brand.
- Don't rely on M3 component defaults. Build our own wrappers (`PrimaryButton`, `RevLensTextField`, …) that read `RevLens.colors`.
- Use `enableEdgeToEdge(...)` with system-bar icon style following the theme.

---

## 4. Typography, Shape, Spacing, Elevation, Motion

### 4.1 Typography (font: **Inter**, bundled in `res/font`)

| Style | Size / line | Weight | Used for |
|-------|-------------|--------|----------|
| `displayLarge` | 36 / 44 | Bold, `tnum` | Hero numbers (MRR, results) |
| `headlineLarge` | 28 / 36 | Bold | Screen titles inside content |
| `headlineMedium` | 22 / 28 | Bold | Section titles, sheet titles |
| `titleLarge` | 20 / 28 | SemiBold | Top app bar title |
| `titleMedium` | 16 / 24 | SemiBold | Card titles, list titles, button label |
| `bodyLarge` | 16 / 24 | Regular | Body, input text |
| `bodyMedium` | 14 / 20 | Regular | Secondary body |
| `labelLarge` | 14 / 20 | Medium | Chips, tabs |
| `labelMedium` | 12 / 16 | Medium | Captions, axis labels, helper text |
| `labelSmall` | 11 / 16 | Medium, +0.5 tracking, UPPERCASE | Section headers in Settings |

Always apply `fontFeatureSettings = "tnum"` (tabular figures) to numeric text so values don't jitter while sliders move.

### 4.2 Shape

| Token | Radius | Used for |
|-------|--------|----------|
| `extraSmall` | 8dp | Tooltips, small badges |
| `small` | 12dp | Text fields, table cells |
| `medium` | 16dp | Cards, tiles |
| `large` | 24dp | Bottom sheets (top corners), hero cards |
| `PillShape` | 50% | Buttons, chips, segmented control, delta chips |

### 4.3 Spacing (4dp grid)

`4 · 8 · 12 · 16 · 20 · 24 · 32 · 48`

- Screen horizontal padding: **20dp**
- Card inner padding: **16dp**
- Gap between cards: **12dp**
- Section gap: **24dp**
- Min touch target: **48dp**

### 4.4 Elevation

| Surface | Treatment |
|---------|-----------|
| Cards, inputs, chips | Flat, `surfaceElevated` fill, **no shadow** |
| App bar | Flat; shows a 1dp `divider` line only when content is scrolled |
| Bottom nav | `surface` + 1dp top `divider` |
| FAB, snackbar | Small shadow (4–6dp) |
| Bottom sheet / dialog | `surface` + `scrim` behind, no shadow |

### 4.5 Motion

| Interaction | Spec |
|-------------|------|
| Button / chip press | scale 0.98, 100ms |
| Result numbers | animate count-up/down, 300ms ease-out |
| Chart line draw-in | 600ms on first show; value changes morph 250ms |
| Bottom sheet | spring (no bounce), 300ms |
| Screen transition | fade-through 200ms (tab switch), shared slide 250ms (push) |
| Slider drag | value bubble follows thumb, light haptic on release |

### 4.6 Icons
Material Symbols (Rounded, weight 500), 24dp. Inside a 40dp circle (`surfaceElevated`) for list-leading icons.

---

## 5. Colour Usage Rules

### 5.1 Where each colour goes

| Element | Colour |
|---------|--------|
| Primary CTA button | `primaryAction` fill, `onPrimaryAction` text |
| Secondary button | `surfaceElevated` fill, `textPrimary` text |
| Text button / links | `accentBlue`, bold |
| Selected chip | `primaryAction` fill, `onPrimaryAction` text |
| Unselected chip | `surfaceElevated` fill, `textPrimary` text |
| Slider active track | `brandPrimary` |
| Switch ON track | `brandPrimary` (white thumb with check icon) |
| Progress bar / step bar | `brandPrimary` on `surfaceElevated` track |
| Bottom-nav active indicator | 3dp `brandPrimary` bar above the icon, icon + label in `textPrimary` |
| Input focus | 2dp `primaryAction` border |
| Input error | 2dp `error` border + `error` helper text |
| Positive delta | `success` fill, white text, ▲ icon |
| Negative delta | `error` fill, white text, ▼ icon |
| Neutral delta | `surfaceElevated` fill, `textSecondary` text |
| Health "Good / Stretch / Poor" | `success` / `warning` (black text) / `error` |
| Tooltip on charts | `primaryAction` fill, `onPrimaryAction` text |
| Links in dark mode | `accentBlue`, keep bold |

### 5.2 Chart colours

| Chart element | Colour / style |
|---------------|----------------|
| Single series (e.g. MRR trend) | `chart1` cyan, 3dp solid, area fill cyan alpha 0.18→0 (dark: 0.28→0) |
| Scenario A / B / C | cyan / indigo / emerald, 3dp solid |
| Baseline / "today" line | `chartBaseline`, 2dp **dashed** |
| Forecast base | cyan solid + confidence band (cyan 12%) |
| Forecast optimistic | emerald, dashed |
| Forecast conservative | indigo, dashed |
| Break-even: revenue / cost | cyan / indigo; profit zone `brandTertiarySoft` |
| Break-even marker | 10dp circle with `surface` ring + dashed vertical line |
| Before / after bars | before = `borderDefault`, after = cyan |
| Sensitivity (tornado) bars | positive = emerald, negative = `error` |
| Sparklines by metric group | revenue = cyan · retention = indigo · unit economics = emerald |
| Goal ring | sweep gradient cyan → indigo, track `surfaceElevated` |
| Grid lines | `chartGrid` 1dp |
| Axis labels | `textTertiary`, `labelMedium` |

Colour is never the only signal: use dash styles, end-of-line labels and distinct markers too.

### 5.3 Gradients (only 3 allowed)
1. Logo mark (cyan → indigo)
2. Onboarding hero illustration
3. Goal progress ring

---

## 6. Component Library

All components live in `core/designsystem/components/`. They read `RevLens.colors`, never hard-coded hex.

### 6.1 Buttons

| Component | Spec |
|-----------|------|
| **PrimaryButton** | 52dp tall, pill, full-width by default. Bg `primaryAction`, text `onPrimaryAction` `titleMedium`. Pressed: scale 0.98. Disabled: bg `surfaceElevated`, text `textDisabled`. Loading: 20dp spinner replaces label (width unchanged) |
| **SecondaryButton** | Same size. Bg `surfaceElevated`, text `textPrimary` |
| **DestructiveButton** | Same size. Bg `error`, text white. Used in logout / delete dialogs |
| **TextLinkButton** | No bg, `accentBlue`, `labelLarge` bold, 48dp touch height |
| **RevLensIconButton** | 40dp circle, `surfaceElevated`, 24dp icon |
| **RevLensFab (extended)** | 56dp, pill, `primaryAction` bg, icon + label, small shadow |
| **StickyCtaBar** | `surface` bg + 1dp top `divider`, 16dp padding, holds 1 PrimaryButton (+ optional text button) above the nav bar/insets |

### 6.2 Inputs

| Component | Spec |
|-----------|------|
| **RevLensTextField** | 56dp, radius 12dp, filled `surfaceElevated`. Label above in `labelMedium` `textSecondary`. Text `bodyLarge`. Cursor `textPrimary`. Focus: 2dp `primaryAction` border. Error: 2dp `error` border + helper in `error`. Disabled: `textDisabled` |
| **PasswordField** | RevLensTextField + eye icon toggle; optional 4-segment strength bar (grey → `warning` → `success`) |
| **NumberField** | RevLensTextField with decimal keyboard, `tnum`, optional prefix (`$`) and suffix (`%`) in `textSecondary`; input filtering and range validation |
| **DropdownField** | Same look as text field + chevron; menu is a `surface` card, radius 12dp, selected row in `surfaceElevated` |
| **DateField** | Looks like text field + calendar icon; opens Material DatePicker (selected day = `primaryAction`, today ring = `brandPrimary`) |
| **LabeledSlider** | Row 1: label (left) + value in a `NumberField`-style chip (right). Row 2: slider. Track 4dp: active `brandPrimary`, inactive `borderDefault`. Thumb: 20dp `primaryAction` circle with 3dp `surface` ring. While dragging a bubble (`primaryAction` fill) shows the value. Caption below: "Current: $29" in `textTertiary`. Typing in the field moves the slider and vice-versa |
| **RevLensSwitch** | Track 52×32. ON = `brandPrimary` with white thumb + check icon; OFF = `borderDefault` track, white thumb |
| **RevLensCheckbox** | 24dp, radius 6dp. Checked = `primaryAction` fill + `onPrimaryAction` tick; unchecked = 2dp `borderDefault` border |
| **RadioRow** | 24dp ring; selected = `primaryAction` dot |

### 6.3 Selection

| Component | Spec |
|-----------|------|
| **FilterChip / ChoiceChip** | 36dp, pill, `labelLarge`. Unselected: `surfaceElevated` + `textPrimary`. Selected: `primaryAction` + `onPrimaryAction`. Optional 8dp colour dot (for scenario colours) |
| **SegmentedTabs** | Container 44dp, pill, `surfaceElevated`. Selected segment = pill in `surface` (light) / `borderDefault` (dark), `labelLarge` bold. Used for section switching (Pricing / What-If / Break-Even) and horizon (6 / 12 / 24 mo) |
| **PagerDots** | 8dp dots `borderDefault`; active = 24×8 pill `primaryAction` |
| **StepProgressBar** | 3 segments, 4dp tall, gap 4dp, filled = `brandPrimary`, empty = `surfaceElevated` |

### 6.4 Navigation

| Component | Spec |
|-----------|------|
| **RevLensTopAppBar** | 56dp, bg `background`, no elevation. Title `titleLarge` left-aligned. Leading: back/close icon button. Trailing: up to 2 icon buttons or avatar. Divider line appears on scroll |
| **LargeTitleHeader** | In-content title `headlineLarge` + optional subtitle `bodyMedium` `textSecondary` (used on Login, Sign Up, Business Setup…) |
| **RevLensBottomBar** | 64dp + system inset, bg `surface`, 1dp top `divider`, 5 items. Item: 24dp icon + `labelMedium`. Active = `textPrimary` icon/label (filled icon) + 3dp×24dp `brandPrimary` bar on top; inactive = `textTertiary` |
| **WorkspaceSwitcher** | Text button: workspace name `titleLarge` + chevron; opens bottom sheet |
| **Avatar** | 36dp circle, initials `labelLarge`, bg `brandSecondarySoft`, text `brandSecondaryText` |

### 6.5 Data display

| Component | Spec |
|-----------|------|
| **SectionCard** | `surfaceElevated`, radius 16dp, padding 16dp. Optional header row: `titleMedium` + trailing action |
| **KpiCard** | SectionCard. Label (`labelMedium`, `textSecondary`) → value (`headlineMedium`, `tnum`) → DeltaChip → optional 32dp sparkline. Tap = open detail |
| **MetricTile** | Compact KpiCard (min height 120dp) for the 2-column Metrics grid. Sparkline colour by metric group |
| **DeltaChip** | 24dp pill. ▲/▼ 12dp icon + `labelMedium` bold. Positive `success`, negative `error` (white text). Neutral = `surfaceElevated` + `textSecondary` |
| **HealthBadge** | 24dp pill. "Good" `success` / "Stretch" `warning` + black text / "Poor" `error` |
| **ScenarioCard** | SectionCard. Left 10dp colour dot (assigned colour when selected). Title `titleMedium`, subtitle `bodyMedium` `textSecondary` ("Pro $29 → $39 · churn +1.5%"), trailing DeltaChip (ΔMRR). Selected: 2dp `primaryAction` border + check circle. Swipe-to-delete reveals `error` bg |
| **PlanCard** | SectionCard. Plan name, price/billing, customer count, trailing edit icon. "Add plan" = dashed `borderDefault` outline tile |
| **ToolTile** | 96×96 tile, `surfaceElevated`. 40dp icon circle (soft cyan / indigo / emerald) + `labelLarge` label |
| **SettingsRow** | 56dp row. Leading 40dp icon circle, title `bodyLarge`, optional value `textSecondary`, trailing chevron/switch. 1dp `divider` inset 68dp |
| **ComparisonTable** | Sticky first column (labels). Header cells: colour dot + scenario name. Rows 52dp with 1dp `divider`. Best value in bold + small "Best" `success` HealthBadge. Delta vs baseline as `labelMedium` below the value |
| **MilestoneStepper** | Vertical line `borderDefault`. Done = 24dp `brandTertiary` circle + black check. Current = 24dp ring `brandPrimary` 3dp. Future = 24dp ring `borderDefault` |
| **FormulaBox** | `surfaceElevated`, radius 12dp, monospace `bodyMedium`, 16dp padding |

### 6.6 Charts (wrap Vico)

| Component | Spec |
|-----------|------|
| **RevLensLineChart** | Multi-series, 3dp round-cap lines, area fill for single series, 1dp dashed grid (`chartGrid`), 4–5 y-ticks, x labels `labelMedium` `textTertiary`. Touch: vertical guide line + ink tooltip (`primaryAction` pill: date + values with series colour dots) |
| **RevLensBarChart** | Bars radius 6dp top corners; grouped before/after; value labels above bars |
| **Sparkline** | No axes, 2dp line, 32dp tall, end dot 6dp |
| **ProgressRing** | 160dp, stroke 14dp, round caps, sweep gradient cyan→indigo; centre: `displayLarge` % + caption |
| **ChartLegend** | Row of colour dot (8dp, or dash icon for dashed series) + `labelMedium` name |
| **ProgressBar** | 6dp, pill, `brandPrimary` fill on `surfaceElevated` track |

### 6.7 Overlays & feedback

| Component | Spec |
|-----------|------|
| **RevLensBottomSheet** | `surface`, top radius 24dp, 36×4dp handle `borderDefault`, scrim from tokens. Full-height variant for editors |
| **ConfirmDialog** | `surface`, radius 24dp, 24dp padding. Title `headlineMedium`, body `bodyMedium`. Actions: PrimaryButton / DestructiveButton + text button |
| **RevLensSnackbar** | `primaryAction` bg, `onPrimaryAction` text, action text `accentBlue`, radius 12dp. Error variant: `error` bg + white |
| **Skeleton (shimmer)** | `surfaceElevated` → `borderDefault` sweep, 1.2s, same shape as the real content |
| **EmptyState** | 120dp line illustration in `borderDefault` + cyan accent, `titleMedium`, `bodyMedium` `textSecondary`, PrimaryButton |
| **ErrorState** | Icon in `error` soft circle, message, "Try again" SecondaryButton |
| **Tooltip (chart)** | `primaryAction` fill, radius 8dp, `labelMedium` |

---

## 7. Screen-by-Screen UI Plan

All screens: `background` bg, 20dp side padding, edge-to-edge with insets handled in `Scaffold`. Every data screen has **Loading** (skeleton), **Empty**, **Error** states unless noted.

### 7.1 Login

```
┌──────────────────────────────┐
│  (logo mark)  RevLens        │
│                              │
│  Welcome back                │  headlineLarge
│  Log in to model your        │  bodyMedium / textSecondary
│  revenue.                    │
│                              │
│  Email                       │
│  [ you@company.com        ]  │
│  Password                    │
│  [ ••••••••            👁 ]  │
│                Forgot password?  accentBlue
│                              │
│  [        Log in          ]  │  PrimaryButton (ink)
│  ───────────  or  ─────────  │
│  [  G  Continue with Google ]│  SecondaryButton
│                              │
│  New here? Sign up           │  textSecondary + accentBlue bold
└──────────────────────────────┘
```
- **Components:** Logo mark, LargeTitleHeader, RevLensTextField, PasswordField, TextLinkButton, PrimaryButton, SecondaryButton, Snackbar.
- **States:** button loading, inline field errors, snackbar for network/auth failure.
- **Keyboard:** screen scrolls; CTA stays visible above the IME.

### 7.2 Sign Up

```
│  ←                           │
│  Create your account         │
│  Start modelling in minutes. │
│  Full name  [              ] │
│  Email      [              ] │
│  Password   [           👁 ] │
│  ▮▮▮▯  Good strength         │  strength bar
│  ☑ I agree to Terms & Privacy│  (links accentBlue)
│  [     Create account     ]  │
│  Already have an account? Log in
```
- **Components:** TopAppBar (back only), LargeTitleHeader, RevLensTextField ×2, PasswordField + strength bar, RevLensCheckbox, PrimaryButton (disabled until valid + terms checked).

### 7.3 Forgot Password (optional)

- Back button → title "Reset password" → one email field → PrimaryButton "Send reset link".
- **Success state:** 72dp `success` soft circle with check, "Check your inbox", SecondaryButton "Back to log in".

### 7.4 Onboarding (Welcome + Create Workspace, pager)

```
│  ▮▮▯   (step 1 of 3)    Skip │  StepProgressBar (cyan)
│  ┌────────────────────────┐  │
│  │  hero illustration     │  │  surfaceElevated, radius 24
│  │  3 gradient lines      │  │  cyan / indigo / emerald
│  └────────────────────────┘  │
│  See your revenue            │  headlineLarge
│  before it happens           │
│  (●) Simulate price changes  │  icon circles: cyan soft
│  (●) Compare scenarios       │  indigo soft
│  (●) Plan MRR goals          │  emerald soft
│          ━  ●  ●             │  PagerDots
│  [        Continue        ]  │
```
- **Page 2 – Create workspace:** title "Name your workspace", RevLensTextField (workspace name), DropdownField (currency), PrimaryButton "Continue".
- **Components:** StepProgressBar, HorizontalPager, PagerDots, ChartIllustration (hero), RevLensTextField, DropdownField, PrimaryButton, TextLinkButton (Skip on page 1 only).

### 7.5 Business Setup

```
│  ←          ▮▮▮ step 3 of 3  │
│  Your business today         │
│  This is your baseline for   │
│  every simulation.           │
│  ┌ Revenue ────────────────┐ │
│  │ Current MRR   [$ 48,200]│ │
│  │ Customers     [   1,240]│ │
│  │ ARPU (auto)   [  $38.87]│ │  read-only, "Auto" tag
│  └─────────────────────────┘ │
│  ┌ Retention & growth ─────┐ │
│  │ Monthly churn [   2.1 %]│ │
│  │ Monthly growth[   6.0 %]│ │
│  └─────────────────────────┘ │
│  ┌ Acquisition & costs ────┐ │
│  │ CAC / Fixed costs / Var.│ │
│  └─────────────────────────┘ │
│  Pricing plans               │
│  [Starter $9  ·  620 cust. ] │  PlanCard
│  [Pro $29     ·  480 cust. ] │
│  [ + Add plan             ]  │  dashed tile → bottom sheet
│ ┌────────────────────────┐   │
│ │  [ Save & continue ]   │   │  StickyCtaBar
```
- **Components:** TopAppBar, StepProgressBar, LargeTitleHeader, SectionCard ×3, NumberField (prefix `$` / suffix `%`), PlanCard, Add-plan BottomSheet (name, price, billing period DropdownField, customers), StickyCtaBar.
- **Validation:** required fields, churn 0–100%, inline errors; ARPU auto = MRR ÷ customers.
- **Reused later:** same screen reachable from Settings → "Edit business baseline" (top bar title becomes "Business baseline", CTA "Save").

### 7.6 Dashboard (Home tab)

```
│  Acme Inc ▾              (AV)│  WorkspaceSwitcher + Avatar → Settings
│                              │
│  MRR                         │  labelMedium
│  $48,200   [▲ 6.2%]          │  displayLarge + DeltaChip
│  vs last month               │
│  ┌────────────────────────┐  │
│  │   ╱╲_╱‾‾╲__╱‾‾‾        │  │  RevLensLineChart (cyan + fill)
│  │  [3M] [6M] [12M]       │  │  chips (ink when selected)
│  └────────────────────────┘  │
│  ┌ ARR ───┐  ┌ Churn ───┐    │
│  │ $578k  │  │ 2.1%     │    │  KpiCard 2×2
│  │ ▲ 4%   │  │ ▼ 0.3    │    │
│  ├ Customers ┤ ├ ARPU ──┤    │
│  ┌────────────────────────┐  │
│  │ (ring)  Goal $65k      │  │  Goal card: ProgressRing small + HealthBadge
│  │  72%    On track       │  │
│  └────────────────────────┘  │
│  Quick tools                 │  headlineMedium
│  [Pricing][What-if][Break-even]→ ToolTile LazyRow
│  Recent scenarios   See all  │
│  [ScenarioCard] [ScenarioCard]
│  ▔ Home Simulate Scenarios Forecast Metrics
```
- **Components:** TopAppBar (WorkspaceSwitcher, Avatar), DeltaChip, RevLensLineChart, FilterChips, KpiCard ×4, goal SectionCard with mini ProgressRing + HealthBadge, ToolTile row, ScenarioCard (compact), BottomBar. Pull-to-refresh with `brandPrimary` indicator.
- **Taps:** Avatar → Settings. KPI card → Metrics. Goal card → MRR Goal Planner. ToolTile → that tool. "See all" → Scenarios.
- **Empty:** If no data, show "Add your baseline" EmptyState → Business Setup.

### 7.7 Pricing Simulator (Simulate tab, root)

```
│  Simulate                    │  TopAppBar
│  [Pricing|What-If|Break-Even]│  SegmentedTabs
│  Plan                        │
│  [Starter][ Pro ][Team]      │  ChoiceChips (Pro selected = ink)
│  ┌ Price ──────────────────┐ │
│  │ New price      [$ 39 ]  │ │  LabeledSlider
│  │ ──────●─────────        │ │
│  │ Current: $29            │ │
│  ├ Assumptions ────────────┤ │
│  │ Churn change   [+1.5 %] │ │  LabeledSlider
│  │ Customers on plan [480] │ │  NumberField
│  └─────────────────────────┘ │
│  ┌ Result ─────────────────┐ │  hero card (radius 24)
│  │ New MRR (12 mo)         │ │
│  │ $72,400   [▲ 14.2%]     │ │  displayLarge, animated
│  │ ▇ before   ▇▇▇ after    │ │  before/after bars (grey / cyan)
│  └─────────────────────────┘ │
│  ┌ 12-month projection ────┐ │
│  │ baseline ┄┄  simulated ━│ │  line chart: grey dashed vs cyan
│  └─────────────────────────┘ │
│ [ Save as scenario ]  Reset  │  StickyCtaBar
```
- **Components:** TopAppBar, SegmentedTabs (section switcher), ChoiceChips, SectionCard, LabeledSlider ×2, NumberField, result hero card (displayLarge + DeltaChip + BarChart), RevLensLineChart + ChartLegend, StickyCtaBar (PrimaryButton + TextLinkButton "Reset").
- **Behaviour:** every slider change recomputes live (debounced 50–100ms); numbers animate; "Save as scenario" opens a small bottom sheet (name field + Save) then shows a Snackbar with action "View".

### 7.8 What-If Analysis (Simulate tab)

```
│  Simulate                    │
│  [Pricing|What-If|Break-Even]│
│  ┌ Live impact (sticky) ───┐ │
│  │ MRR  ARR   LTV  Payback │ │  4 mini metrics + DeltaChips
│  │ $52k $624k $410  7 mo   │ │
│  └─────────────────────────┘ │
│  Drivers                     │
│  Price            [ +10 % ]  │  LabeledSlider
│  Churn            [ -0.5 %]  │
│  CAC              [ -$12  ]  │
│  Monthly growth   [ +2 %  ]  │
│  Expansion revenue[ +1 %  ]  │
│  ┌ Sensitivity ────────────┐ │
│  │ Price     ▇▇▇▇▇▇ (emerald)│ │  horizontal bars, tornado
│  │ Churn   ▇▇▇▇   (emerald)│ │
│  │ CAC  (error) ▇▇         │ │
│  └─────────────────────────┘ │
│ [ Save as scenario ]  Reset all │
```
- **Components:** SegmentedTabs, sticky SectionCard with 4 MetricTiles (compact) + DeltaChips, LabeledSlider ×5, horizontal BarChart (positive emerald / negative error), StickyCtaBar.
- Live-impact card pins under the tabs while scrolling.

### 7.9 Break-Even Calculator (Simulate tab)

```
│  [Pricing|What-If|Break-Even]│
│  Fixed costs / month  [$ 18,000]
│  Variable cost / cust [$ 6.50   ]
│  Price / customer     [$ 38.87  ]  (prefilled from baseline)
│  ┌ Result ─────────────────┐ │
│  │ Break-even at           │ │
│  │ 214 customers           │ │  displayLarge
│  │ Reached in ~ month 7    │ │
│  │ ▬▬▬▬▬▬▬▬▬▯▯▯  168 / 214 │ │  ProgressBar (cyan)
│  └─────────────────────────┘ │
│  ┌ Revenue vs cost ────────┐ │
│  │   cost ━ (indigo)       │ │
│  │      ╲  ●  ╱ rev (cyan) │ │  intersection marker + tooltip "Break-even"
│  │  loss   ░░░  profit(emerald soft)
│  └─────────────────────────┘ │
│ [ Save as scenario ]         │
```
- **Components:** SegmentedTabs, NumberField ×3, result SectionCard, ProgressBar, RevLensLineChart (2 series + marker + shaded profit zone), ChartLegend, StickyCtaBar.

### 7.10 Scenario Builder (Scenarios tab, root)

```
│  Scenarios            [☑ select]
│  ┌────────────────────────┐  │
│  │ ● Price hike Pro       │  │  ScenarioCard
│  │ Pro $29→$39 · churn +1.5│ │
│  │               [▲ 14.2%]│  │
│  └────────────────────────┘  │
│  ┌────────────────────────┐  │
│  │ ● Cut churn campaign   │  │
│  └────────────────────────┘  │
│                  [ + New ]   │  extended FAB (ink)
```
- **Select mode** (via ☑ icon or long-press): cards show check circles, selected card gets 2dp ink border and a colour dot (cyan → indigo → emerald, max 3). Bottom bar is replaced by StickyCtaBar: **[ Compare (2) ]**.
- **Swipe left** to delete (error bg, trash icon) with Undo snackbar. Long-press menu: Rename, Duplicate, Delete.
- **Editor = full-height bottom sheet** (keeps this as one screen):

```
│  ───  (handle)               │
│  New scenario          ✕     │
│  Name [ Price hike Pro     ] │
│  ▸ Pricing                   │  expandable SectionCards
│     Price change   [ +10 % ] │
│  ▸ Retention                 │
│     Churn change   [ +1.5 %] │
│  ▸ Growth                    │
│  ▸ Costs (CAC)               │
│ ┌────────────────────────┐   │
│ │ 12-mo MRR  $72,400 ▲14%│   │  sticky result bar
│ │ [        Save        ] │   │
```
- **Components:** TopAppBar (+ select icon), ScenarioCard list (LazyColumn), RevLensFab, EmptyState ("No scenarios yet" + "Create scenario"), full-height RevLensBottomSheet, expandable SectionCards, LabeledSlider, sticky result bar, StickyCtaBar (Compare), Snackbar with Undo.

### 7.11 Scenario Comparison (Scenarios tab, pushed — bottom bar hidden)

```
│  ←  Compare                  │
│  [● Pro hike][● Churn cut][● +  ]  chips with colour dots (cyan/indigo/emerald)
│  ┌ MRR over 12 months ─────┐ │
│  │ baseline ┄┄ (grey)      │ │  overlay line chart
│  │ ━ cyan  ━ indigo        │ │
│  └─────────────────────────┘ │
│             │ ● Pro  │ ● Churn │
│  MRR (12m)  │ $72.4k │ $64.1k  │  ComparisonTable
│             │ ▲14%[Best]│ ▲5%   │
│  ARR        │ $869k  │ $769k   │
│  Churn      │ 3.6%   │ 1.6%[Best]
│  LTV        │ $320   │ $486    │
│  Payback    │ 9 mo   │ 6 mo    │
```
- **Components:** TopAppBar (back, overflow), ChoiceChips with colour dots (tap to add/remove, max 3 + baseline), RevLensLineChart (multi-series) + ChartLegend, ComparisonTable (horizontally scrollable if >2 scenarios, sticky label column), "Best" HealthBadge per row.
- **Baseline** is always shown as a grey dashed series.

### 7.12 Revenue Forecast (Forecast tab, root)

```
│  Forecast                    │
│  [Forecast | MRR Goal]       │  SegmentedTabs
│  Based on  [ Baseline     ▾] │  DropdownField (baseline or saved scenario)
│  [ 6 mo | 12 mo | 24 mo ]    │  SegmentedTabs
│  ┌────────────────────────┐  │
│  │  optimistic ┄ (emerald)│  │
│  │  base ━ (cyan) + band  │  │  big chart 240dp
│  │  conservative ┄ (indigo)│ │
│  └────────────────────────┘  │
│  ● Base  ┄ Optimistic  ┄ Conservative   ChartLegend
│  [End MRR][Total rev][Growth]│  3 MetricTiles
│  [ ⚙ Assumptions ]           │  SecondaryButton → sheet
│  Month by month              │
│  Jan  $48.2k   ▲ 6.0%        │  list with dividers
│  Feb  $51.1k   ▲ 6.0%        │
```
- **Components:** SegmentedTabs ×2, DropdownField, RevLensLineChart (3 lines + confidence band), ChartLegend, MetricTile ×3, SecondaryButton, Assumptions BottomSheet (growth model DropdownField: Linear / Compound; churn, expansion LabeledSliders; Apply), month-by-month list (sticky header).

### 7.13 MRR Goal Planner (Forecast tab)

```
│  [Forecast | MRR Goal]       │
│  Target MRR  [$ 65,000]      │
│  Deadline    [ 31 Dec 2026 📅]│
│        ╭─────────╮           │
│       │   72%    │           │  ProgressRing 160dp, gradient cyan→indigo
│        ╰─────────╯           │
│    $46.8k of $65k  [On track]│  HealthBadge
│  ┌ Required ───────────────┐ │
│  │ New customers / month 38│ │  3 MetricTiles in a row/stack
│  │ Growth rate needed  5.4%│ │
│  │ Max churn allowed   2.4%│ │
│  └─────────────────────────┘ │
│  Milestones                  │
│  ✔ Oct  $50k                 │  MilestoneStepper
│  ◯ Nov  $55k  (current)      │
│  ○ Dec  $65k                 │
│ [ Save goal ]                │
```
- **Components:** SegmentedTabs, NumberField, DateField (DatePicker), ProgressRing, HealthBadge (On track = success, Stretch = warning, Unlikely = error), MetricTile ×3, MilestoneStepper, StickyCtaBar.
- Feasibility text underneath badge in `bodyMedium` `textSecondary` ("You need 5.4% growth; your current average is 6.0%").

### 7.14 SaaS Metrics (Metrics tab, root)

```
│  Metrics                     │
│  [This month][3M][12M]       │  FilterChips
│  ┌ Overall health: Good ───┐ │  banner SectionCard + HealthBadge
│  └─────────────────────────┘ │
│  REVENUE                     │  labelSmall (uppercase)
│  [MRR  $48.2k ▲6%  ~~~ ] [ARR ...]  (cyan sparklines)
│  [ARPU ...]                  │
│  RETENTION                   │
│  [Churn 2.1% ▼0.3 ~~~] [NRR 104%]   (indigo sparklines)
│  UNIT ECONOMICS              │
│  [LTV $410][CAC $96]         │  (emerald sparklines)
│  [LTV:CAC 4.3][Payback 7 mo] │
```
- **Components:** TopAppBar, FilterChips, health banner, section labels, MetricTile grid (2 columns, `LazyVerticalGrid`) with Sparkline + DeltaChip.
- **Tap tile → ModalBottomSheet:** metric name, `displayLarge` value, DeltaChip, line chart (series colour = group colour), FormulaBox (e.g. `LTV = ARPU ÷ churn rate`), "What it means" paragraph, benchmark bar with marker ("Good ≥ 3.0").

### 7.15 Settings (pushed from avatar — bottom bar hidden)

```
│  ←  Settings                 │
│  (AV 64)  Aarav Sharma       │  profile header
│           aarav@acme.com  Edit
│  WORKSPACE                   │  labelSmall
│  Workspace name   Acme Inc  >│  SettingsRow
│  Currency            USD    >│
│  Business baseline          >│  → Business Setup
│  APPEARANCE                  │
│  Theme            System    >│  RadioRow dialog: System / Light / Dark
│  NOTIFICATIONS               │
│  Weekly summary       [ ON ] │
│  Goal alerts          [ ON ] │
│  SECURITY                    │
│  Change password            >│
│  Biometric lock       [ OFF ]│
│  ABOUT                       │
│  Version 1.0.0 · Terms · Privacy
│  Log out                     │  error-coloured text row
```
- **Components:** TopAppBar, profile header (Avatar 64dp, text button), SettingsRow, RevLensSwitch, section labels, theme RadioRow dialog, ConfirmDialog (logout → DestructiveButton).
- Theme choice persists in DataStore and is passed to `RevLensTheme(darkTheme = …)`.

---

## 8. Where the Files Go

```
core/designsystem/
├── theme/
│   ├── Color.kt            # section 2
│   ├── RevLensColors.kt    # section 3 (data class + locals)
│   ├── Theme.kt            # section 3 (RevLensTheme)
│   ├── Type.kt             # Inter family + RevLensTypography
│   ├── Shape.kt            # RevLensShapes + PillShape
│   └── Spacing.kt          # object Spacing { val s4 = 4.dp ... }
└── components/
    ├── buttons/    PrimaryButton, SecondaryButton, DestructiveButton, TextLinkButton, RevLensIconButton, RevLensFab, StickyCtaBar
    ├── inputs/     RevLensTextField, PasswordField, NumberField, DropdownField, DateField, LabeledSlider, RevLensSwitch, RevLensCheckbox, RadioRow
    ├── selection/  RevLensChip, SegmentedTabs, PagerDots, StepProgressBar
    ├── navigation/ RevLensTopAppBar, LargeTitleHeader, RevLensBottomBar, WorkspaceSwitcher, Avatar
    ├── display/    SectionCard, KpiCard, MetricTile, DeltaChip, HealthBadge, ScenarioCard, PlanCard, ToolTile, SettingsRow, ComparisonTable, MilestoneStepper, FormulaBox
    ├── charts/     RevLensLineChart, RevLensBarChart, Sparkline, ProgressRing, ChartLegend, RevLensProgressBar
    └── feedback/   RevLensBottomSheet, ConfirmDialog, RevLensSnackbar, SkeletonBox, EmptyState, ErrorState
```

Add `@Preview` (light + dark) for every component so both themes can be checked side by side.

---

## 9. Quick Checklist

**Do**
- Read colours only through `RevLens.colors` / `MaterialTheme.colorScheme`.
- Keep the UI shell neutral. Let cyan / indigo / emerald live in charts, sliders, switches and progress.
- Use tabular figures for every number.
- Pair colour with shape (dash, marker, icon) in charts.
- Test every screen in light and dark, and with large font scale.
- Keep one primary (ink) button per screen.

**Don't**
- Don't use cyan or emerald as small text colour on white.
- Don't add shadows to cards.
- Don't use pure-black backgrounds in dark mode (use `#1C1C1C`).
- Don't enable Material You dynamic colour.
- Don't use more than 3 scenario colours at once.
- Don't hard-code hex values inside screens.
