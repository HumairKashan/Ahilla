# Ahilla — DESIGN.md

Framework-neutral design spec. Version 1.2 · 26 Oct 2026.
Companion file: `DESIGN-compose.md` (how to build this in Compose Multiplatform).

**Changes in 1.2 (26 Oct 2026):** steps 5 and 6 approved. New strapwork rosette (6.1b) replaces the eight-fold fragment on the Today header (6.1a is retired) and the nested-star pattern panel on Mine empty (6.4 rewritten); the older twelve-fold rosette (6.1) stays on widgets until a decision (see "Pending decisions"). New components 8.23–8.28 (checkbox row, stepper, expected row, notification card, amount row, share card). New screens 9.10–9.18 (Settings home, Dates, Calendars, Your community, Location, Appearance and reminders, Announcements and Confirm the day, Notifications, Privacy and about, Support Ahilla, Onboarding, Share). The Month view gains a Moon sighting button (9.2). Pro and Give are gone; the design now covers regions, read-only phone calendars and moon-sighting announcements.

**Changes in 1.1 (25 Oct 2026):** new twelve-fold rosette (6.1) and pattern panel (6.4); the original eight-fold fragment is kept for the Today header only (6.1a); new screens 9.6–9.9 (Today tab, Islamic day, Mine with its empty state, Jump to date / converter); new components 8.18–8.22 (list card, bottom sheet, segmented toggle, wheel picker, section heading) plus a tonal button and a status chip; new tokens `scrim`, `numeralM`, `arabicXS`; Plex Mono scope widened (section 3); widgets use the new rosette. Salah times and the Pro screen are not part of v1.

Source of truth, in order: (1) this file, (2) the approved mockups on the "Ahilla — Brand Identity" design canvas, (3) `brand-identity.md`, `information-architecture.md`, `core-screens.md` in the Project. If this file and a mockup disagree, stop and ask; don't pick one.

## 0. Rules for whoever builds from this file

1. **Build only what is specified.** Section 9 lists the screens that are fully specified. Section 12 lists what is not designed yet. Do not invent those screens; use the tokens and components here and ask.
2. **Use tokens, never literals.** No hex colour, font size or dp value appears in screen code. Everything comes from the tokens below.
3. **Nothing is hard-coded white or black.** Line art, icons, stars and the motif all use `ink` (or `inkMuted` for the large pattern panel), so they flip with the theme.
4. **Accuracy before beauty.** A wrong date destroys trust; a plain screen doesn't. If the design and the date engine disagree, the engine wins.
5. **Calm over crowded.** No badges, banners, upsell prompts, streaks, confetti, or urgency language. If a feature adds a control that isn't in this file, ask first.
6. **Strings are localised from day one.** No user-facing text hard-coded in layouts. Layouts use start/end, never left/right (Arabic and Urdu UI will mirror).

## 1. Principles that decide design questions

- **Glance-first.** The widget is seen far more often than the app. Big numeral, one label, nothing else.
- **Hijri leads.** In the app, the Hijri date is primary and the Gregorian date is secondary, always shown together.
- **Honest about dates.** When a date depends on moon sighting, the UI says so (hollow dot, "Awaits sighting", method line). It never pretends to certainty.
- **Plain where you work, atmospheric where you pause.** The month grid, forms and settings are plain and readable. The motif and starfield appear only on "moments" (section 6).
- **Private.** No account, no tracking, works offline. One optional city is stored on the device. It suggests a region and drives the optional sunset day change. Two optional, disclosed exceptions: reading the phone's calendars (on the device, read-only) and, from v1.1, moon-sighting announcements delivered by Google's notification service by region.
- **Region-aware, never guessing.** Dates start from Umm al-Qura and shift to match the user's region (Pakistan is expected a day after Saudi Arabia). A month whose start depends on sighting shows as expected until an announcement or the user confirms it. The user never types a date from scratch.
- **Fail silent, never wrong.** An announcement that is not signed, not approved or does not match the date engine is ignored; the app keeps the expected day.

## 2. Colour

Two themes: **Paper** (light) and **Night** (dark). Same roles, every colour flips. Default follows the system; Settings › Appearance can force either.

| Token | Role | Paper | Night |
|---|---|---|---|
| `canvas` | App and widget background | `#F7F1E3` | `#16171A` |
| `surface` | Cards, sheets, dialogs | `#FCF8EF` | `#1F2024` |
| `line` | Dividers, card borders, grid rules | `#E2D8C3` | `#2F3136` |
| `ink` | Text, line art, icons, stars, motif | `#1F1B16` | `#ECE7DD` |
| `inkMuted` | Secondary text, Gregorian line, captions | `#6B6256` | `#A39C90` |
| `accent` | Today, selection, buttons, progress, your events | `#1F6F6B` | `#5FBFB5` |
| `accentWash` | Chips, pressed states, selected nav pill, method tag | `#E2ECE7` | `#1D2D2C` |
| `event` | Islamic-day dots and markers only | `#7E6026` | `#D4B26A` |
| `onAccent` | Text/icons on an `accent` fill | = `canvas` | = `canvas` |
| `scrim` | Dim layer behind sheets (the only token with alpha) | `#1F1B16` at 45% (`ink` at 45%) | `#08090A` at 62% (near-black, never pure black) |

Meaning:
- **Teal (`accent`) = you, now, act.** Today, selection, primary buttons, your own events, progress.
- **Gold (`event`) = a day the calendar marks.** Dots and small labels only. Never a button, never a fill, never text larger than a caption.
- **Meaning never rides on colour alone.** Today is a filled cell; an Islamic day is a dot; an awaiting-sighting day is a hollow dot. Add a text label or accessibility description for each.

Verified contrast (WCAG, computed 24 Oct 2026):

| Pair | Paper | Night |
|---|---|---|
| `ink` on `canvas` | 15.2 | 14.6 |
| `inkMuted` on `canvas` | 5.3 | 6.6 |
| `inkMuted` on `surface` | 5.7 | 6.0 |
| `inkMuted` on `accentWash` | 5.0 | 5.3 |
| `accent` on `canvas` | 5.3 | 8.2 |
| `accent` on `accentWash` | 4.9 | 6.6 |
| `event` on `canvas` | 5.2 | 8.9 |
| `event` on `surface` | 5.5 | 8.0 |
| `onAccent` on `accent` | 5.3 | 8.2 |

All text pairs meet 4.5:1. `line` is decorative only (1.3:1 / 1.4:1): never rely on it to show that a control is a control. Any control's boundary or state must use `ink`, `inkMuted` or `accent` (≥3:1).

## 3. Typography

All fonts are SIL Open Font License, free with transferable licences (the app may be sold). Ship the font files in the app; never load from the network. Keep the OFL text in `licences/` and in Settings › Privacy & about.

| Family | Use | Weights/styles to bundle |
|---|---|---|
| Cormorant Garamond | Numerals, dates, titles (display) | 500, 600, 500 italic |
| IBM Plex Sans | All UI text | 400, 500, 600 |
| IBM Plex Mono | Eyebrow labels, tags | 500 |
| Amiri | Arabic script | 400 |

Urdu needs a Nastaliq face (candidate: Noto Nastaliq Urdu, OFL). Not chosen yet (see section 12).

All sizes are in **sp** and scale with the system font size (see section 10 for the one exception).

| Role | Font | Size / line | Weight | Used for |
|---|---|---|---|---|
| `hero` | Cormorant | 112 / 92 | 500 | Today numeral (moments only) |
| `display` | Cormorant | 56 / 60 | 500 | Large dates in moments, share cards |
| `numeralL` | Cormorant | 64 / 56 | 500 | Countdown number in cards |
| `numeralM` | Cormorant | 30 / 30 | 500, tabular | Days number in Mine list rows |
| `cardTitle` | Cormorant | 30 / 34 | 500 | Event name in a countdown card |
| `screenTitle` | Cormorant | 34 / 38 | 500 | Month name; Hijri date on the day planner |
| `sheetTitle` | Cormorant | 32 / 38 | 500 | "New event" and other sheet titles |
| `dateValue` | Cormorant | 22 / 26 | 500 | Date value in forms |
| `numeral` | Cormorant | 22 / 24 | 500 (600 for today) | Hijri day in month cells |
| `heading` | Plex Sans | 20 / 28 | 600 | Section headings |
| `titleS` | Plex Sans | 17 / 24 | 600 | Title row of the selected-day preview card |
| `fieldText` | Plex Sans | 20 / 28 | 500 | Text typed into a field |
| `body` | Plex Sans | 16 / 24 | 400 | Body text |
| `bodyM` | Plex Sans | 15 / 22 | 500 or 600 | Row values, event card titles, buttons (600) |
| `label` | Plex Sans | 14 / 20 | 500 | Form labels, links |
| `caption` | Plex Sans | 13 / 18 | 400 | Secondary lines (Gregorian date, "in 3 days") |
| `small` | Plex Sans | 12 / 16 | 400 or 500 | Legend, helper text, nav labels (500, 600 selected) |
| `micro` | Plex Sans | 11 / 13 | 400 or 500 | Gregorian figure in month cell, weekday header (11/16, +0.04em, caps) |
| `eyebrow` | Plex Mono | 11 / 16 | 500, caps, +0.2em | "TODAY · 18 RAMADAN 1448" (moments only) |
| `eyebrowS` | Plex Mono | 10 / 14 | 500, caps, +0.2em | "COUNTDOWN", "COMING UP" |
| `tag` | Plex Mono | 10 / 14 | 500, caps, +0.14em | "UMM AL-QURA", "RAMADAN · DAY 18 OF 29" |
| `arabic` | Amiri | 28 / 44 | 400 | Arabic date and names |
| `arabicS` | Amiri | 26 / 40 | 400 | Arabic line in the Today card; Arabic name on the Islamic day screen |
| `arabicXS` | Amiri | 20 / 32 | 400 | Arabic date beside the Hijri date in the Islamic day dates card |

