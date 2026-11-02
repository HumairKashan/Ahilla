# Ahilla — DESIGN-compose.md

How to build `DESIGN.md` in Compose Multiplatform (commonMain, Android + iOS). Version 1.2 · 26 Oct 2026 (matches DESIGN.md 1.2).

**Status of the code below:** written without compiling (no Android/KMP toolchain was available when this was drafted). Treat names, structure and values as the spec; fix imports and signatures when you build. If a value here disagrees with `DESIGN.md`, `DESIGN.md` wins: change this file, not the design.

**Changes in 1.2 (26 Oct 2026):** new `StrapworkRosette` (DESIGN §6.1b) replaces `CornerFragment` on the Today header (delete `CornerFragment.kt`) and is the centre of the new `PatternPanel` (§6.4); `Rosette` (nested stars) stays for widgets only; `RosetteMedallion` is removed. New components: `CheckboxRow`, `Stepper`, `ExpectedRow`, `AmountRow` and `ShareCard`: specs in DESIGN §8.23–8.28, built from the existing `ListCard` and `FormCard` rows (no new code sections); the moon-sighting notification and announcement logic are in section 13. New screens are composed from existing components (DESIGN §9.10–9.18).

**Changes in 1.1:** added `scrim`, `numeralM`, `arabicXS`; new twelve-fold `Rosette` and `RosetteMedallion` (the old eight-fold one is now `CornerFragment`, Today header only); `PatternPanel`; components for list cards, sheets, segmented toggle, wheel picker, tonal button and status chip (section 8.8 onward).

## 1. Ground rules

- All UI lives in `commonMain`. Platform code is limited to the two `expect/actual` pairs in section 6, widgets (section 10), and anything the OS forces.
- Screens read tokens only through `Ahilla.colors`, `Ahilla.type`, `Ahilla.dims`, `Ahilla.motion`. **No `Color(0x…)`, no `.sp` or `.dp` literals in screen code** (components may use the dp values from `DESIGN.md` §4 and §8 as named constants in `AhillaDims`).
- **Material 3 is a source of primitives only** (switch, text field, ripple, scaffold plumbing). Never let its default colours, typography or shapes show. Everything is restyled through `AhillaTheme`.
- Use `start`/`end`, never `left`/`right`. Use `stringResource`; no hard-coded user-facing strings.
- No shadows: never pass an elevation above `0.dp`.

## 2. File layout

```
composeApp/src/
  commonMain/
    composeResources/font/
      cormorant_garamond_medium.ttf
      cormorant_garamond_semibold.ttf
      cormorant_garamond_medium_italic.ttf
      ibm_plex_sans_regular.ttf
      ibm_plex_sans_medium.ttf
      ibm_plex_sans_semibold.ttf
      ibm_plex_mono_medium.ttf
      amiri_regular.ttf
    kotlin/.../ui/
      theme/   Color.kt  Type.kt  Dims.kt  Motion.kt  Theme.kt
      components/  MarkerDot.kt  DayCell.kt  FormCard.kt  SwitchRow.kt  RepeatOption.kt
                   AhillaFab.kt  AhillaBottomBar.kt  StatusChip.kt  TonalButton.kt
                   ListCard.kt  SegmentedToggle.kt  WheelPicker.kt  AhillaSheet.kt  SectionHeading.kt
      moments/  Rosette.kt  StrapworkRosette.kt  PatternPanel.kt  Starfield.kt  TypewriterEyebrow.kt
      share/    ShareCard.kt  (the four card layouts, rendered to a bitmap for sharing)
      components also: CheckboxRow.kt  Stepper.kt  ExpectedRow.kt  AmountRow.kt
  androidMain/kotlin/.../ui/theme/Motion.android.kt
  iosMain/kotlin/.../ui/theme/Motion.ios.kt
licences/  OFL texts for the four font families
```

Font files: download the static instances from Google Fonts (Cormorant Garamond, IBM Plex Sans, IBM Plex Mono, Amiri). Keep their OFL licence files in `licences/` and show them in Settings › Privacy & about. Resource names must be lowercase letters, digits and underscores.

## 3. Colour tokens — `Color.kt`

```kotlin
@Immutable
data class AhillaColors(
    val canvas: Color,
    val surface: Color,
    val line: Color,
    val ink: Color,
    val inkMuted: Color,
    val accent: Color,
    val accentWash: Color,
    val event: Color,
    val onAccent: Color,
    /** Dim layer behind sheets; the only token with alpha. */
    val scrim: Color,
    val isDark: Boolean,
)

val PaperColors = AhillaColors(
    canvas = Color(0xFFF7F1E3),
    surface = Color(0xFFFCF8EF),
    line = Color(0xFFE2D8C3),
    ink = Color(0xFF1F1B16),
    inkMuted = Color(0xFF6B6256),
    accent = Color(0xFF1F6F6B),
    accentWash = Color(0xFFE2ECE7),
    event = Color(0xFF7E6026),
    onAccent = Color(0xFFF7F1E3), // = canvas
    scrim = Color(0x731F1B16),    // ink at 45%
    isDark = false,
)

val NightColors = AhillaColors(
    canvas = Color(0xFF16171A),
    surface = Color(0xFF1F2024),
    line = Color(0xFF2F3136),
    ink = Color(0xFFECE7DD),
    inkMuted = Color(0xFFA39C90),
    accent = Color(0xFF5FBFB5),
    accentWash = Color(0xFF1D2D2C),
    event = Color(0xFFD4B26A),
    onAccent = Color(0xFF16171A), // = canvas
    scrim = Color(0x9E08090A),    // near-black at 62%
    isDark = true,
)

val LocalAhillaColors = staticCompositionLocalOf { PaperColors }
```

## 4. Typography — `Type.kt`

Fonts are Compose resources, so the families must be built inside a composable.

