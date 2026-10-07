# RevLens — Android MVP Plan (Kotlin + Jetpack Compose)

## 1. Overview

**RevLens** is a SaaS revenue modeling and what-if simulation app. Founders enter their baseline business numbers once, then simulate pricing, churn, growth and cost changes, save them as scenarios, compare scenarios, forecast revenue and plan MRR goals.

- **Category:** Business / Finance — SaaS financial modeling & analytics
- **Type:** Calculator + dashboard hybrid (mostly local computation)
- **Full product:** 36 screens (34 on Android — Landing and Pricing pages are web-only)
- **MVP:** 14 screens (+ Forgot Password recommended = 15)

---

## 2. MVP Screens (14)

| # | Screen | Graph | Tab |
|---|--------|-------|-----|
| 1 | Login | Auth | — |
| 2 | Sign Up | Auth | — |
| 3 | Onboarding (Welcome + Create Workspace, 2-step pager) | Onboarding | — |
| 4 | Business Setup | Onboarding | — |
| 5 | Dashboard | Main | Home |
| 6 | Pricing Simulator | Main | Simulate |
| 7 | What-If Analysis | Main | Simulate |
| 8 | Break-Even Calculator | Main | Simulate |
| 9 | Scenario Builder | Main | Scenarios |
| 10 | Scenario Comparison | Main | Scenarios (push) |
| 11 | Revenue Forecast | Main | Forecast |
| 12 | MRR Goal Planner | Main | Forecast |
| 13 | SaaS Metrics | Main | Metrics |
| 14 | Settings | Main | Avatar icon (full screen) |

Optional 15th: **Forgot Password** (Auth graph).

---

## 3. Navigation Map

```
App start (SplashScreen → check session)
 ├─ AUTH graph          (no bottom bar)
 │    Login ⇄ Sign Up   (+ Forgot Password)
 ├─ ONBOARDING graph    (no bottom bar)
 │    Onboarding → Business Setup
 └─ MAIN graph          (bottom bar on the 5 root screens)
      🏠 Home       → Dashboard
      🎚 Simulate   → Pricing Simulator · What-If · Break-Even
      🗂 Scenarios  → Scenario Builder → Scenario Comparison
      📈 Forecast   → Revenue Forecast · MRR Goal Planner
      📊 Metrics    → SaaS Metrics
      ⚙ Settings   ← avatar icon in top bar
```

### Gate logic

| State | Destination |
|-------|-------------|
| No session | Auth graph |
| Session, setup not done | Onboarding graph |
| Session + setup done | Main graph |

Pop the back stack after each gate so Back does not return to Login/Onboarding.

### Bottom bar visibility

- **Visible:** Dashboard, Pricing Simulator, Scenario Builder, Revenue Forecast, SaaS Metrics (and their chip-row siblings)
- **Hidden:** Auth screens, Onboarding screens, Scenario Comparison, Settings

---

## 4. Screen Components

| Screen | Main components |
|--------|-----------------|
| Login | Logo, email field, password field, primary button (loading), "Forgot?" text button, Snackbar |
| Sign Up | Name/email/password fields, strength bar, terms checkbox, primary button |
| Onboarding | HorizontalPager, dot indicator, workspace-name field, currency dropdown, Next/Skip |
| Business Setup | Number fields (MRR, customers, churn %, CAC, costs), plan cards, "Add plan" bottom sheet, Save |
| Dashboard | Top bar + avatar, KPI cards, line chart + range chips, quick-tool grid, recent scenarios, goal ring, pull-to-refresh |
| Pricing Simulator | Plan chips, LabeledSlider, results card, delta chip, before/after bar chart, assumptions sheet, Save as scenario |
| What-If | Stack of LabeledSliders, reset, live impact cards, sensitivity bar chart |
| Break-Even | Cost/price inputs, result cards, revenue-vs-cost line chart |
| Scenario Builder | Scenario cards, FAB, expandable assumption sections, sticky live-result bar, swipe-to-delete, multi-select + Compare |
| Scenario Comparison | Scenario chips, side-by-side metric table, overlay line chart, delta chips, "best" badge |
| Revenue Forecast | Segmented button (6/12/24 mo), best/base/worst chart, assumptions sheet, month-by-month list |
| MRR Goal Planner | Target field, date picker, required-customers card, feasibility indicator, milestone timeline |
| SaaS Metrics | Metric tile grid, health badges, period selector, ModalBottomSheet (formula + sparkline) |
| Settings | Profile header, grouped ListItems + Switches, theme dialog, biometric toggle, logout confirm dialog |

### Shared design-system components

`RevLensTopAppBar` · `PrimaryButton` · `SecondaryButton` · `RevLensTextField` · `NumberField` · `LabeledSlider` · `KpiCard` · `MetricTile` · `DeltaChip` · `SectionCard` · `ScenarioCard` · `RevLensLineChart` · `RevLensBarChart` · `SegmentedTabs` · `ConfirmDialog` · `LoadingState` · `EmptyState` · `ErrorState`

---

## 5. Project File Structure

Single `app` module, package-by-feature, layered inside.