Rules:
- **Cormorant only at 20 sp and above.** It is thin at small sizes.
- **Plex Mono is for small caps labels** (eyebrows and tags): on moments, on the Islamic day screen's header and date labels, and on converter result labels. Never for body text, grids, lists or settings.
- **Figures that change or align use tabular numerals** (month cell Gregorian figure, agenda times, countdown).
- Sentence case everywhere except eyebrows/tags (caps) and the weekday header (caps).

## 4. Spacing, shape, elevation, size

- **Spacing scale (dp):** 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 24, 28, 32, 40, 48. Prefer 4-multiples.
- **Screen insets:** 16 for cards and grids; 20 for free text blocks; grid sits at 12.
- **Card padding:** 16–18 (forms 18, countdown card 22). **Gap between cards:** 12.
- **Radius (dp):** cell 14 · small card/strip/event card 16 · form card 20 · preview and countdown card 24 · FAB 18 · nav indicator 15 (a 56×30 pill) · pills and chips fully round · widgets 28 · "moment" frame 36.
- **Elevation: none.** No shadows anywhere. Separate surfaces with a 1 dp `line` border on `surface`, or by background change.
- **Borders:** 1 dp `line`. Selected/checked controls use `accent`.
- **Minimum touch target 48 × 48 dp.** Icon buttons are drawn 44 and must extend their hit area to 48.
- **Icon size:** 22 dp in a 44 dp button; 14 dp inline in chips and rows; 24 dp in the FAB.

## 5. Iconography

- **Monoline, 24 × 24 grid, 1.6 stroke** (1.8 for FAB and inline chips), round caps and joins, no fill.
- Colour: `ink` by default, `inkMuted` for secondary, `accent` for selected.
- No crescent, no five-pointed star, no star paired with a moon, anywhere (icons included).
- Icons used so far: chevron left/right, back arrow, close (×), plus, calendar, calendar-jump (calendar with arrow), settings (two sliders), today (rounded page with dot), mine (bookmark), repeat (two arcs with arrowheads), arrow-right (inside chips), share (arrow rising out of an open tray).
- Icon-only buttons always carry an accessibility label.

## 6. Motif and moments

**Moments** are the places where the app pauses: the Today header, countdown cards, share cards, onboarding. Only these get the motif, the starfield, the eyebrow and the typewriter reveal. Quieter cases: widgets carry the nested-star rosette (6.1) but no starfield or typing; the Mine empty state carries the strapwork panel (6.4) only; share cards carry the strapwork rosette (6.1b) and no starfield. The month grid, day planner, Islamic day screen, Mine list, event form, Jump to date sheet and all settings screens get none of the motif.

### 6.1 Rosette (twelve-fold) — approved 25 Oct 2026
The brand's geometric star motif: a twelve-point star flower inside two nested star outlines, drawn in one line colour. It is cropped at a card corner on widgets. (Since 1.2 it is used on widgets only; see 6.1b.) Never filled, never coloured.

Geometry. S = reference size (150). Centre C. Angles run clockwise from straight up. Radii are fractions of S (the reference value for S = 150 is in brackets):
- **Outer ring:** circle, 0.907·S (136).
- **Outer star outline:** 24 vertices, alternating. Points at 0.853·S (128), at angles 15° + 30°·i. Valleys at 0.680·S (102), at angles 30°·(i + 1).
- **Inner star outline:** same angles; points at 0.733·S (110), valleys at 0.587·S (88).
- **Twelve kites,** one per i = 0…11: inner vertex 0.147·S (22) at 30°·i; side vertices 0.320·S (48) at 30°·i ± 15°; tip 0.533·S (80) at 30°·i.
- **Centre circle:** 0.073·S (11).
Stroke 1.2 dp, colour `ink`.

Reference SVG (centre on the top-right corner of a 150 × 150 box; mirror for other corners; `currentColor` = `ink`):

```svg
<svg viewBox="0 0 150 150" fill="none" stroke="currentColor" stroke-width="1.2">
  <circle cx="150" cy="0" r="136"/>
  <polygon points="183.1,-123.6 201.0,-88.3 240.5,-90.5 238.3,-51.0 273.6,-33.1 252.0,0.0 273.6,33.1 238.3,51.0 240.5,90.5 201.0,88.3 183.1,123.6 150.0,102.0 116.9,123.6 99.0,88.3 59.5,90.5 61.7,51.0 26.4,33.1 48.0,0.0 26.4,-33.1 61.7,-51.0 59.5,-90.5 99.0,-88.3 116.9,-123.6 150.0,-102.0"/>
  <polygon points="178.5,-106.3 194.0,-76.2 227.8,-77.8 226.2,-44.0 256.3,-28.5 238.0,0.0 256.3,28.5 226.2,44.0 227.8,77.8 194.0,76.2 178.5,106.3 150.0,88.0 121.5,106.3 106.0,76.2 72.2,77.8 73.8,44.0 43.7,28.5 62.0,0.0 43.7,-28.5 73.8,-44.0 72.2,-77.8 106.0,-76.2 121.5,-106.3 150.0,-88.0"/>
  <polygon points="150.0,-22.0 162.4,-46.4 150.0,-80.0 137.6,-46.4"/>
  <polygon points="161.0,-19.1 183.9,-33.9 190.0,-69.3 162.4,-46.4"/>
  <polygon points="169.1,-11.0 196.4,-12.4 219.3,-40.0 183.9,-33.9"/>
  <polygon points="172.0,0.0 196.4,12.4 230.0,0.0 196.4,-12.4"/>
  <polygon points="169.1,11.0 183.9,33.9 219.3,40.0 196.4,12.4"/>
  <polygon points="161.0,19.1 162.4,46.4 190.0,69.3 183.9,33.9"/>
  <polygon points="150.0,22.0 137.6,46.4 150.0,80.0 162.4,46.4"/>
  <polygon points="139.0,19.1 116.1,33.9 110.0,69.3 137.6,46.4"/>
  <polygon points="130.9,11.0 103.6,12.4 80.7,40.0 116.1,33.9"/>
  <polygon points="128.0,0.0 103.6,-12.4 70.0,0.0 103.6,12.4"/>
  <polygon points="130.9,-11.0 116.1,-33.9 80.7,-40.0 103.6,-12.4"/>
  <polygon points="139.0,-19.1 137.6,-46.4 110.0,-69.3 116.1,-33.9"/>
  <circle cx="150" cy="0" r="11"/>
</svg>
```

Placement and strength:
- **Corner crop (widgets):** the box is S; the centre sits on the corner and the card clips the rest. Small widget: 96 dp at 30% of `ink`. Large widget: 84 dp at 22%. Keep it in the corner, never behind text a user must read.
- **Medallion:** no longer used (Mine empty now uses 6.4).
- The rosette is static. It stays when reduce motion is on.

### 6.1b Strapwork rosette (twelve-fold) — approved 26 Oct 2026
The brand's main motif from now on: a colourless twelve-fold strapwork rosette, drawn from the owner's reference pattern. Single line colour, no fill, no colour. Used cropped at the Today header corner, whole on onboarding and the greeting card, cropped on the other share cards, and extended into the Mine empty panel (6.4). Never a crescent, a five-pointed star or a star beside a moon.

Construction (unit space: centre at the origin, y down, angles clockwise from straight up, radii as fractions of S). `pol(r, a)` = the point at radius r and angle a.
Parameters: IN .30 · SIDE .46 · TIP .64 · SIDEA 11° · APEX .84 · STAR_P .20 · STAR_V .13 · LEAF0 .13 · LEAF1 .40 · LEAFW 7° · C .07 · RING .105 · inset D .014.
- **12 kites**, i = 0…11, a = 30°·i: `[pol(IN,a), pol(SIDE,a+SIDEA), pol(TIP,a), pol(SIDE,a−SIDEA)]`.
- **12 "houses"** between kites: `[pol(SIDE,a+SIDEA), pol(TIP,a), pol(APEX,a+15°), pol(TIP,a+30°), pol(SIDE,a+30°−SIDEA)]`.
- **12 leaves** at 30°·i + 15°, not inset: a rhombus `[pol(LEAF0,a+15°), pol(m,a+15°+LEAFW), pol(LEAF1,a+15°), pol(m,a+15°−LEAFW)]` with m = (LEAF0+LEAF1)/2, and a centre vein from `pol(LEAF0,a+15°)` to `pol(LEAF1,a+15°)`.
- **Centre:** a 12-point star (points STAR_P at 30°·i, valleys STAR_V at 30°·i + 15°), a ring of radius RING and a centre circle of radius C.
- **Inset:** every cell is shrunk inward by D so the gaps read as straps.

Reference path (unit space, S = 1; scale by S and stroke by 0.0071·S; `fill="none"`, `stroke-linejoin="round"`). Treat the construction above as the source of truth and the path as a check:

```svg
<svg viewBox="-0.9 -0.9 1.8 1.8" fill="none" stroke="currentColor" stroke-width="0.0062" stroke-linejoin="round">
  <path d="M0,-0.328 L0.072,-0.452 L0,-0.607 L-0.072,-0.452Z M0.098,-0.463 L0.017,-0.636 L0.211,-0.789 L0.303,-0.559 L0.147,-0.45Z M0.034,-0.126 L0.099,-0.246 L0.104,-0.386 L0.037,-0.262Z M0.034,-0.126 L0.104,-0.386 M0.164,-0.284 L0.288,-0.356 L0.303,-0.526 L0.164,-0.428Z M0.316,-0.352 L0.333,-0.542 L0.577,-0.577 L0.542,-0.333 L0.352,-0.316Z M0.092,-0.092 L0.209,-0.163 L0.283,-0.283 L0.163,-0.209Z M0.092,-0.092 L0.283,-0.283 M0.284,-0.164 L0.428,-0.164 L0.526,-0.303 L0.356,-0.288Z M0.45,-0.147 L0.559,-0.303 L0.789,-0.211 L0.636,-0.017 L0.463,-0.098Z M0.126,-0.034 L0.262,-0.037 L0.386,-0.104 L0.246,-0.099Z M0.126,-0.034 L0.386,-0.104 M0.328,0 L0.452,0.072 L0.607,-0 L0.452,-0.072Z M0.463,0.098 L0.636,0.017 L0.789,0.211 L0.559,0.303 L0.45,0.147Z M0.126,0.034 L0.246,0.099 L0.386,0.104 L0.262,0.037Z M0.126,0.034 L0.386,0.104 M0.284,0.164 L0.356,0.288 L0.526,0.303 L0.428,0.164Z M0.352,0.316 L0.542,0.333 L0.577,0.577 L0.333,0.542 L0.316,0.352Z M0.092,0.092 L0.163,0.209 L0.283,0.283 L0.209,0.163Z M0.092,0.092 L0.283,0.283 M0.164,0.284 L0.164,0.428 L0.303,0.526 L0.288,0.356Z M0.147,0.45 L0.303,0.559 L0.211,0.789 L0.017,0.636 L0.098,0.463Z M0.034,0.126 L0.037,0.262 L0.104,0.386 L0.099,0.246Z M0.034,0.126 L0.104,0.386 M0,0.328 L-0.072,0.452 L0,0.607 L0.072,0.452Z M-0.098,0.463 L-0.017,0.636 L-0.211,0.789 L-0.303,0.559 L-0.147,0.45Z M-0.034,0.126 L-0.099,0.246 L-0.104,0.386 L-0.037,0.262Z M-0.034,0.126 L-0.104,0.386 M-0.164,0.284 L-0.288,0.356 L-0.303,0.526 L-0.164,0.428Z M-0.316,0.352 L-0.333,0.542 L-0.577,0.577 L-0.542,0.333 L-0.352,0.316Z M-0.092,0.092 L-0.209,0.163 L-0.283,0.283 L-0.163,0.209Z M-0.092,0.092 L-0.283,0.283 M-0.284,0.164 L-0.428,0.164 L-0.526,0.303 L-0.356,0.288Z M-0.45,0.147 L-0.559,0.303 L-0.789,0.211 L-0.636,0.017 L-0.463,0.098Z M-0.126,0.034 L-0.262,0.037 L-0.386,0.104 L-0.246,0.099Z M-0.126,0.034 L-0.386,0.104 M-0.328,0 L-0.452,-0.072 L-0.607,0 L-0.452,0.072Z M-0.463,-0.098 L-0.636,-0.017 L-0.789,-0.211 L-0.559,-0.303 L-0.45,-0.147Z M-0.126,-0.034 L-0.246,-0.099 L-0.386,-0.104 L-0.262,-0.037Z M-0.126,-0.034 L-0.386,-0.104 M-0.284,-0.164 L-0.356,-0.288 L-0.526,-0.303 L-0.428,-0.164Z M-0.352,-0.316 L-0.542,-0.333 L-0.577,-0.577 L-0.333,-0.542 L-0.316,-0.352Z M-0.092,-0.092 L-0.163,-0.209 L-0.283,-0.283 L-0.209,-0.163Z M-0.092,-0.092 L-0.283,-0.283 M-0.164,-0.284 L-0.164,-0.428 L-0.303,-0.526 L-0.288,-0.356Z M-0.147,-0.45 L-0.303,-0.559 L-0.211,-0.789 L-0.017,-0.636 L-0.098,-0.463Z M-0.034,-0.126 L-0.037,-0.262 L-0.104,-0.386 L-0.099,-0.246Z M-0.034,-0.126 L-0.104,-0.386 M0,-0.2 L0.034,-0.126 L0.1,-0.173 L0.092,-0.092 L0.173,-0.1 L0.126,-0.034 L0.2,0 L0.126,0.034 L0.173,0.1 L0.092,0.092 L0.1,0.173 L0.034,0.126 L0,0.2 L-0.034,0.126 L-0.1,0.173 L-0.092,0.092 L-0.173,0.1 L-0.126,0.034 L-0.2,0 L-0.126,-0.034 L-0.173,-0.1 L-0.092,-0.092 L-0.1,-0.173 L-0.034,-0.126Z M0.105,0 a0.105,0.105 0 1,0 -0.21,0 a0.105,0.105 0 1,0 0.21,0 M0.07,0 a0.07,0.07 0 1,0 -0.14,0 a0.07,0.07 0 1,0 0.14,0"/>
</svg>
```

Placement and strength:
- **Today header crop:** a 150 dp box with the rosette's centre on its top-right corner (`viewBox="-0.882 0 0.882 0.882"`, so S ≈ 170 dp), stroke 0.0071 in unit space (≈ 1.2 dp), `ink` at 30%. Keep it in the corner, never behind more than the eyebrow.
- **Onboarding welcome:** whole, 240 dp, `inkMuted` at 60%, stroke 0.006.
- **Greeting share card:** whole, 210 dp of a 360 dp card, `inkMuted` at 55%. Other share cards: crop as on Today, 210 dp (190 on the moon card), `ink` at 30%.
- Static. It stays when reduce motion is on.

### 6.1a Corner fragment (eight-fold) — retired 26 Oct 2026
Replaced on the Today header by 6.1b. Do not build it. Kept below only so older mockups can be read.

(Retired spec follows.)

The first motif. A fragment of an eight-fold pattern, centred on a card corner and cropped by the card edge. Drawn in `ink`, 1 dp stroke, no fill.

Geometry, with S = the size of the square drawn at the corner (reference S = 150) and the centre on the corner:
- circles: radius 0.24·S and 0.813·S
- diamonds (squares rotated 45°): half-diagonal 0.44·S and 0.707·S
- axis-aligned squares: half-side 0.311·S and 0.5·S

```svg
<svg viewBox="0 0 150 150" fill="none" stroke="currentColor" stroke-width="1">
  <circle cx="150" cy="0" r="36"/>
  <circle cx="150" cy="0" r="122"/>
  <polygon points="216,0 150,66 84,0 150,-66"/>
  <polygon points="196.7,46.7 103.3,46.7 103.3,-46.7 196.7,-46.7"/>
  <polygon points="256,0 150,106 44,0 150,-106"/>
  <polygon points="225,75 75,75 75,-75 225,-75"/>
</svg>
```

Strength: 30% of `ink` on the Today header. Clip to the card; keep it in the corner, never behind more than the eyebrow.

### 6.2 Starfield (celestial)
Sparse dots and a few four-point glints, `ink`, ambient. Moments only. Never on the month grid, forms, settings or widgets.
- Dots: radius 0.8–1.4 dp, about 13 per 390 × 300 area; glints: a 8–10 dp plus sign, 1 dp stroke, round caps, 2 per area.
- Strength: Paper 40% of `ink`, Night 80% (Night needs more because off-white on charcoal reads dimmer when thin).
- Placement is decorative but **deterministic**: seed the layout from the card id/size so it doesn't jump on rotation or recomposition.
- Never: crescents, five-pointed stars, a star next to a moon.

### 6.3 Typewriter reveal and eyebrow
The eyebrow text ("TODAY · 18 RAMADAN 1448") types in with a blinking caret.
- Reveal: 23 characters in about 2.6 s, stepped (one character at a time), starting 0.5 s after the screen appears. Plays once per appearance.
- Caret: 1 dp wide, text height, blinks at 1 s intervals (50% duty), disappears when typing ends or after 3 s.
- Star twinkle: opacity 1 → 0.35 → 1 over 5 s; stagger every third and fourth star by 1.6 s and 3.1 s; every fifth star runs 7 s.
- Eyebrows are caps, Plex Mono 500, +0.2em tracking; separator is " · ".

### 6.4 Pattern panel (empty states) — rewritten 26 Oct 2026
A framed field built from the strapwork rosette (6.1b) and one ring of twelve outer kites around it, so the rosette reads as the centre of a larger pattern. For calm empty states only (the Mine tab today). It replaces the earlier nested-star lattice with a clear window and medallion.
- **Frame:** radius 36, 1 dp `line`, height 300, margin 8 / 16, clip content.
- **Outer ring:** twelve kites `[pol(TIP,a), pol(APEX,a+15°), pol(1.06,a), pol(APEX,a−15°)]`, a = 30°·i, inset D = .0125, in addition to the 6.1b cells.
- **Placement:** unit space cropped with `viewBox="-0.712 -0.596 1.424 1.192"` and `preserveAspectRatio="xMidYMid slice"` into the frame, so the pattern fills the frame and is cropped by its edges.
- **Stroke and colour:** 0.0044 in unit space (≈ 1 dp), `inkMuted` at 60%. Static; no starfield.