```kotlin
@Immutable
class AhillaType(
    // Cormorant Garamond
    val hero: TextStyle, val display: TextStyle, val numeralL: TextStyle, val numeralM: TextStyle,
    val cardTitle: TextStyle,
    val screenTitle: TextStyle, val sheetTitle: TextStyle, val dateValue: TextStyle, val numeral: TextStyle,
    val monthItalic: TextStyle,
    // IBM Plex Sans
    val heading: TextStyle, val titleS: TextStyle, val fieldText: TextStyle, val body: TextStyle,
    val bodyM: TextStyle, val label: TextStyle, val caption: TextStyle, val small: TextStyle,
    val micro: TextStyle, val weekday: TextStyle,
    // IBM Plex Mono (small caps labels: moments, Islamic day header and labels, converter result label)
    val eyebrow: TextStyle, val eyebrowS: TextStyle, val tag: TextStyle,
    // Amiri
    val arabic: TextStyle, val arabicS: TextStyle, val arabicXS: TextStyle,
)

@Composable
fun rememberAhillaType(): AhillaType {
    val cormorant = FontFamily(
        Font(Res.font.cormorant_garamond_medium, FontWeight.Medium),
        Font(Res.font.cormorant_garamond_semibold, FontWeight.SemiBold),
        Font(Res.font.cormorant_garamond_medium_italic, FontWeight.Medium, FontStyle.Italic),
    )
    val plex = FontFamily(
        Font(Res.font.ibm_plex_sans_regular, FontWeight.Normal),
        Font(Res.font.ibm_plex_sans_medium, FontWeight.Medium),
        Font(Res.font.ibm_plex_sans_semibold, FontWeight.SemiBold),
    )
    val mono = FontFamily(Font(Res.font.ibm_plex_mono_medium, FontWeight.Medium))
    val amiri = FontFamily(Font(Res.font.amiri_regular, FontWeight.Normal))

    return remember(cormorant, plex, mono, amiri) {
        fun s(
            family: FontFamily, size: Int, line: Int, weight: FontWeight,
            tracking: Float = 0f, tabular: Boolean = false, italic: Boolean = false,
        ) = TextStyle(
            fontFamily = family,
            fontSize = size.sp,
            lineHeight = line.sp,
            fontWeight = weight,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            letterSpacing = tracking.em,
            fontFeatureSettings = if (tabular) "tnum" else null,
        )
        val M = FontWeight.Medium; val N = FontWeight.Normal; val SB = FontWeight.SemiBold
        AhillaType(
            hero = s(cormorant, 112, 92, M), display = s(cormorant, 56, 60, M),
            numeralL = s(cormorant, 64, 56, M, tabular = true), numeralM = s(cormorant, 30, 30, M, tabular = true),
            cardTitle = s(cormorant, 30, 34, M),
            screenTitle = s(cormorant, 34, 38, M), sheetTitle = s(cormorant, 32, 38, M),
            dateValue = s(cormorant, 22, 26, M), numeral = s(cormorant, 22, 24, M, tabular = true),
            monthItalic = s(cormorant, 36, 36, M, italic = true),
            heading = s(plex, 20, 28, SB), titleS = s(plex, 17, 24, SB), fieldText = s(plex, 20, 28, M),
            body = s(plex, 16, 24, N), bodyM = s(plex, 15, 22, M),
            label = s(plex, 14, 20, M), caption = s(plex, 13, 18, N), small = s(plex, 12, 16, N),
            micro = s(plex, 11, 13, N, tabular = true), weekday = s(plex, 11, 16, M, tracking = 0.04f),
            eyebrow = s(mono, 11, 16, M, tracking = 0.2f), eyebrowS = s(mono, 10, 14, M, tracking = 0.2f),
            tag = s(mono, 10, 14, M, tracking = 0.14f),
            arabic = s(amiri, 28, 44, N), arabicS = s(amiri, 26, 40, N), arabicXS = s(amiri, 20, 32, N),
        )
    }
}
```

Usage notes: weights vary at the call site with `.copy(fontWeight = …)` (e.g. `numeral` is SemiBold for today, `bodyM` is SemiBold for event card titles, `small` is Medium for nav labels and SemiBold when selected). Only the bundled weights exist, so never ask for another (no Cormorant 400, no Plex Mono 400).

Month-grid font-scale cap (DESIGN §10):

```kotlin
@Composable
fun CappedFontScale(max: Float = 1.3f, content: @Composable () -> Unit) {
    val d = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(d.density, fontScale = d.fontScale.coerceAtMost(max)),
        content = content,
    )
}
```

## 5. Dimensions and shapes — `Dims.kt`

```kotlin
object AhillaDims {
    // spacing
    val s2 = 2.dp; val s4 = 4.dp; val s6 = 6.dp; val s8 = 8.dp; val s10 = 10.dp; val s12 = 12.dp
    val s14 = 14.dp; val s16 = 16.dp; val s18 = 18.dp; val s20 = 20.dp; val s24 = 24.dp
    val s28 = 28.dp; val s32 = 32.dp; val s40 = 40.dp; val s48 = 48.dp
    // insets
    val inset = 16.dp; val textInset = 20.dp; val gridInset = 12.dp
    // radii
    val rCell = 14.dp; val rCard = 16.dp; val rForm = 20.dp; val rPreview = 24.dp
    val rFab = 18.dp; val rWidget = 28.dp; val rMoment = 36.dp; val rSheet = 28.dp
    val rPill = 22.dp; val rPillWide = 24.dp; val rToggle = 22.dp; val rToggleSeg = 19.dp; val rWheelBand = 12.dp
    // sizes
    val minTouch = 48.dp; val iconButton = 44.dp; val icon = 22.dp; val iconInline = 14.dp
    val cellHeight = 58.dp; val cellMinWidth = 52.dp; val dot = 5.dp; val dotLegend = 6.dp; val dotRow = 7.dp
    val navHeight = 80.dp; val navIndicatorW = 56.dp; val navIndicatorH = 30.dp
    val fab = 56.dp; val hairline = 1.dp
    val wheelRow = 40.dp; val wheelHeight = 200.dp; val sheetHandleW = 36.dp; val sheetHandleH = 4.dp
    val toggleHeight = 44.dp; val tonalHeight = 52.dp; val pillWideHeight = 48.dp; val patternPanelHeight = 300.dp
}
```

## 6. Motion and reduce motion — `Motion.kt`

```kotlin
@Immutable
class AhillaMotion(val reduceMotion: Boolean)
val LocalAhillaMotion = staticCompositionLocalOf { AhillaMotion(reduceMotion = false) }

@Composable
expect fun rememberReduceMotion(): Boolean
```

```kotlin
// androidMain — "Remove animations" in system settings sets the animator scale to 0
@Composable
actual fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}
```

```kotlin
// iosMain
@Composable
actual fun rememberReduceMotion(): Boolean = remember { UIAccessibilityIsReduceMotionEnabled() }
```

Known limit: both read the setting once per composition and do not observe changes while the app is open. That is acceptable for v1; re-read it on resume if it becomes an issue.

## 7. Theme — `Theme.kt`

