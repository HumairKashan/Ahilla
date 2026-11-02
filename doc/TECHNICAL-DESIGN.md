# Ahilla — TECHNICAL-DESIGN.md

Design step 7. Version 1.0 · 26 Oct 2026. Draft for the owner's review: decisions marked **Proposed** are not confirmed.

Companion files: `DESIGN.md` and `DESIGN-compose.md` (the UI), `WEBSITE.md` (website and announcement service), `umm-al-qura-explained.md` (the engine's base data), `development-plan.md` (order of work).

**Status when written:** the base date engine (Umm al-Qura table, tabular fallback, tests) is built. The offset and overrides layers were being built in Claude Code in parallel; this file defines what they must become. Where it disagrees with code already written, stop and reconcile, don't silently pick one.

## 0. Rules

1. **The engine is pure.** `core:hijri` has no UI, no Android or iOS types, no clock, no I/O. It takes values in and returns values out, so every behaviour is unit-testable and identical on both platforms.
2. **Accuracy before beauty; fail silent, never wrong.** If an input can't be verified (an announcement, an override that breaks the month-length rule), it is ignored and the previous date stands.
3. **Local first.** All user data lives on the device. The network is used only by the optional announcements (section 8). Nothing is sent to us about the user.
4. **One source of truth for a date.** Every screen, widget, reminder and share card gets a date from the same `CalendarResolver`. No screen does its own date maths.
5. **Dates re-resolve, they are never stored as Gregorian.** An event on "18 Ramadan" is stored as Hijri month and day and resolved when needed, so a confirmed month start moves it correctly.

## 1. Modules (Kotlin Multiplatform)

```
core:hijri      pure Kotlin. UmmAlQura table, tabular, month-start model, resolver, recurrence maths, sunset maths.
core:model      data classes and enums shared by everything (Settings, Event, MonthStart, RegionPreset...).
core:data       storage (SQLDelight database, key-value settings), repositories. Depends on model.
core:announce   record parsing, signature verification, announcement store. Depends on model and hijri.
core:calendar   expect/actual: read-only phone calendars (CalendarContract on Android, EventKit later).
core:reminders  expect/actual: scheduling local notifications, rescheduling after any date change.
core:share      expect/actual: render a share card to an image, hand it to the system share sheet.
ui:design       tokens, components, motifs (the DESIGN-compose.md code).
feature:today, feature:month, feature:day, feature:event, feature:mine,
feature:settings (dates, calendars, location, appearance, support, privacy), feature:announcements,
feature:onboarding, feature:share
app:android     Application, Activity, manifest, FCM service, widgets (Glance), Play Billing
app:ios         later
```

Dependency rule: `feature:*` → `ui:design`, `core:*`. `core:hijri` depends on nothing. No `core` module depends on a `feature` or on `ui`. A feature never reaches another feature directly; navigation goes through the app module.

## 2. Date engine (`core:hijri`)

### 2.1 Layers
```
base method    Umm al-Qura (1343–1500 AH) or tabular; outside the table: tabular, flagged approximate
  → region + user shift      a whole-calendar shift in days (clamped −2 … +2)
    → month-start overrides   per month: announced or confirmed start dates
      → CalendarResolver      date shown in the app, widgets, reminders, cards
```
This matches the layers in `umm-al-qura-explained.md`, with "manual overrides" now split into announced and confirmed starts (section 2.3).

### 2.2 The month-start model
The calendar is a sequence of **month starts**: for each (Hijri year, month) a Gregorian date. The day of a Hijri month is `date − start(month) + 1`, where the month is the one with the latest start on or before `date`.
- **Base starts** come from the method table.
- **Shift:** every base start moves by `shift` days. A positive "starts later" shift lowers the Hijri date shown (Pakistan preset: starts 1 day later; today shows 17 Ramadan when Saudi shows 18). Settings use plain words; the sign lives only inside the engine, as in the engine decisions of 20 Oct.
- **Override:** replaces one month's start with a given date. Its effect is local: it changes the length of the month before it and of the month it starts.
- **Validity rule:** every month must be 29 or 30 days long (a 31- or 28-day month is impossible). An override or announcement that would break this for either neighbouring month is **rejected** and ignored. This is also the first line of defence against a typo or a wrong announcement.

```kotlin
data class MonthKey(val year: Int, val month: Int)            // month 1..12
data class MonthStart(
    val key: MonthKey,
    val startsOn: LocalDate,
    val state: StartState,        // EXPECTED, ANNOUNCED, CONFIRMED_BY_USER, PAST
    val saudiStartsOn: LocalDate, // Umm al-Qura, for the "Saudi Tue 9 Mar" line
)
interface CalendarResolver {
    fun hijriOf(date: LocalDate): HijriDate               // includes isApproximate
    fun startOf(key: MonthKey): MonthStart
    fun gregorianOf(h: HijriDate): LocalDate?             // day 30 of a 29-day month resolves to the last day (2.5); null only if year/month are invalid
    fun upcomingStarts(from: LocalDate, count: Int): List<MonthStart>
}
```
The resolver is built from `EngineInputs(method, shiftDays, overrides)` and is immutable. Changing any input builds a new resolver; the UI observes a `StateFlow<CalendarResolver>`.

### 2.3 Resolving a month start
For one month, in order:
1. **User confirmation** (`CONFIRMED_BY_USER`): the user's explicit choice in Confirm the day. It wins (it is their calendar), and "Use the expected day" removes it. **Proposed.**
2. **Announcement** (`ANNOUNCED`): the signed record for the user's region with the highest `revision`, if it passes verification (section 8.5) and the validity rule.
3. **Expected** (`EXPECTED`): base start plus region and user shift.
Once the date has passed the state becomes `PAST` (solid dot, no longer awaiting sighting). The Announcements screen lists only months whose state is `EXPECTED` or recently changed.

### 2.4 Sunset day change
Off by default. When on, "today" is computed from the instant, not the date:
`hijriOfInstant(now, zone, city)`: if `now` is after the city's sunset, use tomorrow's date. Sunset comes from a standard solar-position algorithm (NOAA) evaluated on the device from the city's latitude and longitude; no network. If the city is missing the switch can't be turned on. The widget and reminders use the same function and refresh at sunset (section 7).

### 2.5 Hijri days that don't exist
Events recurring on day 30 (or 29 of a 29-day month where it matters): resolve to the **last day of that month** and show it as such. Never skip a year. Birthdays on 30 of a month use the same rule. This is a documented behaviour with its own tests.

### 2.6 API stability
The engine's public API is small (above). Everything else is internal. Golden tests (section 10) freeze the dates the app has ever shown.

## 3. Regions
A `RegionPreset` is a bundled data file, changed only by an app release:

```json
{ "id": "pk", "name": "Pakistan", "shiftDays": 1, "cities": ["PK"],
  "announcer": "Central Ruet-e-Hilal Committee", "topic": "region-pk", "announcements": true }
{ "id": "sa-gulf", "name": "Saudi Arabia and the Gulf", "shiftDays": 0, "cities": ["SA","AE","KW","QA","BH","OM"], "announcements": false }
```
- A region is **suggested** from the city's country code; the user picks. "I'm not sure" = base method, shift 0.
- A new region is added only after its convention is researched and (for announcements) a trustworthy announcer exists. Europe and others are not in v1.
- Saudi Arabia and the Gulf has no announcements because the dates are calculated (a future arrangement is possible).
- Region and user adjustment are stored separately: `shift = region.shiftDays + user.adjust`, clamped −2 … +2, so changing the region doesn't lose the user's own adjustment.

## 4. Data model and storage

### 4.1 Entities
```kotlin
data class Settings(
    val method: Method,                 // UMM_AL_QURA | TABULAR
    val regionId: String?,              // null = none chosen
    val adjustDays: Int,                // user's extra shift, −2..2
    val sunsetChange: Boolean,          // default false
    val city: City?,                    // name, country, lat, lon; device only
    val theme: ThemeChoice,             // PAPER | NIGHT | SYSTEM
    val weekStart: DayOfWeek?,          // null = follow locale
    val defaultReminderTime: LocalTime, // 09:00
    val moonReminder: MoonReminder,     // OFF | KEY_MONTHS | EVERY_MONTH
    val announcementsOn: Boolean,       // subscribes to the region topic
    val showPhoneCalendars: Boolean,
    val shownCalendarIds: Set<Long>,
)
data class Override(val key: MonthKey, val source: Source /*USER|ANNOUNCEMENT*/, val startsOn: LocalDate,
                    val revision: Int?, val announcementId: String?, val savedAt: Instant)
data class Event(val id: String, val title: String, val date: EventDate, val allDay: Boolean,
                 val time: LocalTime?, val recurrence: Recurrence, val countdown: Boolean,
                 val reminder: Reminder?)
sealed interface EventDate { data class Hijri(val y: Int?, val m: Int, val d: Int); data class Gregorian(val date: LocalDate) }
sealed interface Recurrence { None; YearlyHijri; MonthlyHijri; YearlyGregorian; Custom(...) }
```
Islamic days (Ramadan, Eids, Ashura, Laylat al-Qadr, sacred months) are **not stored**: they are a bundled table of rules ("10 Dhul Hijjah", "the last ten nights of Ramadan", "month = Rajab") resolved by the engine. Their descriptions are bundled text.

### 4.2 Storage (**Proposed**)
- **SQLDelight** (KMP) for `Event` and `Override`: small, queryable, migrations are explicit.
- **multiplatform-settings** or DataStore for `Settings`.
- No cloud, no account, no export in v1. A device backup via Android's auto-backup is on (it covers the app's local data, not a server of ours). Decide whether to exclude the city from backup.
- Phone-calendar events are **never stored**: read on demand and held in memory.