Reference path for the whole panel (rosette plus outer ring), unit space:

```svg
<svg viewBox="-0.712 -0.596 1.424 1.192" preserveAspectRatio="xMidYMid slice" fill="none" stroke="currentColor" stroke-width="0.0044" stroke-linejoin="round">
  <path d="M0,-0.325 L0.074,-0.452 L0,-0.61 L-0.074,-0.452Z M0.097,-0.462 L0.016,-0.636 L0.212,-0.791 L0.305,-0.559 L0.147,-0.449Z M0.034,-0.126 L0.099,-0.246 L0.104,-0.386 L0.037,-0.262Z M0.034,-0.126 L0.104,-0.386 M0.162,-0.281 L0.29,-0.355 L0.305,-0.529 L0.162,-0.428Z M0.315,-0.352 L0.332,-0.543 L0.579,-0.579 L0.543,-0.332 L0.352,-0.315Z M0.092,-0.092 L0.209,-0.163 L0.283,-0.283 L0.163,-0.209Z M0.092,-0.092 L0.283,-0.283 M0.281,-0.162 L0.428,-0.162 L0.529,-0.305 L0.355,-0.29Z M0.449,-0.147 L0.559,-0.305 L0.791,-0.212 L0.636,-0.016 L0.462,-0.097Z M0.126,-0.034 L0.262,-0.037 L0.386,-0.104 L0.246,-0.099Z M0.126,-0.034 L0.386,-0.104 M0.325,0 L0.452,0.074 L0.61,-0 L0.452,-0.074Z M0.462,0.097 L0.636,0.016 L0.791,0.212 L0.559,0.305 L0.449,0.147Z M0.126,0.034 L0.246,0.099 L0.386,0.104 L0.262,0.037Z M0.126,0.034 L0.386,0.104 M0.281,0.162 L0.355,0.29 L0.529,0.305 L0.428,0.162Z M0.352,0.315 L0.543,0.332 L0.579,0.579 L0.332,0.543 L0.315,0.352Z M0.092,0.092 L0.163,0.209 L0.283,0.283 L0.209,0.163Z M0.092,0.092 L0.283,0.283 M0.162,0.281 L0.162,0.428 L0.305,0.529 L0.29,0.355Z M0.147,0.449 L0.305,0.559 L0.212,0.791 L0.016,0.636 L0.097,0.462Z M0.034,0.126 L0.037,0.262 L0.104,0.386 L0.099,0.246Z M0.034,0.126 L0.104,0.386 M0,0.325 L-0.074,0.452 L0,0.61 L0.074,0.452Z M-0.097,0.462 L-0.016,0.636 L-0.212,0.791 L-0.305,0.559 L-0.147,0.449Z M-0.034,0.126 L-0.099,0.246 L-0.104,0.386 L-0.037,0.262Z M-0.034,0.126 L-0.104,0.386 M-0.162,0.281 L-0.29,0.355 L-0.305,0.529 L-0.162,0.428Z M-0.315,0.352 L-0.332,0.543 L-0.579,0.579 L-0.543,0.332 L-0.352,0.315Z M-0.092,0.092 L-0.209,0.163 L-0.283,0.283 L-0.163,0.209Z M-0.092,0.092 L-0.283,0.283 M-0.281,0.162 L-0.428,0.162 L-0.529,0.305 L-0.355,0.29Z M-0.449,0.147 L-0.559,0.305 L-0.791,0.212 L-0.636,0.016 L-0.462,0.097Z M-0.126,0.034 L-0.262,0.037 L-0.386,0.104 L-0.246,0.099Z M-0.126,0.034 L-0.386,0.104 M-0.325,0 L-0.452,-0.074 L-0.61,0 L-0.452,0.074Z M-0.462,-0.097 L-0.636,-0.016 L-0.791,-0.212 L-0.559,-0.305 L-0.449,-0.147Z M-0.126,-0.034 L-0.246,-0.099 L-0.386,-0.104 L-0.262,-0.037Z M-0.126,-0.034 L-0.386,-0.104 M-0.281,-0.162 L-0.355,-0.29 L-0.529,-0.305 L-0.428,-0.162Z M-0.352,-0.315 L-0.543,-0.332 L-0.579,-0.579 L-0.332,-0.543 L-0.315,-0.352Z M-0.092,-0.092 L-0.163,-0.209 L-0.283,-0.283 L-0.209,-0.163Z M-0.092,-0.092 L-0.283,-0.283 M-0.162,-0.281 L-0.162,-0.428 L-0.305,-0.529 L-0.29,-0.355Z M-0.147,-0.449 L-0.305,-0.559 L-0.212,-0.791 L-0.016,-0.636 L-0.097,-0.462Z M-0.034,-0.126 L-0.037,-0.262 L-0.104,-0.386 L-0.099,-0.246Z M-0.034,-0.126 L-0.104,-0.386 M0,-0.2 L0.034,-0.126 L0.1,-0.173 L0.092,-0.092 L0.173,-0.1 L0.126,-0.034 L0.2,0 L0.126,0.034 L0.173,0.1 L0.092,0.092 L0.1,0.173 L0.034,0.126 L0,0.2 L-0.034,0.126 L-0.1,0.173 L-0.092,0.092 L-0.173,0.1 L-0.126,0.034 L-0.2,0 L-0.126,-0.034 L-0.173,-0.1 L-0.092,-0.092 L-0.1,-0.173 L-0.034,-0.126Z M0.105,0 a0.105,0.105 0 1,0 -0.21,0 a0.105,0.105 0 1,0 0.21,0 M0.07,0 a0.07,0.07 0 1,0 -0.14,0 a0.07,0.07 0 1,0 0.14,0 M0,-0.656 L0.199,-0.813 L0,-1.041 L-0.199,-0.813Z M0.328,-0.568 L0.579,-0.604 L0.521,-0.902 L0.234,-0.804Z M0.568,-0.328 L0.804,-0.234 L0.902,-0.521 L0.604,-0.579Z M0.656,0 L0.813,0.199 L1.041,0 L0.813,-0.199Z M0.568,0.328 L0.604,0.579 L0.902,0.521 L0.804,0.234Z M0.328,0.568 L0.234,0.804 L0.521,0.902 L0.579,0.604Z M-0,0.656 L-0.199,0.813 L0,1.041 L0.199,0.813Z M-0.328,0.568 L-0.579,0.604 L-0.521,0.902 L-0.234,0.804Z M-0.568,0.328 L-0.804,0.234 L-0.902,0.521 L-0.604,0.579Z M-0.656,0 L-0.813,-0.199 L-1.041,0 L-0.813,0.199Z M-0.568,-0.328 L-0.604,-0.579 L-0.902,-0.521 L-0.804,-0.234Z M-0.328,-0.568 L-0.234,-0.804 L-0.521,-0.902 L-0.579,-0.604Z"/>
</svg>
```

## 7. Motion and reduce motion

- Motion is rare and slow. No bounces, no parallax, no confetti.
- Screen and sheet transitions: standard platform motion, 200–300 ms.
- **Reduce motion** (system setting) turns off the typewriter reveal, the caret, the starfield, and any transition longer than a fade. With reduce motion on, the eyebrow shows fully typed, the **starfield is not drawn at all** (not frozen), and the geometric motif stays.
- **Widgets always use the still version**: no starfield, no typing, motif as a static image.

## 8. Components

All components use tokens. "Min target" is 48 dp unless stated.

### 8.1 Top bar (tab screens)
Title block left: `screenTitle` + `caption` in `inkMuted` (e.g. "1448 AH · Feb – Mar 2027"). Actions right, each a 44 dp icon button (hit 48): previous month, next month, jump to date, settings. Padding: top 24, bottom 12, start 20, end 12. Detail screens (day planner) use a back arrow top-left (padding 16 top, 8 start) and previous/next day on the right; the title block sits below with 20 start inset.

### 8.2 Bottom navigation
Height 80, canvas background, 1 dp `line` top border. Three equal items: **Today, Calendar, Mine**. Each: a 22 dp icon inside a 56 × 30 indicator, label `small` below (gap 4). Selected: indicator `accentWash` (radius 15), icon and label `accent`, label weight 600. Unselected: `inkMuted`, weight 500. No badges. The bar is hidden on sheets (Add / edit event).

### 8.3 Floating action button (+)
56 × 56, radius 18, `accent` fill, `onAccent` plus icon (24 dp, 1.8 stroke). Position: end 16, bottom 16 above the nav bar. Accessibility label: "Add event" (on the day planner: "Add event on 25 February"). It adds an event on the selected/visible date.

### 8.4 Month day cell
Column width = (screen − 24 padding − 6 gaps of 2) / 7, minimum 52 dp wide, **height 58 dp**, radius 14. A centred column: Hijri numeral (`numeral`), Gregorian figure (`micro`, tabular, `inkMuted`), then a **reserved 5 × 5 dp dot slot** (always reserved, so rows align).

| State | Look |
|---|---|
| Default | `ink` numeral; `inkMuted` Gregorian figure |
| Today | `accent` fill; `onAccent` numeral (600) and Gregorian (500); slot empty unless there is a dot |
| Outside this Hijri month | numeral stays 500 but in `inkMuted` (no 400 weight is bundled) |
| Month change in the grid | Gregorian figure carries the month abbreviation ("8 Feb", "1 Mar") on the 1st of the Hijri month and on the Gregorian 1st; otherwise just the number |