```kotlin
enum class ThemeMode { System, Paper, Night }

object Ahilla {
    val colors: AhillaColors @Composable @ReadOnlyComposable get() = LocalAhillaColors.current
    val type: AhillaType @Composable @ReadOnlyComposable get() = LocalAhillaType.current
    val motion: AhillaMotion @Composable @ReadOnlyComposable get() = LocalAhillaMotion.current
    val dims = AhillaDims
}

val LocalAhillaType = staticCompositionLocalOf<AhillaType> { error("AhillaTheme missing") }

@Composable
fun AhillaTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Paper -> false
        ThemeMode.Night -> true
    }
    val colors = if (dark) NightColors else PaperColors
    val type = rememberAhillaType()
    val motion = AhillaMotion(rememberReduceMotion())

    CompositionLocalProvider(
        LocalAhillaColors provides colors,
        LocalAhillaType provides type,
        LocalAhillaMotion provides motion,
    ) {
        MaterialTheme(colorScheme = colors.toMaterial(), content = content)
    }
}

// Bridge so Material primitives never show their defaults.
private fun AhillaColors.toMaterial(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent, onPrimary = onAccent,
        background = canvas, onBackground = ink,
        surface = surface, onSurface = ink,
        surfaceVariant = surface, onSurfaceVariant = inkMuted,
        outline = line, outlineVariant = line,
        primaryContainer = accentWash, onPrimaryContainer = accent,
    )
}
```

The root of every screen paints `Ahilla.colors.canvas` as its background explicitly (do not rely on the Material scaffold default). Persist the user's `ThemeMode` in local settings (Settings › Appearance).

## 8. Components

### 8.1 Marker dot — `MarkerDot.kt`

```kotlin
enum class DotKind { Islamic, AwaitsSighting, Mine }

/** Always reserves 5 dp so rows align, even when kind is null. */
@Composable
fun MarkerDot(kind: DotKind?, modifier: Modifier = Modifier) {
    val c = Ahilla.colors
    val base = modifier.size(Ahilla.dims.dot)
    when (kind) {
        null -> Box(base)
        DotKind.Islamic -> Box(base.background(c.event, CircleShape))
        DotKind.AwaitsSighting -> Box(base.border(1.2.dp, c.event, CircleShape))
        DotKind.Mine -> Box(base.background(c.accent, CircleShape))
    }
}
```

### 8.2 Month day cell — `DayCell.kt`

```kotlin
@Immutable
data class DayCellModel(
    val hijriDay: Int,
    val gregorianLabel: String,   // "25" or "1 Mar" on a month change
    val isToday: Boolean,
    val inHijriMonth: Boolean,
    val marker: DotKind?,
    val a11yDescription: String,  // built by the date engine layer, see DESIGN §10
)

@Composable
fun DayCell(
    model: DayCellModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Ahilla.colors
    val t = Ahilla.type
    val shape = RoundedCornerShape(Ahilla.dims.rCell)

    val numeralColor = when {
        model.isToday -> c.onAccent
        !model.inHijriMonth -> c.inkMuted
        else -> c.ink
    }
    val figureColor = if (model.isToday) c.onAccent else c.inkMuted

    Column(
        modifier = modifier
            .height(Ahilla.dims.cellHeight)
            .clip(shape)
            .background(if (model.isToday) c.accent else Color.Transparent)
            // proposed (not mocked up): selected non-today day gets a ring
            .then(if (selected && !model.isToday) Modifier.border(1.5.dp, c.accent, shape) else Modifier)
            .semantics(mergeDescendants = true) {
                contentDescription = model.a11yDescription
                role = Role.Button
            }
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = model.hijriDay.toString(),
            style = t.numeral.copy(
                fontWeight = if (model.isToday) FontWeight.SemiBold else FontWeight.Medium,
            ),
            color = numeralColor,
        )
        Text(
            text = model.gregorianLabel,
            style = t.micro.copy(fontWeight = if (model.isToday) FontWeight.Medium else FontWeight.Normal),
            color = figureColor,
        )
        MarkerDot(model.marker)
    }
}
```

The grid wraps its cells in `CappedFontScale(1.3f)`. The selected-day preview card below it does not.

### 8.3 Form card and rows — `FormCard.kt`, `SwitchRow.kt`

```kotlin
@Composable
fun FormCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val c = Ahilla.colors
    val shape = RoundedCornerShape(Ahilla.dims.rForm)
    Column(
        modifier = modifier.fillMaxWidth().clip(shape).background(c.surface)
            .border(Ahilla.dims.hairline, c.line, shape),
        content = content,
    )
}

/** Material 3's switch is 52×32 with a 24 dp thumb when on, which matches DESIGN §8.12. */
@Composable
fun SwitchRow(
    label: String, helper: String?, checked: Boolean, onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Ahilla.colors; val t = Ahilla.type
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 56.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = Ahilla.dims.s18),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = t.label, color = c.ink)
            if (helper != null) Text(helper, style = t.small, color = c.inkMuted)
        }
        Switch(
            checked = checked, onCheckedChange = null, // the row handles the click
            colors = SwitchDefaults.colors(
                checkedTrackColor = c.accent, checkedThumbColor = c.onAccent, checkedBorderColor = c.accent,
                uncheckedTrackColor = Color.Transparent, uncheckedThumbColor = c.inkMuted,
                uncheckedBorderColor = c.inkMuted,
            ),
        )
    }
}
```

### 8.4 Repeat option — `RepeatOption.kt`

```kotlin
@Composable
fun RepeatOptionRow(
    title: String, detail: String?, selected: Boolean, onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = Ahilla.colors; val t = Ahilla.type
    Row(
        modifier = modifier.fillMaxWidth()
            .heightIn(min = if (detail != null) 52.dp else 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.size(20.dp).clip(CircleShape).background(if (selected) c.canvas else Color.Transparent)
                .border(if (selected) 6.dp else 1.5.dp, if (selected) c.accent else c.inkMuted, CircleShape),
        )
        Column {
            Text(
                title,
                style = if (selected) t.bodyM.copy(fontWeight = FontWeight.SemiBold) else t.body,
                color = if (selected) c.ink else c.inkMuted,
            )
            if (detail != null) Text(detail, style = t.small, color = c.inkMuted)
        }
    }
}
```

### 8.5 Floating action button — `AhillaFab.kt`

```kotlin
@Composable
fun AhillaFab(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Ahilla.colors
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(Ahilla.dims.rFab),
        containerColor = c.accent,
        contentColor = c.onAccent,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
    ) { AhillaIcon(AhillaIcons.Plus, strokeWidth = 1.8.dp) }
}
```

Position: `Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp)` above the bottom bar.