### 4.3 Migration and integrity
Schema version in the database, forward-only migrations with a test per migration. An `Override` is validated against the resolver rule on every load; one that is no longer valid (for example after a rule change in a new table) is dropped and the user isn't alerted with an error, only the date reverts to expected.

## 5. Reminders
- **Kinds:** personal event reminders, Islamic-day reminders ("Arafah is tomorrow"), the **moon-sighting evening reminder** (around sunset on the 29th; key months or every month), and, in v1.1, announcement notifications (section 8).
- **Scheduling (Android, Proposed):** a `ReminderScheduler` computes the next N (about 30) reminders from the resolver and schedules them with `AlarmManager.setAndAllowWhileIdle` (inexact, no exact-alarm permission). Reminders at "9:00" tolerate a few minutes of drift. The moon reminder is time-sensitive at sunset, but a few minutes late is acceptable; do not request `SCHEDULE_EXACT_ALARM`.
- **Reschedule triggers:** boot, app update, time zone change, any change to settings, events, overrides or an accepted announcement, and a periodic worker (daily) as a safety net.
- **Copy** comes from DESIGN §8.26 and §11, and always names the date it refers to in both calendars.
- A reminder is never created without the notification permission; the permission is requested when the first reminder switch is turned on (DESIGN §9.16).