Dots (5 dp circle):
- **Solid `event`**: an Islamic day.
- **Hollow `event`** (1.2 dp ring): an Islamic day that awaits sighting and has no override. It becomes solid when the user sets an override or the day has passed.
- **Solid `accent`**: one of the user's own events.

Last ten nights of Ramadan: a solid dot on each odd night (21, 23, 25, 27, 29).
Weekday header: `micro` 11/16, caps, +0.04em tracking, `inkMuted`, centred. Week start follows the phone's locale.

### 8.5 Legend and method line
Under the grid, padding 12 / 20 / 14. Row 1: three entries (dot + `small` text): Islamic day · Awaits sighting · Yours. Row 2: "Dates: Umm al-Qura · **Change**" (link in `accent`, 500) that opens Settings › Dates. The method named is whichever the user chose.

### 8.6 Selected-day preview card
`surface`, 1 dp `line`, radius 24, margin 0 / 16, padding 16 / 18. Contents, 12 gap: (a) title row: `titleS` "Thursday, 18 Ramadan" + `caption` "25 February 2027", and a **Open day** link right (`label`-weight 600, `accent`, hit area padded to 48); (b) one row per item (7 dp dot + `body`) e.g. "Ramadan · day 18 of 29"; (c) 1 dp `line` divider; (d) "Next" block: `small` 500 label, `body` name, right-aligned `caption` ("Sun 28 Feb / in 3 days"). Defaults to today.

### 8.7 Event card (day planner)
Tappable, `surface`, 1 dp `line`, radius 16, padding 10 / 14. Row 1: 7 dp `accent` dot + `bodyM` 600 title. Row 2: `caption` `inkMuted`: time range or, for repeating events, a 14 dp repeat icon + the rule ("Every night of Ramadan"), or "16:45 · reminder 15 min before". Time label sits left of the card: 44 wide, `caption` tabular `inkMuted`, aligned to the card's first line.

### 8.8 Now line (today only)
Row height 24: time in `small` 600 `accent`, an 8 dp `accent` dot, then a 2 dp `accent` line across.

### 8.9 Sunset line
Only when "sunset day change" is on. Row height 32: time (44 wide, `caption`), "Sunset" `small` 500, then a 1 dp `line` across. Below it, a chip: "19 Ramadan begins" (arrow icon 14 dp, `small` 500, `accent` on `accentWash`, radius full, padding 4 / 10), indented 56 from the start.

### 8.10 Islamic-day strip
A link card under the day planner title: `surface`, 1 dp `line`, radius 16, padding 12 / 16, margin 0 / 16. Left: 7 dp `event` dot + `bodyM` 500 ("Ramadan · day 18 of 29"). Right: `caption` `inkMuted` ("Last ten in 3 days"). Opens the Islamic day screen.

### 8.11 Form card and rows
`surface`, 1 dp `line`, radius 20. Rows are min 56 dp high (the date row 64), padded 18 horizontally, separated by 1 dp `line`. Label: `label` `inkMuted`. Value: `bodyM` 500 (or `dateValue` for dates, with the Gregorian line in `caption` below). A row that opens something shows a 18 dp chevron in `inkMuted`. The title field is its own card: `small` 500 label above a `fieldText` input (no inner border, no underline).

### 8.12 Switch
Track 52 × 32, radius 16; thumb 24, inset 4. **On:** `accent` track, `onAccent`-coloured thumb (`canvas`). **Off:** transparent track with a 2 dp `inkMuted` outline, 16 dp `inkMuted` thumb, inset 8. Label on the left; optional `small` helper under it. The whole row is the touch target.

### 8.13 Radio list (Repeat)
Rows with a 20 dp circle. **Selected:** 6 dp `accent` ring with a `canvas` centre; row min height 52 when it has a second line (`small` `inkMuted`), text `bodyM` 600. **Unselected:** 1.5 dp `inkMuted` ring; text `body`, `inkMuted`; min height 44, expanded to a 48 dp hit area. Rows separated by 1 dp `line`.

### 8.14 Buttons
- **Primary pill (Save):** height 44 (hit 48), padding 0 / 20, radius 22, `accent` fill, `onAccent` text `bodyM` 600.
- **Primary pill, full width** ("Go to this date"): height 48, radius 24, same colours and text.
- **Tonal button, full width** ("Share this day"): height 52, radius 18, `accentWash` fill, `accent` text `bodyM` 600, optional 20 dp leading icon with a 10 gap. The only secondary filled button in v1.
- **Text link:** `label` 500–600 in `accent`, hit area 48.
- Destructive actions (Delete) are a text button in `ink` with a confirmation sheet; no red fill.

### 8.15 Chips and tags
- **Chip:** radius full, `accentWash` fill, `accent` text `small` 500, optional 14 dp icon.
- **Method tag** (moments only): `tag` style, `accent` on `accentWash`, padding 5 / 10, radius full.
- **Status chip** (Islamic day screen): radius full, `surface` fill, 1 dp `line` border, `small` 500 in `ink`, padding 5 / 12, a 7 dp dot (hollow `event` for "Awaits sighting") then an 8 gap. A confirmed day shows no chip.

### 8.16 Countdown card (moments)
`surface`, 1 dp `line`, radius 24, padding 22, gap 14. Top: 7 dp `event` dot + `eyebrowS` "COUNTDOWN" in `inkMuted`. Middle: `cardTitle` event name (left) and `numeralL` number + `body` "days" (right, baseline-aligned). Then `caption` "1 Shawwal 1448 · Tuesday 9 March 2027". Bottom: 4 dp progress bar (`line` track, `accent` fill, radius 2) and a `tag` label "RAMADAN · DAY 18 OF 29".

### 8.17 Today header (moment)
Frame radius 36, `canvas`, 1 dp `line`, padding 40 / 28 / 28. Starfield behind; strapwork corner crop (6.1b) top-right. Content: eyebrow with typewriter ("TODAY · 18 RAMADAN 1448"); `hero` numeral with the month name in Cormorant 500 italic 36/36 and "1448 AH" in Plex Mono 500 12/16 +0.2em `inkMuted`; Arabic date (`arabicS`, `inkMuted`); a row with the Gregorian date (`body`, `inkMuted`) and the method tag. The rest of the Today tab is in 9.6.

### 8.18 List card and rows
Card: `surface`, 1 dp `line`, radius 20 (same as the form card), no padding. Row: min height 56 (64 when it has a second line), padding 8 / 18, 12 gap, separated by 1 dp `line`. Left: 7 dp dot (kinds in 8.4). Middle: `bodyM` 500 title and an optional `caption` `inkMuted` line (it may start with a 14 dp repeat icon). Right (optional): two stacked `caption` lines in `inkMuted`, end-aligned; or `numeralM` plus `caption` "days" for a day count. The whole row is the touch target.

### 8.19 Bottom sheet
Top corners radius 28, `canvas`, 1 dp `line` top border, padding 10 / 16 / 28. Drag handle 36 × 4 dp, radius 2, `inkMuted` at 50%, centred. The screen behind is covered by `scrim`. Dismiss by swiping down, tapping the scrim, or Back. With reduce motion the sheet fades instead of sliding.

### 8.20 Segmented toggle
Height 44, radius 22, `surface` fill, 1 dp `line`, padding 3. Two equal segments. Selected: `accentWash` fill, radius 19, text `label` 600 in `accent`. Unselected: no fill, `label` 500 in `inkMuted`. Each segment is a tab for screen readers ("Hijri, selected").

### 8.21 Wheel picker
Height 200 (five rows of 40), `surface`, 1 dp `line`, radius 20. Selection band: inset 8, top 79, height 40, radius 12, `accentWash`. Day, month and year columns at widths 0.8 : 1.7 : 1, text centred, Cormorant 500: selected 26/40 `ink`; one step away 21/40 `inkMuted`; two steps away 18/40 `inkMuted` at 50%. Scrolling snaps to a row, with the platform's light haptic tick. Days clamp to the month length (29 or 30 for Hijri). Accessibility: each column exposes its value and increase / decrease actions; never rely on dragging alone.

### 8.22 Section heading
`label` (14/20, 500) in `inkMuted`. Start padding 20 (or 4 when inside a 16 inset). 10 below it to the content, 24 above the section.

### 8.23 Checkbox row
Min 56, padding 8 / 18, gap 14, separated by 1 dp `line`. A 22 dp box, radius 6. **On:** `accent` fill with a `canvas` check (3 dp stroke). **Off:** 1.5 dp `inkMuted` outline, no fill. Name `bodyM` (500; 600 when on), optional `caption` `inkMuted` under it. Role: checkbox. Used in Calendars.

### 8.24 Stepper (− / +)
Two 44 dp circles, 8 dp apart, 1.5 dp `inkMuted` outline, `ink` icon (20 dp, 1.8 stroke), hit area 48. Labels "Fewer days" and "More days". Used for the date adjustment in Dates. Range −2 … +2 days.