### 8.6 Bottom bar — `AhillaBottomBar.kt`

Build it custom (Material's `NavigationBar` indicator is 64×32, ours is 56×30):

```kotlin
enum class Tab { Today, Calendar, Mine }

@Composable
fun AhillaBottomBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = Ahilla.colors; val t = Ahilla.type
    Column(modifier.fillMaxWidth().background(c.canvas)) {
        Box(Modifier.fillMaxWidth().height(Ahilla.dims.hairline).background(c.line))
        Row(Modifier.fillMaxWidth().height(Ahilla.dims.navHeight - Ahilla.dims.hairline)) {
            Tab.entries.forEach { tab ->
                val on = tab == selected
                Column(
                    Modifier.weight(1f).fillMaxHeight()
                        .selectable(selected = on, role = Role.Tab, onClick = { onSelect(tab) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                ) {
                    Box(
                        Modifier.size(Ahilla.dims.navIndicatorW, Ahilla.dims.navIndicatorH)
                            .clip(RoundedCornerShape(15.dp))
                            .background(if (on) c.accentWash else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) { AhillaIcon(tab.icon, tint = if (on) c.accent else c.inkMuted) }
                    Text(
                        stringResource(tab.label),
                        style = t.small.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium),
                        color = if (on) c.accent else c.inkMuted,
                    )
                }
            }
        }
    }
}
```

Handle system-bar insets by padding the bar's bottom with `WindowInsets.navigationBars` so the 80 dp bar content is not covered.

### 8.7 Icons

Draw the monoline icons as `ImageVector`s or Compose resources (SVG → vector drawable) on a 24 × 24 grid, stroke 1.6 dp (1.8 for FAB and chips), round caps and joins, no fill. Tint with `LocalContentColor` or an explicit token. Wrap in `AhillaIcon(icon, tint, strokeWidth)` so every icon is drawn the same way. Give icon-only buttons a `contentDescription`.

### 8.8 Status chip and tonal button — `StatusChip.kt`, `TonalButton.kt`

```kotlin
/** DESIGN §8.15. dot = null for a plain chip. */
@Composable
fun StatusChip(text: String, dot: DotKind?, modifier: Modifier = Modifier) {
    val c = Ahilla.colors; val t = Ahilla.type
    Row(
        modifier.clip(CircleShape).background(c.surface).border(Ahilla.dims.hairline, c.line, CircleShape)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (dot != null) Box(
            Modifier.size(Ahilla.dims.dotRow).then(
                if (dot == DotKind.AwaitsSighting) Modifier.border(1.2.dp, c.event, CircleShape)
                else Modifier.background(if (dot == DotKind.Mine) c.accent else c.event, CircleShape)
            )
        )
        Text(text, style = t.small.copy(fontWeight = FontWeight.Medium), color = c.ink)
    }
}

/** DESIGN §8.14: full-width tonal button, 52 dp, radius 18. The only secondary filled button. */
@Composable
fun TonalButton(text: String, icon: (@Composable () -> Unit)?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = Ahilla.colors; val t = Ahilla.type
    val shape = RoundedCornerShape(Ahilla.dims.rFab)
    Row(
        modifier.fillMaxWidth().height(Ahilla.dims.tonalHeight).clip(shape).background(c.accentWash)
            .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) { icon(); Spacer(Modifier.width(10.dp)) }
        Text(text, style = t.bodyM.copy(fontWeight = FontWeight.SemiBold), color = c.accent)
    }
}
```

The full-width primary pill ("Go to this date") is the same pattern with an `accent` fill, `onAccent` text, height `pillWideHeight` and radius `rPillWide`.

### 8.9 List card and row — `ListCard.kt`, `SectionHeading.kt`

```kotlin
/** DESIGN §8.18. Same frame as FormCard (radius 20, 1 dp line, no padding). */
@Composable
fun ListCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) = FormCard(modifier, content)

@Composable
fun ListRow(
    title: String,
    dot: DotKind?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleIcon: (@Composable () -> Unit)? = null,   // 14 dp repeat icon
    divider: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = Ahilla.colors; val t = Ahilla.type
    Column(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = if (subtitle != null) 64.dp else 56.dp)
                .padding(horizontal = Ahilla.dims.s18, vertical = Ahilla.dims.s8),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Rows use a 7 dp dot (dotRow); the month grid uses 5 dp. Give MarkerDot a size parameter when building.
            MarkerDot(dot)
            Column(Modifier.weight(1f)) {
                Text(title, style = t.bodyM, color = c.ink)
                if (subtitle != null) Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subtitleIcon != null) { subtitleIcon(); Spacer(Modifier.width(6.dp)) }
                    Text(subtitle, style = t.caption, color = c.inkMuted)
                }
            }
            trailing?.invoke()
        }
        if (divider) HorizontalDivider(thickness = Ahilla.dims.hairline, color = c.line)
    }
}

/** Two stacked caption lines, end-aligned ("Sun 28 Feb" / "in 3 days"). */
@Composable
fun TrailingTwoLines(first: String, second: String) {
    val c = Ahilla.colors; val t = Ahilla.type
    Column(horizontalAlignment = Alignment.End) {
        Text(first, style = t.caption, color = c.inkMuted)
        Text(second, style = t.caption, color = c.inkMuted)
    }
}

/** Day count for Mine countdown rows: numeralM + "days". */
@Composable
fun TrailingDays(days: Int, daysLabel: String) {
    val c = Ahilla.colors; val t = Ahilla.type
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Text("$days", style = t.numeralM, color = c.ink)
        Text(daysLabel, style = t.caption, color = c.inkMuted, modifier = Modifier.padding(bottom = 3.dp))
    }
}

/** DESIGN §8.22. */
@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier, start: Dp = 20.dp) {
    Text(
        text, style = Ahilla.type.label, color = Ahilla.colors.inkMuted,
        modifier = modifier.padding(start = start, top = 24.dp, bottom = 10.dp),
    )
}
```

The last row of a card passes `divider = false`.

### 8.10 Bottom sheet — `AhillaSheet.kt`

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AhillaSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val c = Ahilla.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = Ahilla.dims.rSheet, topEnd = Ahilla.dims.rSheet),
        containerColor = c.canvas,
        scrimColor = c.scrim,
        tonalElevation = 0.dp,
        dragHandle = {
            Box(
                Modifier.padding(top = 10.dp, bottom = 2.dp)
                    .size(Ahilla.dims.sheetHandleW, Ahilla.dims.sheetHandleH)
                    .background(c.inkMuted.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
            )
        },
        content = { Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp), content = content) },
    )
}
```

DESIGN §8.19 also asks for a 1 dp `line` top border; Material's sheet does not draw one, so add it with `Modifier.drawBehind { drawLine(c.line, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }` on the sheet content. Under reduce motion, replace the slide with a fade.

### 8.11 Segmented toggle — `SegmentedToggle.kt`

```kotlin
@Composable
fun SegmentedToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val c = Ahilla.colors; val t = Ahilla.type
    val outer = RoundedCornerShape(Ahilla.dims.rToggle)
    Row(
        modifier.fillMaxWidth().height(Ahilla.dims.toggleHeight).clip(outer).background(c.surface)
            .border(Ahilla.dims.hairline, c.line, outer).padding(3.dp).selectableGroup(),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(Ahilla.dims.rToggleSeg))
                    .background(if (on) c.accentWash else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(i) }),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = t.label.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium),
                    color = if (on) c.accent else c.inkMuted,
                )
            }
        }
    }
}
```

### 8.12 Wheel picker — `WheelPicker.kt`

One column, then the framed picker with the selection band behind three columns (DESIGN §8.21).

```kotlin
@Composable
fun WheelColumn(
    items: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit,
    label: String, modifier: Modifier = Modifier,
) {
    val c = Ahilla.colors; val t = Ahilla.type
    val rowH = Ahilla.dims.wheelRow
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(state)
    // With two rows of padding above and below, the first visible item is the centred one once the list has snapped.
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress to state.firstVisibleItemIndex }
            .filter { (scrolling, _) -> !scrolling }
            .collect { (_, index) -> if (index != selectedIndex) onSelected(index) }
    }
    LazyColumn(
        state = state, flingBehavior = fling,
        contentPadding = PaddingValues(vertical = rowH * 2),
        modifier = modifier.fillMaxHeight().semantics {
            contentDescription = label
            stateDescription = items[selectedIndex]
            customActions = listOf(
                CustomAccessibilityAction("Next") { if (selectedIndex < items.lastIndex) { onSelected(selectedIndex + 1); true } else false },
                CustomAccessibilityAction("Previous") { if (selectedIndex > 0) { onSelected(selectedIndex - 1); true } else false },
            )
        },
    ) {
        itemsIndexed(items) { i, text ->
            val (style, color) = when (abs(i - selectedIndex)) {
                0 -> t.dateValue.copy(fontSize = 26.sp, lineHeight = 40.sp) to c.ink
                1 -> t.dateValue.copy(fontSize = 21.sp, lineHeight = 40.sp) to c.inkMuted
                else -> t.dateValue.copy(fontSize = 18.sp, lineHeight = 40.sp) to c.inkMuted.copy(alpha = 0.5f)
            }
            Box(Modifier.height(rowH).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text, style = style, color = color)
            }
        }
    }
}

