# CLAUDE.md — Ahilla (أهلّة)

Context for any Claude Code session working in this repo. Read this first.

## What Ahilla is
A focused, beautifully designed Hijri (Islamic) calendar app for Android and iOS.
It competes on accuracy, UI quality and widgets, not on feature count.
Name from Al-Baqarah 2:189: the new moons (al-ahilla) are markers of time for people and for Hajj.

Positioning: what Pillars did for prayer apps, done for the Hijri calendar —
calendar-first planning + premium design and reliable widgets + privacy-first, ad-free.

## Philosophy (tie-breakers for every decision)
1. **Calm over crowded.** One job, done beautifully. If a feature makes the app busier, it doesn't ship.
2. **Honest about dates.** Hijri dates differ by country. Always show which method is in use; let users match local sighting. Never claim to be "the correct" date.
3. **For the whole ummah.** No country, sect or madhhab bias. Neutral language; users choose which observances to show.
4. **Private by design.** Offline, no account, no tracking, no analytics, no ads. Location (later, for salah times / sunset) stays on the device.
5. **Free where it matters.** Core calendar free forever. One-time Pro unlock, no subscription, no nagging.
6. **Glance-first.** Widgets are a headline feature, not an afterthought.

## Hard constraints
- **No network access in the app.** No server, no remote config, no analytics/crash/tracking SDKs, no ads SDKs.
- **No new dependency without asking the developer first.** State what it is, its licence, and why.
- Only assets/libraries with licences that allow commercial use and transfer to a new owner (the app may be sold).
- Package / applicationId / bundle ID: **`app.ahilla`** — permanent, never change it.
- Do not modify `iosApp/` yet (no Mac available). Keep shared code iOS-compatible anyway.

## Stack
- Kotlin Multiplatform + Compose Multiplatform. Gradle (Kotlin DSL), version catalog in `gradle/libs.versions.toml`.
- `shared/` — all shared code: date engine, logic, and most UI. Work happens in `shared/src/commonMain`.
- `androidApp/` — thin Android entry point. Android widgets (Jetpack Glance, aim for Widget Quality Tier 1) go here later.
- `iosApp/` — thin iOS entry point. iOS widgets (SwiftUI + WidgetKit) go here later.
- Developer machine: Windows, Android Studio. Use `.\gradlew.bat` in PowerShell.

## Date engine rules (accuracy before beauty)
- Pure Kotlin in `commonMain`. **No platform calls, no java.time, no system clock inside the engine** — "today" is passed in.
- Methods: **Umm al-Qura (default)** from our own embedded table, so Android and iOS always agree; **tabular/civil** as the alternative.
- User offset: −2…+2 days, applied after the method.
- Manual month-start overrides (UI exposes Ramadan, Shawwal, Dhul Hijjah; engine accepts any month).
- Conversion both ways: Hijri ⇄ Gregorian.
- Every engine change ships with unit tests against published reference dates. Never weaken a test to make it pass.

## v1 priorities (MoSCoW)
- **Must:** accurate engine (method + offset), dual month view, Islamic event overlays, Hijri ⇄ Gregorian converter, home screen widgets (2×2, 4×2), offline/no account.
- **Should:** personal events with Hijri recurrence, countdowns, Hijri birthday, manual overrides, sunset day change, Sunnah fasting days, event sets by tradition, Pro unlock, Give screen.
- **Could:** salah time markers, lock screen widget, share cards, export to phone calendar, "on this day" layer.
- **Won't:** Quran reader, adhan/prayer notifications, accounts/cloud sync, ads, iOS before Android launch.

Precedence: accuracy before beauty · widgets before secondary screens · all Musts before any Could ·
protect the Ramadan 1448 deadline (~Feb 2027) by cutting scope, never by skipping tests.

## Users (check designs against them)
- **Ayesha** (Lahore): wants today's Hijri date at a glance via widget; sets "+1 day" once and forgets.
- **Omar** (Dubai): planner; Hijri recurring events, dual month view, calendar export. Likely Pro buyer.
- **Yusuf** (Manchester): follows his mosque's sighting; overrides, method choice, sunset day change, privacy.
- **Siti** (Kuala Lumpur): family Hijri birthdays, share cards, large text, calm UI.

## UI rules
- Theme lives in one place (colours, typography, shapes) so the brand can be swapped later. Placeholder: neutral teal accent, light + dark.
- No crescent/star iconography. Subtle geometric motif later.
- Respect system font scaling; strong contrast; generous touch targets.
- Advanced options one tap away, never on the main screen.

## Code conventions
- Clean, documented code: KDoc on public APIs, explain *why* for anything non-obvious (especially calendar math).
- Small, focused commits with clear messages.
- If something conflicts with this file or the project brief, ask instead of guessing.

## Commands
- Build Android debug: `.\gradlew.bat :androidApp:assembleDebug`
- Shared tests (JVM/Android host): `.\gradlew.bat :shared:testAndroidHostTest`

## Status
- Milestone 1 (in progress): project setup, CLAUDE.md, date engine + tests, basic month view, settings (method + offset), git.
