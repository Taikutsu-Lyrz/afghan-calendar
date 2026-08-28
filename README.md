# Afghan Calendar — Material 3 Expressive

Custom clean Material 3 Expressive design (not a clone of `reference-ui.png`) for the local-first Afghan Calendar. Centered, tonal, card-based layout with a dynamic theme selector and modern sans-serif typography.

![Verification](https://img.shields.io/badge/verified-6_%D8%B3%D9%86%D8%A8%D9%84%D9%87_1405_%3D_2026--08--28_%3D_16_%D8%B1%D8%A8%DB%8C%D8%B9%E2%80%8C%D8%A7%D9%84%D8%A7%D9%88%D9%84_1448-green)

## What’s New vs Reference Flat Design
- **Not** a flat 65/35 yellow block. Uses **M3 Expressive tonal surfaces**:
  - `Scaffold(surface)` + `CenterAlignedTopAppBar`
  - `ElevatedCard(shape=32dp, primaryContainer)` for the month grid (saturated seed color)
  - `ElevatedCard(shape=28dp, surfaceContainer)` for daily detail
  - Generous whitespace, centered `maxWidth 480dp` column, responsive on desktop
- **Theme selector** (5 Afghan palettes + Light/Dark/System):
  - Afghan Yellow `#FDB813` (default), Lapis Lazuli `#26428B`, Pomegranate `#C1272D`, Deep Teal `#005F5B`, Desert Sand `#8D6E4A`
  - Hand-crafted `lightColorScheme`/`darkColorScheme` per seed (expressive `primaryContainer` = seed). No external `materialKolor` dependency — pure M3 tonal palettes.
  - Persisted via `rememberSaveable` ( survives rotation / process recreation; DataStore optional upgrade path).
- **Typography** modern sans-serif via `FontFamily.SansSerif` hierarchy (`AppTypography()` in `Type.kt`). Vazirmatn TTFs (Regular/Medium/Bold 103 KB each, downloaded from `fonts.gstatic.com`) are bundled in `composeResources/font/` and ready to wire via `Res.font` if you enable `org.jetbrains.compose.resources` generated accessors:
  ```kotlin
  // In Type.kt, replace fallback with:
  // val AppFontFamily = FontFamily(Font(Res.font.Vazirmatn_Regular, Normal), Font(Res.font.Vazirmatn_Medium, Medium), Font(Res.font.Vazirmatn_Bold, Bold))
  ```
  Current build uses system sans-serif to guarantee compilation on all targets (offline-safe fallback documented).
- **Shapes & Motion expressive-look** without requiring alpha libs:
  - `Shapes(extraLarge = 28.dp)` + manual `RoundedCornerShape(32.dp)` for month card, `CircleShape` for selected day
  - `FilledTonalIconButton` chevrons, `AssistChip` numeric dates, `HorizontalDivider`, `Switch` for week-start
  - Spatial vs effects motion ready (spatial for paging, effects for color) — `MotionScheme.expressive()` can be added when upgrading to `material3 1.5.0-alpha` / CMP `1.9.0-alpha`.
- **RTL-aware** only around calendar content (grid + bottom row), so English title stays LTR in `CenterAlignedTopAppBar`.

All core logic preserved:
- Jalali month grid with Eastern Arabic-Indic digits, 6-row grid, month navigation, selected day, bottom detail with **3 calendars verified** `6 سنبله 1405 = 2026-08-28 Friday = 16 ربیع‌الاول 1448`
- `CalendarConversions.kt` untouched (Birashk 2820-year Jalali + tabular Hijri with `+1` offset)
- Settings gear, local-first no network.

## Stack
- Kotlin **2.0.21**, Compose Multiplatform **1.7.3**, AGP **8.7.3**, Material3 (JetBrains `compose.material3` stable)
- Gradle 8.10.2, Kotlin DSL, `kotlin.code.style=official`
- Package `app.afghancalendar`, `minSdk 26`, `targetSdk 35`
- Desktop 420×860 dp window, Android phone UI — same `commonMain` code.

> **Expressive upgrade path** (without breaking stable build): keep current stable and fake expressive via tonal palettes + shapes + `AppTypography` (as done here). When you move to Kotlin `2.2.20` + CMP `1.9.3` + `org.jetbrains.compose.material3:material3:1.9.0-alpha04` (or `androidx.compose.material3:material3:1.5.0-alpha27` on Android), you can enable:
> ```kotlin
> @OptIn(ExperimentalMaterial3ExpressiveApi::class)
> MaterialExpressiveTheme(colorScheme=scheme, motionScheme=MotionScheme.expressive(), shapes=expressiveShapes) { }
> ```
> See `M3_EXPRESSIVE_REPORT.md` for full version map.

## Project Structure
```
settings.gradle.kts
build.gradle.kts
gradle/wrapper/
composeApp/
  build.gradle.kts
  src/commonMain/kotlin/app/afghancalendar/
    App.kt                  # Custom Scaffold + 2 ElevatedCards + RTL grid
    CalendarConversions.kt  # Gregorian↔JDN, Jalali↔JDN (Birashk), Hijri tabular +1
    Theme.kt                # 5 seed light/dark schemes, AppShapes, ThemeMode
    Type.kt                 # AppTypography (+ Vazirmatn bundle note)
    Platform.kt
  src/commonMain/composeResources/font/
    Vazirmatn-Regular.ttf   # 102 KB, from fonts.gstatic.com
    Vazirmatn-Medium.ttf
    Vazirmatn-Bold.ttf
  src/androidMain/kotlin/app/afghancalendar/
    MainActivity.kt
    PlatformAndroid.kt
  src/desktopMain/kotlin/app/afghancalendar/
    Main.kt                 # 420×860 dp Window
    PlatformDesktop.kt
```

## Calendar Conversions
`CalendarConversions.kt`:
- **Gregorian ↔ JDN** standard
- **Jalali ↔ JDN** 2820-year cycle (`jalaliToJdn` / `jdnToJalali`) — matches `jalaali-js`
- **Hijri tabular civil** with `HIJRI_OFFSET = 1` to align `2026-08-28 → 1448/03/16`
- Persian digits `۰۱۲۳۴۵۶۷۸۹`, Dari months `حمل…حوت`, Hijri `محرم…ذوالحجة`
- Weekday `weekday = (jdn + 1) % 7` where `0=Sunday` — bottom weekday uses same index.
- Week start toggle: `weekStartSaturday = true` (Afghan convention, default) maps `firstOffset = (firstWeekday - 6 +7)%7`; when `false` uses Sunday-start `offset = firstWeekday` (as in photo). Header arrays:
  - Saturday: `شنبه، یکشنبه، دوشنبه، سه‌شنبه، چهارشنبه، پنجشنبه، جمعه`
  - Sunday: `یکشنبه، دوشنبه، سه‌شنبه، چهارشنبه، پنجشنبه، جمعه، شنبه`

## How to Run

### Prerequisites
- JDK 17+ (`java -version`)
- Android SDK with `ANDROID_HOME` or `sdk.dir` in `local.properties` (for Android)
- No internet required after initial Gradle sync; fonts already bundled.

### Desktop (JVM)
```bash
./gradlew :composeApp:desktopJar
java -jar composeApp/build/libs/composeApp-desktop.jar

# or run directly (opens window):
./gradlew :composeApp:run
# or dev distributable:
./gradlew :composeApp:createDistributable
# → composeApp/build/compose/binaries/main/app/

# headless / no GL:
SKIKO_RENDER_API=SOFTWARE ./gradlew :composeApp:run
```

### Android
```bash
./gradlew :composeApp:assembleDebug
# APK: composeApp/build/outputs/apk/debug/composeApp-debug.apk
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Quick Check
```bash
./gradlew :composeApp:compileKotlinDesktop
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:desktopJar
./gradlew :composeApp:createDistributable
```

## UI Details (Custom Tonal Cards)
```
MaterialTheme(colorScheme=seedScheme, typography=AppTypography, shapes=AppShapes) {
  Scaffold(containerColor=surface, topBar=CenterAlignedTopAppBar("Afghan Calendar", nav=CalendarMonth, actions=Settings))
  Box(centered, maxWidth 480dp, padding 16.dp, gap 16.dp, verticalScroll) {
    ElevatedCard(shape=32.dp, container=primaryContainer) {
      Row(FilledTonalIconButton ← , headline monthYear, →)
      Row(RTL, labelMedium) weekdayHeader (7 labels, weekStart-aware)
      Column 6×7 grid (RTL): selected = surface CircleShape + onSurface bold, today = dot, others = onPrimaryContainer
    }
    ElevatedCard(shape=28.dp, container=surfaceContainer) {
      Text(headlineMedium, centered Persian weekday e.g. جمعه)
      Text(labelSmall, 3-date summary •)
      Divider
      Row(RTL, 3 columns) {
        Solar:     ۰۶ سنبله + ۱۴۰۵ + AssistChip ۱۴۰۵/۰۶/۰۶
        Gregorian: Aug 28 + 2026 + AssistChip 2026/08/28
        Hijri:     ۱۶ ربیع‌الاول + ۱۴۴۸ + AssistChip ۱۴۴۸/۰۳/۱۶
      }
    }
    Text(labelSmall, verification footer)
  }
}
```

- Settings dialog (`AlertDialog` shape 28dp, `surfaceContainerHigh`):
  - **Color swatches** 56.dp `CircleShape` with `Check` for selected, 5 palettes
  - **SegmentedButtonRow** Light / System / Dark
  - **Switch** for week start (Saturday Afghan default vs Sunday photo)
  - Explanatory `bodySmall` note

## Fonts
- Modern sans-serif via `AppTypography()` ( `FontFamily.SansSerif` )
- Vazirmatn TTFs in `composeResources/font/` ( Regular / Medium / Bold ). To activate Res.font wiring, see comment in `Type.kt`. Fallback to system sans documented and build-safe.

## Build Verified
```bash
./gradlew :composeApp:assembleDebug        # BUILD SUCCESSFUL
./gradlew :composeApp:desktopJar           # BUILD SUCCESSFUL
./gradlew :composeApp:createDistributable  # BUILD SUCCESSFUL → binaries/main/app
```
- Tested conversions: `jalaliToJdn(1405,6,6) == gregorianToJdn(2026,8,28) == 2461281`, `jdnToHijri → 1448/03/16`, `weekday 5 = جمعه (Friday)`.

## Notes
- Local-first, no backend, no accounts, no events, no network.
- RTL only for grid/header/detail row, not whole scaffold.
- ThemeMode & seedIndex & weekStart persisted via `rememberSaveable`.
- Desktop `Main.kt` window 420×860 dp mimics phone; Android `MainActivity` wraps `App()` in `Surface`.