@Composable
fun DateWheels(
    days: List<String>, day: Int, onDay: (Int) -> Unit,
    months: List<String>, month: Int, onMonth: (Int) -> Unit,
    years: List<String>, year: Int, onYear: (Int) -> Unit,
    dayLabel: String, monthLabel: String, yearLabel: String,
) {
    val c = Ahilla.colors
    val shape = RoundedCornerShape(Ahilla.dims.rForm)
    Box(
        Modifier.fillMaxWidth().height(Ahilla.dims.wheelHeight).clip(shape).background(c.surface)
            .border(Ahilla.dims.hairline, c.line, shape)
    ) {
        Box(
            Modifier.padding(horizontal = 8.dp).fillMaxWidth().height(Ahilla.dims.wheelRow)
                .align(Alignment.Center)
                .background(c.accentWash, RoundedCornerShape(Ahilla.dims.rWheelBand))
        )
        Row(Modifier.fillMaxSize()) {
            WheelColumn(days, day, onDay, dayLabel, Modifier.weight(0.8f))
            WheelColumn(months, month, onMonth, monthLabel, Modifier.weight(1.7f))
            WheelColumn(years, year, onYear, yearLabel, Modifier.weight(1f))
        }
    }
}
```

The month length (29 or 30 for Hijri) comes from the date engine: when the month or year changes, rebuild `days` and clamp `day`. Fire the platform's light haptic when `selectedIndex` changes (`LocalHapticFeedback`).

## 9. Moments — `moments/`

Moments only (Today header, countdown cards, share cards, onboarding). Exceptions: widgets carry the nested-star `Rosette` (as a static vector, section 10) and the Mine empty state carries `PatternPanel`. Never in the grid, forms, settings, the Islamic day screen or the Jump to date sheet.

### 9.1 Rosette (nested stars, widgets only) — `Rosette.kt`

Geometry is in DESIGN §6.1; every radius is a fraction of S. Angles run clockwise from straight up.

```kotlin
enum class Corner { TopStart, TopEnd, BottomStart, BottomEnd }

internal fun DrawScope.drawNestedStars(centre: Offset, s: Float, color: Color, stroke: Stroke) {
    fun polar(r: Float, deg: Float): Offset {
        val t = (deg - 90f) * (PI.toFloat() / 180f)
        return Offset(centre.x + r * s * cos(t), centre.y + r * s * sin(t))
    }
    fun star(points: Float, valleys: Float) = Path().apply {
        for (i in 0 until 12) {
            val a = polar(points, 30f * i + 15f); val b = polar(valleys, 30f * i + 30f)
            if (i == 0) moveTo(a.x, a.y) else lineTo(a.x, a.y)
            lineTo(b.x, b.y)
        }
        close()
    }
    drawPath(star(0.853f, 0.680f), color, style = stroke)
    drawPath(star(0.733f, 0.587f), color, style = stroke)
}

internal fun DrawScope.drawRosette(centre: Offset, s: Float, color: Color, strokeWidth: Float) {
    val stroke = Stroke(width = strokeWidth)
    fun polar(r: Float, deg: Float): Offset {
        val t = (deg - 90f) * (PI.toFloat() / 180f)
        return Offset(centre.x + r * s * cos(t), centre.y + r * s * sin(t))
    }
    drawCircle(color, radius = 0.907f * s, center = centre, style = stroke)
    drawNestedStars(centre, s, color, stroke)
    for (i in 0 until 12) {
        val d = 30f * i
        val kite = Path().apply {
            val p = listOf(polar(0.147f, d), polar(0.320f, d + 15f), polar(0.533f, d), polar(0.320f, d - 15f))
            moveTo(p[0].x, p[0].y); for (k in 1 until p.size) lineTo(p[k].x, p[k].y); close()
        }
        drawPath(kite, color, style = stroke)
    }
    drawCircle(color, radius = 0.073f * s, center = centre, style = stroke)
}