```
revlens/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── res/
        │   └── java/com/revlens/app/
        │       ├── RevLensApp.kt                  # @HiltAndroidApp
        │       ├── MainActivity.kt                # setContent, edge-to-edge, splash
        │       │
        │       ├── navigation/
        │       │   ├── Routes.kt                  # @Serializable route objects
        │       │   ├── RevLensNavHost.kt          # root NavHost + gate logic
        │       │   ├── AuthNavGraph.kt
        │       │   ├── OnboardingNavGraph.kt
        │       │   ├── MainNavGraph.kt
        │       │   └── BottomNavItem.kt
        │       │
        │       ├── core/
        │       │   ├── designsystem/
        │       │   │   ├── theme/                 # Color.kt Type.kt Shape.kt Theme.kt
        │       │   │   └── components/            # KpiCard, LabeledSlider, charts, dialogs...
        │       │   ├── ui/                        # UiState helpers, UiText
        │       │   └── util/                      # CurrencyFormatter, PercentFormatter, DispatcherProvider
        │       │
        │       ├── domain/
        │       │   ├── model/                     # BusinessProfile, Plan, Scenario, Assumptions, MetricResult
        │       │   ├── calc/                      # pure Kotlin, no Android imports
        │       │   │   ├── MrrCalculator.kt
        │       │   │   ├── ChurnCalculator.kt
        │       │   │   ├── LtvCalculator.kt
        │       │   │   ├── PricingSimulationEngine.kt
        │       │   │   ├── BreakEvenCalculator.kt
        │       │   │   ├── ForecastEngine.kt
        │       │   │   ├── WhatIfEngine.kt
        │       │   │   └── MrrGoalPlanner.kt
        │       │   ├── repository/                # interfaces
        │       │   └── usecase/                   # SaveScenario, CompareScenarios, GetMetrics...
        │       │
        │       ├── data/
        │       │   ├── local/
        │       │   │   ├── db/                    # RevLensDatabase, DAOs, entities
        │       │   │   └── prefs/                 # DataStore (onboardingDone, theme, currency)
        │       │   ├── remote/                    # API service, DTOs (auth, sync)
        │       │   ├── mapper/
        │       │   └── repository/                # implementations
        │       │
        │       ├── di/                            # Hilt modules
        │       │
        │       └── feature/
        │           ├── auth/
        │           │   ├── LoginScreen.kt
        │           │   ├── SignUpScreen.kt
        │           │   ├── ForgotPasswordScreen.kt   # optional
        │           │   ├── AuthViewModel.kt
        │           │   └── AuthUiState.kt
        │           ├── onboarding/
        │           │   ├── OnboardingScreen.kt
        │           │   ├── BusinessSetupScreen.kt
        │           │   ├── OnboardingViewModel.kt
        │           │   └── components/
        │           ├── dashboard/
        │           ├── pricing/                   # Pricing Simulator
        │           ├── whatif/
        │           ├── breakeven/
        │           ├── scenario/
        │           │   ├── ScenarioBuilderScreen.kt
        │           │   ├── ScenarioComparisonScreen.kt
        │           │   ├── ScenarioViewModel.kt
        │           │   └── components/
        │           ├── forecast/
        │           ├── goal/                      # MRR Goal Planner
        │           ├── metrics/                   # SaaS Metrics
        │           └── settings/
        │
        ├── test/                                  # unit tests (calculators first)
        └── androidTest/                           # Compose UI tests
```

### Per-feature file pattern

```
feature/<name>/
├── <Name>Screen.kt        # stateless UI + route composable
├── <Name>ViewModel.kt     # StateFlow<UiState>, onEvent()
├── <Name>UiState.kt       # data class / sealed interface
└── components/            # composables used only by this feature
```

---

## 6. Tech Stack

| Concern | Choice |
|---------|--------|
| UI | Jetpack Compose + Material 3 (Compose BOM) |
| Architecture | MVVM, unidirectional data flow |
| Navigation | Navigation Compose, type-safe routes, nested graphs |
| DI | Hilt |
| Local data | Room (scenarios, business profile), DataStore (flags/prefs) |
| Network | Retrofit/Ktor + kotlinx.serialization (when backend is ready) |
| Charts | Vico (Compose-native) |
| Async | Coroutines + Flow |
| Build | Gradle version catalog, Kotlin 2.x Compose plugin, KSP |
| Splash | core-splashscreen API |
| Testing | JUnit, Turbine, Compose UI test |

---

## 7. Rules to Remember

1. All formulas live in `domain/calc` — pure Kotlin, unit tested.
2. Business Setup is the baseline every simulator reads from.
3. Use `BigDecimal` / minor units for stored money; format with `NumberFormat`.
4. Pair every slider with a typed number field.
5. Hoist state; keep screens stateless; use `collectAsStateWithLifecycle()`.
6. Debounce or `derivedStateOf` heavy slider calculations; run them off the main thread.
7. Build the design system and calc engine before the screens.
8. Mock data first, backend second.
9. Digital subscriptions in-app need Google Play Billing (later phase).

---

## 8. Suggested Build Order

1. Project setup, theme, shared components
2. `domain/calc` engine + unit tests
3. Navigation shell (gate logic, bottom bar, empty screens)
4. Business Setup + Onboarding (creates baseline data)
5. Dashboard + SaaS Metrics
6. Pricing Simulator → What-If → Break-Even
7. Scenario Builder → Scenario Comparison
8. Revenue Forecast → MRR Goal Planner
9. Auth screens wired to backend
10. Settings, polish, dark mode, accessibility, tests

---

## 9. After the MVP (22 remaining screens)

Customer Analytics · Cohort Analysis · Revenue Breakdown · LTV · CAC Analytics · Growth Simulator · Data Import · Experiments · Experiment Details · Reports · Integrations · API & Webhooks · Workspace Management · Team & Permissions · Billing · Profile · Security Settings · Notifications · Forgot/Reset Password
