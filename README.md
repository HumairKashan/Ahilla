# Ahilla أهلّة

**A calm, beautifully designed Hijri calendar for Android and iOS.**

*Ahilla* (أهلّة) means "the new moons." The name comes from Al-Baqarah 2:189: *"They ask you about the new moons. Say: they are markers of time for people and for Hajj."*

Ahilla does one job, keeping time by the Hijri calendar, and tries to do it better than anyone else. It is calendar-first, privacy-first and ad-free.

> **Status:** In design. Android target: live before Ramadan 1448 (early–mid February 2027). iOS follows after the Android launch.

---

## Principles

These settle every design decision.

1. **Calm over crowded.** Every feature must earn its place. If it makes the app feel busier, it doesn't ship.
2. **Honest about dates.** Hijri dates can differ by a day between countries. Ahilla shows which method is in use and lets people match their local moon sighting.
3. **For the whole ummah.** Not tied to one country, sect or madhhab. Users choose which sets of observances to display.
4. **Private by design.** Works offline. No account, no tracking. Any location used for salah times stays on the device.
5. **Free where it matters.** The core calendar is free forever. No ads, no dark patterns. Pro is a one-time thank-you.
6. **Glance-first.** People see the widget far more often than they open the app, so widgets are a headline feature.

**Precedence:** accuracy before beauty; widgets before secondary screens; must-haves before anything else. The Ramadan deadline is protected by cutting scope, never by skipping testing.

---

## Features (v1)

**Core**
- Dual Hijri / Gregorian month view
- Hijri ⇄ Gregorian converter
- Islamic event overlays: Ramadan, Eid al-Fitr, Eid al-Adha, Ashura, Laylat al-Qadr and more, with toggleable event sets by tradition
- Home screen widgets (small 2×2, large 4×2)
- Fully offline, no account

**Planning**
- Personal events with Hijri recurrence (e.g. "every 10 Muharram")
- Countdowns (e.g. days until Ramadan)
- Hijri birthday from a Gregorian date of birth
- Sunnah fasting days highlighted
- Optional sunset day-change: the Hijri date turns over at maghrib, not midnight

**Later in v1 or v1.1 if time is short**
- Salah time markers in the day planner (reference only, no notifications)
- Lock screen widget
- Shareable event cards
- "On this day" historical layer
- Export events to the phone's calendar

**Deliberately out of scope:** Quran reader, adhan, prayer notifications, accounts, cloud sync and ads.

---

## Date engine

The date engine is the heart of the app. Accuracy is tested before anything else is built on it.

- **Default method:** Umm al-Qura
- **Alternatives:** tabular / Islamic civil
- **Offset:** ±1–2 days to match local moon sighting
- **Manual overrides** for sighting-dependent dates: the start of Ramadan and both Eids
- The Umm al-Qura table lives in shared Kotlin code, so Android and iOS always show identical dates
- Unit tested against known reference dates

---

## Tech stack

| Layer | Choice |
|---|---|
| Shared logic and date engine | Kotlin Multiplatform |
| UI | Compose Multiplatform |
| Android widgets | Jetpack Glance |
| iOS widgets | SwiftUI + WidgetKit (native) |
| IDE | Android Studio + Kotlin Multiplatform plugin |

iOS builds require a Mac (local, cloud Mac or cloud CI).

---

## Project structure

*Planned. This will be filled in once the project is scaffolded.*

```
ahilla/
├── shared/          # Date engine, data model, shared UI (Kotlin Multiplatform)
├── androidApp/      # Android entry point + Glance widgets
├── iosApp/          # iOS entry point + WidgetKit widgets
└── docs/            # Design notes, research, decisions
```

---

## Getting started

*Setup instructions will be added once the project is scaffolded.*

Prerequisites:
- Android Studio (latest stable) with the Kotlin Multiplatform plugin
- JDK 17+
- For iOS: macOS with Xcode

---

## Roadmap

1. **Requirements and research:** done (October 2026)
2. **Design:** brand identity → information architecture → core screens → widgets → secondary screens → technical design
3. **Implementation:**
   - date engine with tests
   - month and day views
   - event overlays and settings
   - personal events and countdowns
   - widgets
   - Pro and Give screens
   - polish (accessibility, Arabic/Urdu text rendering)
4. **Testing:** date engine reference tests, multiple devices, accessibility
5. **Deployment:** Play Store closed test (12+ testers, 14 days) → production
6. **Maintenance:** yearly sighting overrides, v1.1 features, then the iOS port

---

## Business model

- **Free forever:** dates, Islamic events, basic widgets, Hijri birthday
- **Pro (one-time purchase, no subscription):** premium widget styles and themes, unlimited countdowns and recurring events, share-card templates, historical layer
- **No ads, ever**
- **Give:** links to established charities. No money passes through the developer.

---

## Contributing

Ahilla is currently a solo project. If you'd like to help as a closed tester before launch, get in touch.

## Licence

All rights reserved. Third-party assets (fonts, icons) are used only under open, transferable licences.