/** Corner crop for widgets: the box is S, the centre sits on the chosen corner, the box clips the rest. */
@Composable
fun Rosette(modifier: Modifier = Modifier, corner: Corner = Corner.TopEnd, strength: Float = 0.3f) {
    val color = Ahilla.colors.ink.copy(alpha = strength)
    Canvas(modifier.clipToBounds().clearAndSetSemantics { }) {
        val s = size.minDimension
        val atEnd = corner == Corner.TopEnd || corner == Corner.BottomEnd
        val atTop = corner == Corner.TopStart || corner == Corner.TopEnd
        val centre = Offset(if (atEnd) size.width else 0f, if (atTop) 0f else size.height)
        drawRosette(centre, s, color, strokeWidth = 1.2.dp.toPx())
    }
}
```

`RosetteMedallion` was removed in 1.2. Strength: 0.30 on the small widget (96 dp box) and 0.22 on the large widget (84 dp box). The rosette is static and stays when reduce motion is on. Widgets themselves cannot use this composable: draw a static vector from the SVG in DESIGN §6.1 (section 10).

### 9.1a Strapwork rosette (twelve-fold) — `StrapworkRosette.kt`

DESIGN §6.1b. Build the geometry in unit space (centre at the origin, radii as fractions of S, angles clockwise from straight up) and scale at draw time. Cells are inset by `d` so the gaps read as straps. The corner fragment (eight-fold) is retired: delete `CornerFragment`.

```kotlin
internal object Strapwork {
    const val IN = .30f; const val SIDE = .46f; const val TIP = .64f; const val SIDEA = 11f
    const val APEX = .84f; const val STAR_P = .20f; const val STAR_V = .13f
    const val LEAF0 = .13f; const val LEAF1 = .40f; const val LEAFW = 7f
    const val C = .07f; const val RING = .105f; const val INSET = .014f; const val INSET_OUTER = .0125f

    fun pol(r: Float, deg: Float): Offset {
        val t = (deg - 90f) * (PI.toFloat() / 180f)
        return Offset(r * cos(t), r * sin(t))
    }

    /** Miter inset of a convex polygon by d (positive = inward). Normalise winding first. */
    fun inset(p: List<Offset>, d: Float): List<Offset> {
        val pts = if (signedArea(p) < 0) p.reversed() else p
        val n = pts.size
        // For each edge, shift it inward by d, then intersect neighbouring shifted edges.
        // (Port of inset() in the reference generator; verify winding with a unit test.)
        TODO("intersect offset edges; see test in section 11")
    }

    private fun signedArea(p: List<Offset>) =
        p.indices.sumOf { i -> val a = p[i]; val b = p[(i + 1) % p.size]; (a.x * b.y - b.x * a.y).toDouble() }.toFloat() / 2f

    fun cells(): List<List<Offset>> = buildList {
        for (i in 0 until 12) {
            val a = 30f * i; val b = a + 30f
            add(inset(listOf(pol(IN, a), pol(SIDE, a + SIDEA), pol(TIP, a), pol(SIDE, a - SIDEA)), INSET))
            add(inset(listOf(pol(SIDE, a + SIDEA), pol(TIP, a), pol(APEX, a + 15f), pol(TIP, b), pol(SIDE, b - SIDEA)), INSET))
            val m = (LEAF0 + LEAF1) / 2f
            add(listOf(pol(LEAF0, a + 15f), pol(m, a + 15f + LEAFW), pol(LEAF1, a + 15f), pol(m, a + 15f - LEAFW)))
        }
    }
    fun veins(): List<Pair<Offset, Offset>> = (0 until 12).map { i -> pol(LEAF0, 30f * i + 15f) to pol(LEAF1, 30f * i + 15f) }
    fun star(): List<Offset> = (0 until 12).flatMap { i -> listOf(pol(STAR_P, 30f * i), pol(STAR_V, 30f * i + 15f)) }
    /** Outer ring used by the pattern panel. */
    fun outerKites(): List<List<Offset>> = (0 until 12).map { i ->
        val a = 30f * i
        inset(listOf(pol(TIP, a), pol(APEX, a + 15f), pol(1.06f, a), pol(APEX, a - 15f)), INSET_OUTER)
    }
}

/** Draws the rosette with its centre at [centre]; one unit of S = [s] px. */
internal fun DrawScope.drawStrapwork(centre: Offset, s: Float, color: Color, strokeWidth: Float, withOuterRing: Boolean = false) {
    val stroke = Stroke(width = strokeWidth, join = StrokeJoin.Round)
    fun path(pts: List<Offset>, close: Boolean = true) = Path().apply {
        pts.forEachIndexed { i, p -> val x = centre.x + p.x * s; val y = centre.y + p.y * s; if (i == 0) moveTo(x, y) else lineTo(x, y) }
        if (close) close()
    }
    Strapwork.cells().forEach { drawPath(path(it), color, style = stroke) }
    Strapwork.veins().forEach { (a, b) -> drawPath(path(listOf(a, b), close = false), color, style = stroke) }
    drawPath(path(Strapwork.star()), color, style = stroke)
    drawCircle(color, Strapwork.RING * s, centre, style = stroke)
    drawCircle(color, Strapwork.C * s, centre, style = stroke)
    if (withOuterRing) Strapwork.outerKites().forEach { drawPath(path(it), color, style = stroke) }
}

/** Today header crop: the box is the S-sized square (150 dp); the centre sits on the top-end corner. */
@Composable
fun StrapworkCorner(modifier: Modifier = Modifier, strength: Float = 0.3f) {
    val color = Ahilla.colors.ink.copy(alpha = strength)
    Canvas(modifier.clipToBounds().clearAndSetSemantics { }) {
        val s = size.minDimension / 0.882f      // viewBox -0.882..0 wide, 0..0.882 tall
        drawStrapwork(Offset(size.width, 0f), s, color, strokeWidth = 0.0071f * s)
    }
}