### 8.25 Expected row (Announcements)
List-card row (8.18), min 64. Left: `bodyM` 500 month and year ("Shawwal 1448"); `caption` `inkMuted` what it starts ("Eid al-Fitr", "Sacred month · First ten days, Arafah, Eid al-Adha"). Right, two `caption` lines: first the region's expected date ("Expected Wed 10 Mar", tabular), second the Saudi date in `inkMuted` ("Saudi Tue 9 Mar"). A hollow `event` dot marks the group heading "Awaiting sighting". Tapping a row opens Confirm the day (9.13). After an announcement or a confirmation the row moves to "Confirmed" with a solid dot.

### 8.26 Notification card (specification of the notification content)
Moon-sighting notifications are standard Android notifications. Small icon: the app mark; the colour is the accent. Title is `bodyM`-weight, body is plain. Wording is fixed (section 11). Tapping opens the announcement. The three templates:
- **Evening of the 29th** (local, works offline, about sunset): "Moon sighting tonight" / "The Central Ruet-e-Hilal Committee is expected to announce this evening." (committee name comes from the region).
- **Sighted:** "Eid al-Fitr is tomorrow" / "The moon was sighted. Wed 10 Mar · 1 Shawwal 1448".
- **Not sighted:** "Ramadan completes 30 days" / "The moon was not sighted. Eid is on Thu 11 Mar."
Messages are built on the phone from a data-only push, never shipped as ready text (WEBSITE.md 3.6). A notification is never shown if the signature check or the engine check fails.

### 8.27 Amount row (Support)
Radio row (8.13) min 60 with a `numeralS`-style price at the end: Cormorant 500 22/26, tabular. The selected row is `bodyM` 600. Prices come from the store in the user's currency.

### 8.28 Share card (image)
A 4:5 image, 1080 × 1350 px, designed at 360 × 450 dp and exported at 3×. Full-bleed `canvas`, no border or radius (the image is shared as is). Padding 28. Top: dot + eyebrow (Plex Mono 11/16, caps, +0.2em; event dot for Islamic days and moon sightings, accent dot for your own events). Rosette crop (6.1b) top-right. Footer: 1 dp `line` rule 24 above the bottom; "Ahilla" Cormorant 500 24 with أهلّة Amiri 18 `inkMuted`; at the end a Plex Mono 10 caps tag for the method or region. No store badge, no URL on cards other than the moon card's source link.
- **Islamic day:** `cardTitle` 52/54 name; Hijri date Cormorant italic 500 32/36; Arabic date Amiri 24/40 `inkMuted`; Gregorian date `body` `inkMuted`.
- **Countdown:** event name Cormorant 500 38/42; number Cormorant 500 96/84 with "days" `body` `inkMuted`; Hijri date italic 26/30; Gregorian date.
- **Greeting:** whole rosette, "Eid Mubarak" Cormorant 500 46/50, عيد مبارك Amiri 30/46 `inkMuted`, date line `small`. No names. The same layout serves "Ramadan Mubarak".
- **Moon sighting:** eyebrow "MOON SIGHTING · PAKISTAN"; title Cormorant 500 36/40 ("Eid al-Fitr is tomorrow"); date italic 26/30; outcome line `body` `inkMuted`; a Source box (`surface`, 1 dp `line`, radius 16, padding 12 / 16) with "SOURCE" in Plex Mono 10, the source name `bodyM` 500, "Announced Tue 9 Mar, 21:40" `caption`, and the announcement link in Plex Mono 500 12/16 `accent`. The same card is the website's Open Graph image (cropped to 1200 × 630).
The card is drawn in Paper or Night as chosen in the share sheet, independently of the app theme.

## 9. Screens specified so far

### 9.1 Navigation
Three tabs: **Today** (opens first), **Calendar**, **Mine**. Settings behind a gear in the top bar. A + button on every tab opens Add / edit event. Details: `information-architecture.md`.

### 9.2 Month view (Calendar tab) — approved 23 Oct 2026
Hijri-led grid, one page per Hijri month. Header: month name + "1448 AH · Feb – Mar 2027"; buttons previous, next, jump to date, settings; swipe also changes month. Weekday row, then the grid (padding 0 / 12, gap 2), the legend and method line, then the selected-day preview card (defaults to today), the FAB, and the bottom nav. The method line (8.5) is a row with "Dates: Umm al-Qura · Change" (it shows the region, e.g. "Dates: Pakistan · Change") and, at its end, a **Moon sighting** pill: `accentWash` fill, `accent` text `small` 500, radius full, a 16 dp bell icon, the words "Moon sighting" and a hollow `event` dot while any month awaits sighting. It opens Announcements (9.13). Hit area expanded to 48. The icon is a bell, never a moon. No motif, no starfield. Tapping a day updates the preview card; "Open day" goes to the day planner. "Jump to date" opens a sheet that is also the Hijri ⇄ Gregorian converter (9.9).

### 9.3 Day planner — approved 23 Oct 2026
Back arrow, previous/next day. Title: Hijri date (`screenTitle`) and Gregorian date (`caption`, `inkMuted`). Islamic-day strip (8.10). Agenda list (padding 16 / 20): event cards (8.7) with time labels; the now line (8.8) on today; the sunset line (8.9) when sunset day change is on. **No salah times.** An empty day shows only the strip and the sunset line (if on). Times follow the phone's 12/24-hour setting.