## 6. Phone calendars (`core:calendar`)
- Android: `CalendarContract` `Instances` query for the visible date range (the month on screen plus margins), filtered to `shownCalendarIds`. `READ_CALENDAR` is requested only from "Allow calendar access". A `ContentObserver` refreshes when the system calendar changes.
- Output type `ExternalEvent(title, start, end, allDay, calendarId)`. In the UI only a grey dot is shown on month cells; the day planner shows the events themselves (title and time) with a grey dot. Calendar names are not shown (decision of 26 Oct).
- Strictly **read-only** in v1: the manifest declares `READ_CALENDAR`, never `WRITE_CALENDAR`. Writing events into a calendar is v1.1 and will need a separate permission and its own design.
- Holiday and Birthday calendars from the Google account appear like any other if the user ticks them.
- Android only for now; iOS uses EventKit later behind the same interface.

## 7. Widgets and refresh
- Widgets cannot use the Compose theme or call the engine through the app process. The app writes a **snapshot** (`WidgetSnapshot`: today's Hijri and Gregorian date, the next few Islamic days and countdowns, the method line, the sunset time if enabled) to a small store the widget reads (Glance with DataStore). The widget never computes a date itself, but the snapshot contains enough upcoming days (about 60) that it can roll over at midnight without the app running.
- **Refresh:** at local midnight, at sunset when sunset change is on, after any change in settings, events or overrides, and after an accepted announcement. A widget showing a day that has changed is the worst bug a widget can have: test it (section 10).
- Lock screen widgets: iOS first (DESIGN §9.5).
- iOS widgets use the same snapshot idea with App Groups, later.

## 8. Announcement service and delivery (v1.1)

Product behaviour and the bot's rules are in `WEBSITE.md` section 3. This section fixes the technical shape.

### 8.1 Components
1. **Bot** (private, Telegram **Proposed**): a webhook function; allow-list of two user IDs; commands `/draft`, `/approve`, `/correct`, `/pause`, `/resume`.
2. **Date check:** the function imports the same `core:hijri` logic (Kotlin/JVM or JS via Kotlin/JS, **Proposed**: compile `core:hijri` to JS so the server and the app can't disagree). It computes `startsOn` from the announced outcome.
3. **Signer:** Ed25519 over the canonical JSON of the record. The private key sits in the host's secret store; the app embeds **two** public keys (current and next) so a key can be rotated without breaking old builds.
4. **Publisher:** writes the announcement page and `/moon/feed.json`, sends the FCM message, appends to the audit log, in that order; a failure anywhere aborts and reports to the announcer; nothing is half-published.
5. **Store:** a small key-value store (records, audit log, pause flags). No user data of any kind.
6. **Host (Proposed):** static site plus one function on a platform with a free tier and secrets (for example Cloudflare Pages and Workers, or Firebase Hosting and Functions). FCM sending uses a Google service account stored as a secret.

### 8.2 Record and canonical form
The record in `WEBSITE.md` section 4, plus `issuedAt` (UTC) and `expiresAt` (7 days; the app ignores an expired record even if its signature is valid). Signature covers the record with keys sorted and no whitespace; the signature field is excluded from what is signed.

### 8.3 FCM message
Data-only, high priority, topic `region-pk` (and `test`):
```json
{ "id": "pk-1448-10", "rev": "1", "rec": "<base64url of the signed record>" }
```
The whole signed record travels in the message (it is small), so the phone can verify it without a network request. FCM's size limit (4 KB) is far above it. No notification text is sent.

### 8.4 App side
- A `FirebaseMessagingService` (the FCM client library only, no other Firebase product, no analytics) receives the message and hands it to `core:announce`, which verifies and stores it, rebuilds the resolver, reschedules reminders, and posts the notification built on the phone (DESIGN §8.26).
- Subscribing: `FirebaseMessaging.subscribeToTopic("region-<id>")` when announcements are on and a region with announcements is set; unsubscribe when the switch goes off or the region changes. The FCM token stays on the device and with Google; it is never sent to us.
- **Catch-up (Proposed, optional):** if the user has announcements on, the app may fetch `/moon/feed.json` when opened on or after the 29th, so a phone that missed the push (no Google services, battery restrictions) still learns the result. This request reveals the phone's IP address to our host and is disclosed in the privacy policy; it is skipped when announcements are off.

### 8.5 Verification pipeline (the app, in order; any failure = ignore silently)
1. Parse; schema version known.
2. Signature valid against one of the embedded public keys.
3. `issuedAt` ≤ now + tolerance, `expiresAt` > now.
4. `region` equals the user's region.
5. `startsOn` recomputed by the local engine for (year, month, outcome) equals the record's `startsOn`.
6. The validity rule (29 or 30 days on both neighbouring months) holds.
7. `revision` is greater than what is stored for the same `id`.
Only then is it stored as an `ANNOUNCED` override.

### 8.6 Operating rules
- **Shadow mode** (draft everything, send nothing), then **test topic**, then go-live (`WEBSITE.md` 3.4). Each stage is a config flag on the server, not a code change.
- **Kill switch:** `/pause pk` stops publishing for the region instantly. A **bad announcement** is withdrawn with `/correct`, which is a new, higher-revision record that the app shows as a correction.
- **Audit log:** who, when, exact payload, result of each step. Kept on the server; contains no user data.
- **Alerts:** the announcer gets a message when a step fails or when the 29th evening passes with no draft in shadow or live mode.
- **Key custody:** back up the signing key offline and note where in the handover document. Losing it means shipping an app with a new public key; the second embedded key reduces that risk.
- **Handover/sale:** one repository, one domain, one signing key, one bot, one FCM project (`WEBSITE.md` section 7).

## 9. Permissions, privacy and store forms
| Permission | When | Why |
|---|---|---|
| `POST_NOTIFICATIONS` (Android 13+) | when the first reminder or announcements switch is turned on | reminders, announcements |
| `READ_CALENDAR` | only from "Allow calendar access" | show phone-calendar events |
| `ACCESS_COARSE_LOCATION` | only from "Use my location once" | pick a city; the result is a city name and coordinates, nothing else |
| `INTERNET` | always declared; used only for FCM and the optional feed | announcements |
| `RECEIVE_BOOT_COMPLETED` | declared | reschedule reminders after a reboot |
| Billing (`com.android.vending.BILLING`) | declared with the Play Billing library | Support Ahilla |
Not requested: `WRITE_CALENDAR`, exact alarms, precise or background location, contacts, any account.

**Play Data safety and the privacy policy must match this table.** The FCM token is created by Google's library and held by Google; state it plainly. Have a person who knows the form review it before submission (`WEBSITE.md` section 8).

## 10. Testing
- **Date engine (most important):** the existing checks (every day 1343–1500 AH against `java.time.chrono.HijrahDate`, anchors, table integrity, round trips) stay. Add: shift behaviour (including the sign), validity rule, overrides, rejection of invalid overrides, day-30 resolution (2.5), sunset logic against known sunrise tables, and **golden tests** that freeze the dates for every Islamic day of 1448–1450 for each region preset.
- **Property tests:** for any resolver, `gregorianOf(hijriOf(d)) == d`; month lengths are always 29 or 30; month starts strictly increase.
- **Announcements:** a fixture set of valid, unsigned, wrongly signed, expired, wrong-region, wrong-date, lower-revision and rule-breaking records; every invalid one must change nothing.
- **Reminders:** scheduling after boot, time-zone change and override change; DST edge cases in zones that observe it.
- **Widgets:** the snapshot rolls over at midnight and at sunset; a stale snapshot never shows yesterday's date after midnight.
- **UI:** screenshot tests of the share cards (Paper and Night) and key screens at 100% and 200% font scale; accessibility checks from DESIGN §10.
- **Manual before launch:** devices with and without Google services, battery saver on, notification permission denied, calendar permission denied.

## 11. Build, CI and releases
- Gradle with a version catalog; KMP modules as in section 1; CI on every push runs the unit and golden tests and the lint; releases are signed with a key stored outside the repo.
- Reproducible data: the Umm al-Qura table is generated from the documented ICU source by a script kept in the repo, with `THIRD_PARTY_NOTICES.md` (already in place).
- Minimum Android version and target API: decide at project setup (**Proposed:** min API 26, target the current Play requirement).
- Telemetry: none. Crash reports only from the Play Console's own, opt-in reports.

## 12. Mapping to the plan
| Plan item (`development-plan.md`) | Technical sections |
|---|---|
| 3.1 Project setup + date engine | 1, 2, 10 |
| 3.2 Month view and day view | 2, 6 (grey dots) |
| 3.3 Islamic events, settings (method, region, offset, announcements, sunset) | 2.3, 2.4, 3, 4 |
| 3.4 Personal events, recurrence, countdowns, reminders | 2.5, 4, 5 |
| 3.5 Widgets | 7 |
| 3.6 Read-only phone calendars, Support Ahilla | 6, 9 |
| v1.1 Announcement service | 8 |

## 13. Risks and open decisions
Risks: a wrong announcement (mitigated by human approval, engine re-check, validity rule, rehearsal, kill switch); a single announcer (a backup is needed before go-live); push not arriving on some phones (expected day and optional catch-up remain); a dated widget (snapshot design and tests); Play policy rejecting the support tiers; the engine and the app disagreeing with the server (shared `core:hijri`).

Open decisions:
1. User confirmation wins over an announcement (2.3). **Proposed.**
2. Server shares `core:hijri` through Kotlin/JS (8.1). **Proposed.** Alternative: a second implementation checked against the same golden tests.
3. Storage libraries (4.2) and minimum Android version (section 11).
4. Optional feed catch-up (8.4): include or drop for stricter privacy.
5. Host platform (8.1), bot platform, backup announcer, domain, key custody (`WEBSITE.md` 9).
6. Whether auto-backup should include the city (4.2).
7. Arafah wording and rule for people following the Saudi announcement while their Shawwal differs (`umm-al-qura-explained.md`, "Things to be aware of" 2).