/** Whole rosette (onboarding welcome 240 dp, greeting card 210 dp). Unit box is 1.8 wide. */
@Composable
fun StrapworkWhole(size: Dp, modifier: Modifier = Modifier, strength: Float = 0.6f, muted: Boolean = true) {
    val color = (if (muted) Ahilla.colors.inkMuted else Ahilla.colors.ink).copy(alpha = strength)
    Canvas(modifier.size(size).clearAndSetSemantics { }) {
        val s = this.size.minDimension / 1.8f
        drawStrapwork(center, s, color, strokeWidth = 0.006f * s)
    }
}
```

Implementation note: `inset()` is deliberately a stub. Port the reference generator (`inset` in `motif2/g3.py`, shown in DESIGN §6.1b by construction): shift each edge inward by d along its normal, then intersect each pair of neighbouring shifted edges. Add the unit test in section 11 (every inset cell stays inside its original cell and its area is smaller). Alternatively, ship the precomputed vector path in DESIGN §6.1b as a `PathParser` string and skip the runtime geometry: it is static, so this is the simpler option.

### 9.2 Starfield — `Starfield.kt`

```kotlin
@Composable
fun Starfield(seed: Int, modifier: Modifier = Modifier) {
    val motion = Ahilla.motion
    if (motion.reduceMotion) return                       // not drawn at all (DESIGN §7)
    val c = Ahilla.colors
    val strength = if (c.isDark) 0.8f else 0.4f

    @Composable fun twinkle(cycleMs: Int, offsetMs: Int): State<Float> =
        rememberInfiniteTransition(label = "twinkle").animateFloat(
            initialValue = 1f, targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                tween(cycleMs / 2, easing = LinearEasing), RepeatMode.Reverse,
                initialStartOffset = StartOffset(offsetMs),
            ),
            label = "twinkle",
        )
    val phases = listOf(twinkle(5000, 0), twinkle(5000, 1600), twinkle(7000, 0))

    Canvas(modifier.clipToBounds().clearAndSetSemantics { }) {
        val rnd = Random(seed)                            // deterministic: same layout every time
        val area = size.width * size.height / (390.dp.toPx() * 300.dp.toPx())
        val dots = (13 * area).roundToInt().coerceAtLeast(6)
        repeat(dots) { i ->
            val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
            val r = (0.8f + rnd.nextFloat() * 0.6f).dp.toPx()
            drawCircle(c.ink.copy(alpha = strength * phases[i % 3].value), r, p)
        }
        repeat(2) { i ->                                  // four-point glints
            val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
            val arm = 4.5.dp.toPx(); val col = c.ink.copy(alpha = strength * phases[i % 3].value)
            val sw = 1.dp.toPx()
            drawLine(col, p - Offset(0f, arm), p + Offset(0f, arm), sw, StrokeCap.Round)
            drawLine(col, p - Offset(arm, 0f), p + Offset(arm, 0f), sw, StrokeCap.Round)
        }
    }
}
```

Draw it behind the content (first child of a `Box`), never over text.

### 9.3 Typewriter eyebrow — `TypewriterEyebrow.kt`

```kotlin
@Composable
fun TypewriterEyebrow(text: String, modifier: Modifier = Modifier) {
    val reduce = Ahilla.motion.reduceMotion
    val c = Ahilla.colors
    var shown by remember(text, reduce) { mutableIntStateOf(if (reduce) text.length else 0) }
    var caretVisible by remember(text, reduce) { mutableStateOf(!reduce) }

    LaunchedEffect(text, reduce) {
        if (reduce) return@LaunchedEffect
        delay(500)
        val step = 2600L / text.length.coerceAtLeast(1)
        for (i in 1..text.length) { shown = i; delay(step) }
        delay(3000)
        caretVisible = false
    }
    val blink by rememberInfiniteTransition(label = "caret").animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(keyframes { durationMillis = 1000; 1f at 0; 1f at 499; 0f at 500; 0f at 999 }),
        label = "caret",
    )
    Row(modifier.semantics(mergeDescendants = true) { contentDescription = text }) {
        Text(text.take(shown), style = Ahilla.type.eyebrow, color = c.ink)
        if (caretVisible) {
            Box(Modifier.padding(start = 2.dp).width(1.dp).height(14.dp).alpha(blink).background(c.ink))
        }
    }
}
```

Pass the text already uppercased ("TODAY · 18 RAMADAN 1448"). Screen readers announce the full text immediately, regardless of the animation.

### 9.4 Pattern panel — `PatternPanel.kt`

DESIGN §6.4. The strapwork rosette plus a ring of twelve outer kites, cropped into the frame so the pattern fills it. No lattice, no fade, no medallion.

```kotlin
@Composable
fun PatternPanel(modifier: Modifier = Modifier) {
    val c = Ahilla.colors
    val shape = RoundedCornerShape(Ahilla.dims.rMoment)
    Canvas(
        modifier.fillMaxWidth().height(Ahilla.dims.patternPanelHeight).clip(shape)
            .border(Ahilla.dims.hairline, c.line, shape).clearAndSetSemantics { },
    ) {
        // viewBox -0.712 -0.596 1.424 1.192, preserveAspectRatio slice: scale to cover, centre.
        val s = maxOf(size.width / 1.424f, size.height / 1.192f)
        drawStrapwork(center, s, c.inkMuted.copy(alpha = 0.6f), strokeWidth = 0.0044f * s, withOuterRing = true)
    }
}
```

The caller (Mine empty state) places the panel with margin 8 / 16, then the centred text block (DESIGN §9.8).

## 10. Widgets

Widgets can't use the Compose theme.
- **Android (Jetpack Glance)**: read the same colour constants (`PaperColors`, `NightColors`) from commonMain and wrap each token in `ColorProvider(day = PaperColors.x, night = NightColors.x)` so widgets follow the system day/night setting. Radius: use the system widget corner radius where available, else 28 dp. Custom fonts are not available in Glance (to verify): render Cormorant numerals and names to a bitmap (`ImageProvider`), set at the needed px size and tinted with the right `ink`, or fall back to the system serif in widgets only. Decide this in the technical-design step.
- **iOS (WidgetKit, SwiftUI)**: a separate target that cannot import the Compose theme. Generate `AhillaTokens.swift` from the same values (recommended: keep one `design/tokens.json` and generate both the Kotlin and Swift files), bundle the same fonts in the widget extension, and register them in its Info.plist.
- Refresh at local midnight and, when sunset day change is on, at the city's sunset.
- Widgets are always the still version: no starfield, no typing.
- **Rosette in widgets:** Glance cannot draw on a Canvas, so ship the twelve-fold rosette as a static vector (generate it once from the SVG in `DESIGN.md` §6.1; mirror for other corners), tinted with `ink` at 30% (small widget, 96 dp) or 22% (large widget, 84 dp). In WidgetKit use a SwiftUI `Path` or an image asset with the same values.

## 11. Tests that protect the design

1. **Contrast test** (commonTest): fail the build if any text pair in `DESIGN.md` §2 drops below 4.5:1 or any control pair below 3:1.

```kotlin
private fun channel(x: Float): Double =
    if (x <= 0.03928f) x / 12.92 else ((x + 0.055) / 1.055).pow(2.4)