### 9.4 Add / edit event — approved 24 Oct 2026
Full-screen sheet; no bottom nav. Close (×) top-left, Save pill top-right. Title `sheetTitle` ("New event"; the event's name in edit mode, with a Delete text button at the bottom). Cards, in order, 12 gap, 16 inset:
1. Title field.
2. Form card: **Date** (Hijri first: `dateValue` "18 Ramadan 1448", Gregorian `caption` "Thu 25 Feb 2027"; opens a picker that works in either calendar) and **All day** switch (off reveals a time row).
3. Repeat card: a radio list, always open: Doesn't repeat · **Every year on 18 Ramadan** (second line: "Next: Mon 14 Feb 2028 · follows the Hijri calendar") · Every month on the 18th (Hijri) · Every year on 25 February (Gregorian) · Custom…. The selected option always shows its next occurrence.
4. Form card: **Count down to it** switch (helper: "Shows on Today and as a widget") and **Remind me** row (value "1 day before, 9:00").
Reminders are opt-in; the notification permission is requested the first time a reminder is switched on. Absent on purpose: notes, location, guests, colours, categories.

### 9.5 Widgets — approved 24 Oct 2026; rosette updated 25 Oct 2026
Designed at 170 × 170 dp (2×2) and 360 × 170 dp (4×2); must adapt to the launcher's actual cell size. Radius 28 (use the system widget corner radius where available). Padding 16. Background `canvas`; Night adds a 1 dp `line` border (Paper has none). Tap on a date widget opens Today; tap on a countdown widget opens that event. **Still version only.**
- **Small · date:** "TODAY" in `eyebrow` (mono 11/16 +0.2em), Hijri day (Cormorant 500, 76/64), month name (Cormorant italic 500, 24/28), then a row: gold dot (only on an Islamic day) + Gregorian "Thu 25 Feb" (`small`, `inkMuted`). Twelve-fold rosette (6.1) top-right, 96 dp at 30%.
- **Small · countdown:** gold dot + "COUNTDOWN" (mono 11/16), number (Cormorant 500 76/64) with "days" (15/20, `inkMuted`), event name (Cormorant 500 22/26), date (`small`, `inkMuted`).
- **Large · date and coming up:** left column 112 wide: "TODAY", day (76/64), month (24/28 italic), "Thu 25 Feb · 1448" (`small`); a 1 dp `line` divider; right column: "COMING UP" (mono 11/16, `inkMuted`) and three rows, each a 6 dp dot, a name (`label`, 14/20 500) and a right-aligned "N days" (`small`, `inkMuted`). Dots: gold solid (Islamic day), gold hollow (awaits sighting), teal (own event). Twelve-fold rosette (6.1) top-right, 84 dp at 22%.
- **Lock screen:** inline text ("18 Ramadan 1448"); circular (Hijri day in Cormorant 28 with "RAM" in mono 9/12); rectangular (event name in Cormorant 21/24 + "12 days · Tue 9 Mar" 12/16). One tint chosen by the system, so gold and teal cannot appear. iOS-first (Android support is limited).
- **Refresh:** at local midnight and, when sunset day change is on, at the chosen city's sunset, so a widget changes date at the same moment the app does.
- Android widgets cannot load custom fonts (to verify during technical design). Numerals and names set in Cormorant should be rendered as images; if that is rejected, fall back to the system serif in widgets only.

### 9.6 Today tab — approved 25 Oct 2026
A scrolling column under a settings row (gear, 44 dp, hit 48, right-aligned, padding 12 / 12 / 0). Bottom padding 104 so the FAB never covers the last row. In order:
1. **Today header** moment (8.17), margin 0 / 16.
2. **Countdown card** (8.16), margin 12 / 16 above. It opens that event (or Islamic day). It shows the user's nearest countdown; if none is set, the next major Islamic day.
3. **Later today:** section heading (8.22), then event cards (8.7) with time labels (44 wide). Only events still to come today are shown. Proposed, not shown in a mockup: hide the section when nothing is left.
4. **Next Islamic days:** section heading, then a list card (8.18) with two rows: dot (solid, or hollow when the day awaits sighting), `bodyM` 500 name, right two lines in `caption` ("Sun 28 Feb" / "in 3 days"). It never repeats the day the countdown card shows. Each row opens the Islamic day screen (9.7).
Then the FAB (8.3) and the bottom nav with Today selected. No banners, no prompts.

### 9.7 Islamic day — approved 25 Oct 2026
Reached from Today, Calendar and the day planner. Back arrow top-left (padding 16 / 12 / 0 / 8), nothing on the right. The bottom nav stays, with the tab the user came from selected. Quiet on purpose: no starfield and no rosette. A scrolling column, in order:
1. **Title block** (padding 8 / 20 / 18, gap 6): `eyebrow` "ISLAMIC DAY" in `inkMuted`; `screenTitle` name ("Eid al-Adha"); `arabicS` Arabic name in `inkMuted`; a row with the status chip (8.15, "Awaits sighting", omitted for a confirmed day) and `caption` "in 80 days".
2. **Dates card** (surface, line, radius 20): two rows, padding 14 / 18, a 1 dp `line` between. Each row: `tag` label ("HIJRI", "GREGORIAN") in `inkMuted`, then `dateValue` ("10 Dhul Hijjah 1448", "Sunday, 16 May 2027"). The Hijri row has the Arabic date (`arabicXS`, `inkMuted`) at the end.
3. **Method card** (surface, line, radius 20, padding 16 / 18, gap 8): `bodyM` 600 "Worked out by calculation"; `label` at weight 400 in `inkMuted`: "Dhul Hijjah begins when the moon is sighted, so where you are, Eid may fall a day earlier or later."; `small` `inkMuted`: "Dates: Umm al-Qura · **Change**" (link to Settings › Dates).
4. **About this day:** section heading, then one or two short `body` paragraphs. Wording is neutral and is reviewed before release (section 11).
5. **Around this day:** section heading, then a list card of related days (dot, name, right two lines: date and relation, e.g. "Sat 15 May" / "the day before").
6. **Actions:** a form card (8.11) with **Count down to it** (switch, shown off, helper "Shows on Today and as a widget") and **Remind me** ("1 day before, 9:00"), margin 24 above. Then the tonal "Share this day" button (8.14), margin 16 above, which opens the share card (step 6, not designed yet).

### 9.8 Mine — approved 25 Oct 2026
A tab screen. A settings row like Today's, then the title block (padding 0 / 20 / 8): `screenTitle` "Mine" and `label` at weight 400 in `inkMuted`, "Your events, countdowns and yearly dates". Up to three groups, each a section heading (8.22) and a list card (8.18) with rows of 64. A group with no items is hidden.
- **Countdowns:** events or Islamic days with "Count down to it" on. Dot: gold for an Islamic day, teal for your own. Row: `bodyM` 500 name; `caption` Hijri and Gregorian date ("1 Shawwal 1448 · Tue 9 Mar 2027"); at the end `numeralM` number with `caption` "days".
- **Coming up:** your events that are neither counting down nor yearly. Teal dot, name, `caption` time or repeat rule (with the 14 dp repeat icon), at the end two lines (date and "in 2 days", or "Today" and "20:30").
- **Every year:** events that repeat yearly. Teal dot, name, `caption` the rule with the repeat icon ("14 Dhul Qa'dah"), at the end the next date and "in 55 days".
Every item appears once, in the group that fits it, sorted by next date. A row opens Add / edit event in edit mode. Then the FAB and the bottom nav with Mine selected.

**Empty state** (nothing saved yet): same title block; below it the pattern panel (6.4); then, centred with 28 above: `cardTitle` "Nothing here yet"; `body` in `inkMuted` "Add a birthday, an anniversary or a date you are waiting for. Yearly dates follow the Hijri calendar, so you only enter them once."; `label` in `accent` "Tap + to add your first". The FAB is the only way to add; no extra button.

### 9.9 Jump to date / converter — approved 25 Oct 2026
A bottom sheet (8.19) over the month view; the month view and its nav show dimmed behind the scrim. Content, gap 12, in order:
1. **Title row:** `sheetTitle` "Jump to date" and a "Today" text link at the end (hit 48) that sets the wheels to today.
2. **Segmented toggle** (8.20): Hijri | Gregorian. It chooses which calendar you are entering.
3. **Wheel picker** (8.21): day, month and year of that calendar. It starts on the date currently selected in the month view.
4. **Result card** (surface, line, radius 20, padding 14 / 18, gap 6): `tag` label naming the other calendar ("GREGORIAN"), `dateValue` full date ("Tuesday, 9 March 2027"), and, if that day is an Islamic day, a `caption` line with its dot and name ("Eid al-Fitr · awaits sighting"). It updates live as the wheels move.
5. **Primary pill, full width** (8.14): "Go to this date". It closes the sheet and opens the month view on that date with the day selected.
This sheet is the Hijri ⇄ Gregorian converter; there is no separate converter screen. The same wheels are intended for the Date row of Add / edit event (to be confirmed, section 12).

### 9.10 Settings home — approved 26 Oct 2026
Back arrow, `screenTitle` "Settings". Two list cards (8.18, rows 64 with `bodyM` 500 name, `caption` summary, chevron). Card 1: **Dates** ("Umm al-Qura · Pakistan region"), **Calendars** ("3 calendars shown from your phone"), **Location** ("Islamabad · used only for sunset"), **Appearance and reminders** ("System theme · reminders at 9:00"). Card 2: **Support Ahilla** ("Optional. Nothing is locked."), **Privacy and about** ("No account, no tracking"). Footer: "Ahilla · Version 1.0", `micro`, centred. All summaries show the current setting.

### 9.11 Dates — approved 26 Oct 2026
Title "Dates" with subtitle "Hijri dates can differ by a day between countries." Scrolls.
1. Method radio card (8.13): **Umm al-Qura** ("Saudi Arabia's calculated calendar"), **Tabular (civil)** ("A fixed rule. Can differ by a day or two.").
2. Heading "Match your community"; one card: **Region** row (chevron; "Pakistan · usually a day after Saudi Arabia"), the **Adjust dates** row (`label` "Adjust dates", `dateValue` "1 day behind", `caption` "Today shows as 17 Ramadan 1448", stepper 8.24) and **Announcements** (chevron; "Eid al-Fitr and Eid al-Adha await sighting").
3. Heading "Day change"; one card: switch row "Change the date at sunset" (**off by default**; "Off: changes at midnight, like your phone") and a **City** row (value "Islamabad", chevron), shown at 45% opacity and not tappable while the switch is off.
4. Footnote: "Ahilla shows mainstream Sunni dates. Other traditions may be added later."
The Region preset sets the base offset (Pakistan: −1 day from Umm al-Qura, because the moon is sighted locally); the stepper adds or removes days on top of it.

### 9.12 Calendars — approved 26 Oct 2026
Title "Calendars", subtitle "Show events from the calendars on your phone." Cards: a switch row "Show phone calendars" ("In Today and the month view"); a heading with the account ("Google · name@gmail.com") and a card of checkbox rows (8.23): Timetable, Birthdays, Holidays in Pakistan; a heading "On this phone" and its calendar rows. Footnote: "Ahilla only reads these calendars. It never changes them or sends them anywhere. To edit an event, open the calendar it came from."
Phone-calendar events appear in Today and the day planner as a **grey dot only** (`inkMuted`, 5 dp), with no calendar names; they are separate from Ahilla's own events (teal) and Islamic days (gold).
**Before access:** the same title; a card (`surface`, radius 24, padding 24 / 22 / 14) with `cardTitle` "Bring your day in", a `body` explanation, a full-width primary pill "Allow calendar access" and a text button "Not now". Footnote "Android will ask you once. You can turn this off at any time." The system permission appears only after the pill is tapped.

### 9.13 Announcements and Confirm the day — approved 26 Oct 2026
Opened from Dates, the Month view's Moon sighting pill, or a notification. Title "Announcements". Heading "Awaiting sighting" with a hollow gold dot, then one list card of expected rows (8.25): Shawwal 1448 (Eid al-Fitr, expected Wed 10 Mar, Saudi Tue 9 Mar), Dhul Qa'dah 1448 (sacred month, Fri 9 Apr / Thu 8 Apr), Dhul Hijjah 1448 (sacred month, first ten days, Arafah, Eid al-Adha, Sat 8 May / Fri 7 May), Muharram 1449 (sacred month, Islamic New Year, Ashura, Mon 7 Jun / Sun 6 Jun). Footnote: "Your region announces after the moon is sighted, usually the evening before. Until then Ahilla shows the expected day."
**Confirm the day** is a bottom sheet (8.19) from a row: title "Confirm the day" (`sheetTitle`), `small` `inkMuted` "Shawwal 1448 · Eid al-Fitr · Saudi Arabia: Tue 9 Mar 2027", a radio card with "Tue 9 Mar 2027 · Same day as Saudi Arabia" and "Wed 10 Mar 2027 · A day after Saudi Arabia · expected", a primary pill "Save" and a text button "Use the expected day". Confirming is for correcting a region's expectation; Ahilla never asks the user to type a date.

### 9.14 Your community (Region) — approved 26 Oct 2026
Title "Your community", subtitle "Ahilla starts from Umm al-Qura and shifts it to match how your region sees the moon." A **Your city** row (city and country; chevron to Location), then a radio card with the regions offered for the city, each with a `caption`: "Saudi Arabia and the Gulf" ("Umm al-Qura, as calculated") and "Pakistan" ("The moon is sighted locally, usually a day after Saudi Arabia", status chip "Suggested for Islamabad", 8.15). Footnote about choosing the family's region and more regions later. Regions are only offered where Ahilla knows the convention; others are added when a trustworthy source and announcer exist.

### 9.15 Location — approved 26 Oct 2026
Title "Location". A card with the current city (check mark). A full-width tonal button (8.14) "Use my location once" (location permission is asked only here, and only the city and coordinates are kept, on the phone). A search field. Heading "Common cities" and a list card (Karachi, Lahore, Dubai, Riyadh). Footnote: "Only the city name and coordinates are kept, on your phone." The city suggests a region and, if the sunset switch is on, gives the sunset time.

### 9.16 Appearance and reminders — approved 26 Oct 2026
Scrolls. **Theme:** a segmented toggle (8.20) with Paper, Night and System (System selected by default). **Calendar:** a row "Start of the week" (value "Monday"; follows the phone's locale by default). **Reminders:** a row "Default reminder time" (value "9:00", for all-day events). **Moon sighting** card: switch "Moon-sighting reminder" ("Evening of the 29th, around sunset") with a radio pair, "Key months (Ramadan, the Eids and the sacred months)" and "Every month". A separate card: switch "Announcements from Ahilla" ("Sent to your region through Google's notification service."). Footnote. The notification permission is requested when the first reminder or announcements switch is turned on.

### 9.17 Notifications — approved 26 Oct 2026
The three notification templates in 8.26, shown as Android notification cards on the lock screen (18:10 "Moon sighting tonight"; 21:40 "Eid al-Fitr is tomorrow"; 21:40 "Ramadan completes 30 days"). Tapping opens the announcement page in the app (or its source link). The calendar updates itself; the user can correct a month in Announcements.

### 9.18 Privacy and about, Support Ahilla, Onboarding, Share — approved 26 Oct 2026
- **Privacy and about:** title "Privacy and about", subtitle "Ahilla has no account and no tracking." One info card with four statements ("Your events stay on this phone", "Calendars are only read", "Moon announcements are optional", "No ads, no analytics"), a link card (Privacy policy, Open-source licences, Send feedback; external-link icon where it leaves the app), and the footer "Ahilla · Version 1.0".
- **Support Ahilla:** title; `body` `inkMuted` ("Ahilla is free, with no ads and nothing locked. If it helps you, you can support its upkeep: hosting, the moon-announcement service and new regions."); a segmented toggle (8.20) **One-time** / **Keep it running**; **One-time:** a radio card of amount rows (8.27): "A small thank you" $2, "A kind thank you" $5 (selected), "A generous thank you" $10; button "Say thanks"; **Keep it running:** Monthly $0.99 a month and Yearly $7.99 a year (selected); button "Keep Ahilla running"; footnote "Nothing extra unlocks. Cancel any time in Google Play. Payments go through Google Play, and Ahilla never sees your details." Nothing in the app is locked or changes. No prompts to open this screen anywhere. Amounts are placeholders until the store prices are set and the Play policy check is done.
- **Onboarding** (two steps, a progress indicator of two pill-dots, 20 × 6 active and 6 × 6 inactive): **Welcome** is a moment: starfield, typewriter eyebrow "A CALM HIJRI CALENDAR", whole strapwork rosette (6.1b), "Ahilla" (Cormorant 500 56/60), أهلّة (Amiri 32/48, `inkMuted`), `body` `inkMuted` "Hijri and Gregorian dates, side by side. No account. No tracking.", a primary pill "Get started". **Your dates:** `display` "Which dates does your community follow?", "You can change this later in Settings."; a **Your city** card ("Choose your city" in `accent` until set; optional); a "Dates to follow" radio card that starts with "Saudi Arabia and the Gulf" and "I'm not sure" (start with Umm al-Qura). When a city is chosen, its regional option is added at the top and selected, with a chip "Added for Islamabad" (for example "Pakistan"); the user can still pick another. A primary pill "Continue". No permission is requested during onboarding.
- **Share:** a bottom sheet (8.19, near full height) titled "Share this day" or "Share this announcement": a card preview at 70%, a segmented toggle Paper / Night for the card's look, a primary pill "Share" (system share sheet; the image, and for the moon card its source link), a text button "Save image", and a one-line note ("Shared as an image. Nothing else is sent." / "Shared with a link to the original announcement."). Reached from the Islamic day screen, an event, and an announcement. A share card is only made when the user asks for one.

## 10. Accessibility

- **Contrast:** text pairs in section 2 meet 4.5:1. Controls use `ink`, `inkMuted` or `accent` for boundaries and states (≥3:1).
- **Touch targets:** at least 48 × 48 dp.
- **Font scaling:** all text scales with the system setting; layouts must survive 200%. **One exception:** month-grid cell text caps at 130% so seven columns still fit; the selected-day preview card below scales fully and repeats the information. (Decision to confirm in review.)
- **Screen readers:** every day cell announces, in this order: weekday, Hijri date, Gregorian date, Islamic-day name and day number if any, whether it awaits sighting, "today" if so, number of your events. Example: "Thursday, 18 Ramadan 1448, 25 February 2027. Ramadan, day 18. Today. No events." Dots have text equivalents; icon-only buttons have labels; the FAB says what it adds and where.
- **Not colour alone:** today = fill, Islamic day = dot, awaiting sighting = hollow dot, own event = teal dot with count in the announcement.
- **Reduce motion** and **dark mode** are respected (sections 7 and 2).
- **RTL:** layouts mirror (start/end). Chevrons and the back arrow flip; the repeat icon, calendar and plus do not.
- **Focus order** follows reading order; the FAB comes after the content, before the bottom nav.
- **Sheets:** focus moves into the sheet when it opens and returns to the control that opened it; Back dismisses. **Wheels** expose increase / decrease actions and their current value (8.21).

## 11. Content and language

- Calm, plain, adult. Sentence case. No exclamation marks, no urgency ("hurry", "don't miss"), no streaks or guilt.
- Hijri first, Gregorian second. Gregorian formatting follows the phone's locale (Thu 25 Feb 2027).
- Honest wording for uncertain dates: "Awaits sighting", "Expected 1 Shawwal 1448", "Dates: Umm al-Qura" (or the region's name). For announcements: "Announced by the Central Ruet-e-Hilal Committee", never "Ahilla confirms". Add "Dates may differ in your area. Check with your local mosque if unsure." on the web page, not on the card.
- No claims about religious rulings. Islamic-day descriptions are short, neutral and sourced; wording is reviewed before release.
- Hijri month names (transliteration; the spelling convention is still to be confirmed): Muharram, Safar, Rabi' al-Awwal, Rabi' al-Thani, Jumada al-Awwal, Jumada al-Thani, Rajab, Sha'ban, Ramadan, Shawwal, Dhul Qa'dah, Dhul Hijjah.
- Digits follow the UI language's convention (Latin, Arabic-Indic or Extended Arabic-Indic for Urdu).

## 12. Not designed yet — do not build from guesses

All v1 screens in steps 1–6 are designed. Still open:
- Time picker for events; reminder picker; delete confirmation; notification-permission explanation (including the calendar permission); Start of the week picker; default reminder time picker. (The date wheels exist, 8.21; whether the event Date row opens them in a sheet like Jump to date is to be confirmed.)
- Islamic day wording for other date states: "Confirmed by sighting", "Set by you", a day already passed. Wording for dates outside the engine's accurate range, which fall back to the tabular method and are labelled "approx." (also needed on the Jump to date result).
- Mine: past events, sorting, long lists.
- A year view (all twelve Hijri months, opened from the month title): proposed 25 Oct 2026, not approved.
- Empty, loading and error states elsewhere (an empty day is in 9.3; Mine's empty state in 9.8). Announcements with nothing awaiting; a failed signature check shows nothing.
- A "not sighted / completes 30 days" moon card (same layout as the sighted card, different wording).
- The pre-Ramadan support ask (one dismissible card a year) was discussed and is not designed; it conflicts with "no nagging" and needs the owner's decision.
- Regions beyond Saudi Arabia and the Gulf and Pakistan (India, Bangladesh, Europe and others): each needs a researched convention and a trustworthy announcer before it appears.
- Sample screens (Today, Month, Islamic day, widgets) still show the Saudi example, 18 Ramadan 1448, with the chip "UMM AL-QURA"; with a region set, the chip and method line show the region.

States specified here by extension but not shown in a mockup. Verify in review before shipping:
- Selected day in the month grid when it is not today: proposed as a 1.5 dp `accent` ring (radius 14, no fill).
- A day with both an Islamic marker and your own event: proposed as two dots side by side, gold first, 3 dp apart. A phone-calendar event adds a grey dot after them.
- A disabled state for controls (the City row in Dates is the one specified case, 45% opacity).
- Pressed states: use `accentWash` as the pressed background on rows and cells.

Pending decisions:
- Whether widgets switch from the nested-star rosette (6.1) to the strapwork rosette (6.1b) so the brand uses one motif.
- Hijri month spelling convention (section 11).
- Urdu font (section 3).
- Month grid text scale cap at 130% (section 10).
- Widget fonts on Android (section 9.5).
- Night `scrim` value (section 2) and the widened Plex Mono scope (section 3): confirm in review.
- The tab name "Mine" was kept (a possible alternative, "Personal", was discussed; it is a one-string change).
- Support Ahilla route and amounts; Google Play policy on recurring support with nothing unlocked; Pakistani bank handling of payouts; tax. Before launch.
- Announcement service: bot platform, backup announcer, domain, signing-key custody (WEBSITE.md section 9). Release: v1.1, before Eid al-Fitr 1448 (about 9–10 March 2027).