private fun luminance(c: Color) = 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
private fun contrast(a: Color, b: Color): Double {
    val (hi, lo) = listOf(luminance(a), luminance(b)).sortedDescending()
    return (hi + 0.05) / (lo + 0.05)
}

class ContrastTest {
    @Test fun textPairsMeetAA() {
        listOf(PaperColors, NightColors).forEach { t ->
            listOf(t.canvas, t.surface).forEach { bg ->
                assertTrue(contrast(t.ink, bg) >= 4.5)
                assertTrue(contrast(t.inkMuted, bg) >= 4.5)
                assertTrue(contrast(t.accent, bg) >= 4.5)
                assertTrue(contrast(t.event, bg) >= 4.5)
            }
            assertTrue(contrast(t.onAccent, t.accent) >= 4.5)
            assertTrue(contrast(t.inkMuted, t.accentWash) >= 4.5)
            assertTrue(contrast(t.accent, t.accentWash) >= 4.5)
        }
    }
}
```

2. **Screenshot tests**: `DayCell` in every state (default, today, outside month, selected, each dot kind) × Paper/Night × font scale 1.0, 1.3, 2.0. Month view, day planner, event screen, Today, Islamic day, Mine (with items and empty) and the Jump to date sheet in both themes at 1.0 and 2.0. Compare against the approved mockups by eye the first time, then freeze as goldens.
3. **Reduce-motion test**: with `AhillaMotion(true)`, `Starfield` draws nothing and `TypewriterEyebrow` shows the full text at once.

## 12. Mapping: DESIGN.md → code

| DESIGN.md | Symbol / file |
|---|---|
| §2 Colour | `AhillaColors`, `PaperColors`, `NightColors` — `Color.kt` |
| §3 Typography | `AhillaType`, `rememberAhillaType()` — `Type.kt` |
| §4 Spacing, shape, size | `AhillaDims` — `Dims.kt` |
| §6.1 Rosette (nested stars, widgets) | `Rosette` — `moments/Rosette.kt` |
| §6.1b Strapwork rosette | `StrapworkCorner`, `StrapworkWhole`, `drawStrapwork` — `moments/StrapworkRosette.kt` |
| §6.1a Corner fragment | retired; delete `CornerFragment` |
| §6.4 Pattern panel | `PatternPanel` — `moments/PatternPanel.kt` |
| §6.2 Starfield | `Starfield` — `moments/Starfield.kt` |
| §6.3 Eyebrow | `TypewriterEyebrow` — `moments/TypewriterEyebrow.kt` |
| §7 Reduce motion | `rememberReduceMotion()` + `AhillaMotion` — `Motion.kt` |
| §8.2 Bottom nav | `AhillaBottomBar` |
| §8.3 FAB | `AhillaFab` |
| §8.4 Day cell, dots | `DayCell`, `MarkerDot` |
| §8.11–8.13 Form, switch, radio | `FormCard`, `SwitchRow`, `RepeatOptionRow` |
| §8.14–8.15 Tonal button, status chip | `TonalButton`, `StatusChip` |
| §8.18, §8.22 List card, section heading | `ListCard`, `ListRow`, `SectionHeading` |
| §8.19 Bottom sheet | `AhillaSheet` |
| §8.20 Segmented toggle | `SegmentedToggle` |
| §8.21 Wheel picker | `WheelColumn`, `DateWheels` |
| §8.23–8.27 Checkbox row, stepper, expected row, amount row | `CheckboxRow`, `Stepper`, `ExpectedRow`, `AmountRow` |
| §8.26 Notification | `MoonNotifications` (section 13) |
| §8.28 Share card | `ShareCard` — `share/ShareCard.kt` (section 14) |
| §9.6–9.18 Screens | Today, Islamic day, Mine, Jump to date, Settings family, Announcements, Support, Onboarding, Share: compose from the components above |
| §10 Font-scale cap | `CappedFontScale` |
| §9.5 Widgets | Glance (Android), WidgetKit (iOS): section 10 above |

## 13. Region, announcements and notifications (logic the UI depends on)

Specified in `WEBSITE.md`; the UI contract only:
- `RegionPreset` (Saudi Arabia and the Gulf = 0 offset from Umm al-Qura; Pakistan = −1 day) is stored with the user's own ±day adjustment. A month start is `Expected` until a signed announcement or the user's confirmation arrives, then `Confirmed`. `ExpectedRow` binds to `MonthStart(state, expected, saudi)`.
- Sunset day change is off by default. The city is stored on the device and used only to suggest a region and to compute sunset.
- **Announcements (v1.1):** FCM data-only messages to the topic `region-<code>` (and `test`), subscribed only when "Announcements from Ahilla" is on. The app verifies the signature with the embedded public key and re-checks `startsOn` with its own engine; on any mismatch it shows nothing. The highest `revision` wins. `MoonNotifications.build(record)` creates the three notification templates of DESIGN §8.26 from the data; no ready-made text arrives from the server.
- **Evening reminder (v1):** a local notification scheduled for around sunset on the 29th (key months by default, or every month), independent of the network.
- Phone calendars (read-only, `READ_CALENDAR`, requested only from "Allow calendar access"): events appear as grey dots; nothing is written back in v1.

## 14. Share cards — `share/ShareCard.kt`

`ShareCard(kind, theme)` draws the four layouts of DESIGN §8.28 at 360 × 450 dp and is rendered to a 1080 × 1350 bitmap (`GraphicsLayer.toImageBitmap()` or a platform equivalent) for sharing. It ignores the app theme and uses the Paper or Night token set chosen in the sheet. Share through the platform sheet (`expect/actual`): the image alone for days, countdowns and greetings; the image plus the announcement link as text for the moon card. No analytics and no server round trip.

## 15. Extra tests for 1.2

- `strapwork_cells_inside`: each inset cell has less area than its source cell and all its points lie inside the source cell.
- `strapwork_path_matches_reference`: the generated unit path equals the reference in DESIGN §6.1b to 3 decimals.
- `region_offset`: Pakistan preset gives Umm al-Qura minus one day; the stepper adds on top; the day shown never changes at sunset unless the switch is on.
- `announcement_rejected`: an unsigned record, a bad signature or a `startsOn` that disagrees with the engine produces no notification and no date change.
- `calendar_readonly`: the app declares only the read permission and never writes to the calendar provider.
