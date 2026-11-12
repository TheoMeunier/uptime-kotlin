# Changelog

All notable changes to Uptime Kotlin are documented in this file. The same notes are published with each
[GitHub release](https://github.com/TheoMeunier/uptime-kotlin/releases).

The project follows [Semantic Versioning](https://semver.org/). Security fixes are listed under a **Security** heading
(see [SECURITY.md](SECURITY.md)).

## [v1.6.2](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.6.2) - 2026-11-10

### New Features

* Email notifications are now sent as **HTML emails**, with a colored status badge, the monitor details (target, type, HTTP status, response time, dates) and the error, plus a plain-text version. The `cc` field of the mail channel is now applied by [@TheoMeunier](https://github.com/TheoMeunier) in [#242](https://github.com/TheoMeunier/uptime-kotlin/pull/242)
* Added `NOTIFICATIONS_DATE_FORMAT` to choose how dates are written in Slack, Teams, Telegram, Matrix, ntfy, Gotify, Web Push and email notifications by [@TheoMeunier](https://github.com/TheoMeunier) in [#244](https://github.com/TheoMeunier/uptime-kotlin/pull/244), requested by [@ceejaybassist](https://github.com/ceejaybassist) in [#211](https://github.com/TheoMeunier/uptime-kotlin/issues/211)
* **Ping** monitors now accept a hostname (e.g. `home.example.com`) as well as an IP address, resolved on every check so dynamic IPs keep working by [@TheoMeunier](https://github.com/TheoMeunier) in [#239](https://github.com/TheoMeunier/uptime-kotlin/pull/239), requested by [@alacava](https://github.com/alacava) in [#230](https://github.com/TheoMeunier/uptime-kotlin/issues/230)
* **HTTP** monitors now accept plain `http://` URLs, for internal services without TLS by [@TheoMeunier](https://github.com/TheoMeunier) in [#238](https://github.com/TheoMeunier/uptime-kotlin/pull/238), requested by [@alacava](https://github.com/alacava) in [#231](https://github.com/TheoMeunier/uptime-kotlin/issues/231)

### Improvements

* Added an **SMTP security** setting to email channels: STARTTLS, SMTPS (SSL/TLS) or Ignore TLS, so servers on port 465 now work. The email form is grouped into sections, and dialogs no longer overflow the screen by [@TheoMeunier](https://github.com/TheoMeunier) in [#243](https://github.com/TheoMeunier/uptime-kotlin/pull/243), requested by [@HenockMkb](https://github.com/HenockMkb) in [#221](https://github.com/TheoMeunier/uptime-kotlin/issues/221)
* The interface now uses the **dark theme by default**, including the login page and public status pages. The theme toggle still lets you switch by [@TheoMeunier](https://github.com/TheoMeunier) in [#236](https://github.com/TheoMeunier/uptime-kotlin/pull/236), requested by [@alacava](https://github.com/alacava) in [#234](https://github.com/TheoMeunier/uptime-kotlin/issues/234)

### Bug Fixes

* Fixed Slack push and desktop notifications showing `[no preview available]`: they now show the title and the message of the alert by [@TheoMeunier](https://github.com/TheoMeunier) in [#237](https://github.com/TheoMeunier/uptime-kotlin/pull/237), reported by [@alacava](https://github.com/alacava) in [#232](https://github.com/TheoMeunier/uptime-kotlin/issues/232)
* Fixed the sidebar sorting monitors with uppercase names first: the list is now sorted alphabetically regardless of case by [@TheoMeunier](https://github.com/TheoMeunier) in [#240](https://github.com/TheoMeunier/uptime-kotlin/pull/240), reported by [@alacava](https://github.com/alacava) in [#233](https://github.com/TheoMeunier/uptime-kotlin/issues/233)
* Fixed public status pages showing the full-screen loader twice before their content: they show their own skeleton again while loading by [@TheoMeunier](https://github.com/TheoMeunier) in [#241](https://github.com/TheoMeunier/uptime-kotlin/pull/241)

### Maintenance

* Removed the separate `api-arm64` image build: the `uptime-kotlin` and `worker` images are already published for amd64 and arm64 by [@TheoMeunier](https://github.com/TheoMeunier) in [#235](https://github.com/TheoMeunier/uptime-kotlin/pull/235)

**Full Changelog**: [v1.6.1...v1.6.2](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.6.1...v1.6.2)

## [v1.6.1](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.6.1) - 2026-11-08

### New Features

* Added a **private mode for the built-in `/status` page**: admins can restrict it to users signed in to the instance from the status pages list, anonymous visitors get a 404. It stays public by default by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/224
* Added custom **404 and 500 error pages** by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/219
* Added a discreet **"Powered by Uptime Kotlin"** footer on public status pages by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/223

### Improvements

* Email notifications now validate the list of recipients by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/218
* Public status pages no longer refetch every time the tab gets focus, and refresh every 2 minutes instead by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/224
* Passwords are now limited to 72 characters on login, account creation, profile update and first setup by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/225

### Bug Fixes

* Fixed read-only users seeing HTTP monitor secrets (headers and body) and the push token of push monitors by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/220
* Fixed the first account creation accepting any input: email, name and password are now validated, and two simultaneous setups can no longer both create an admin by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/226
* Fixed formula injection in the CSV export of monitor logs by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/227
* Fixed `/` showing a blank page: it now redirects to the dashboard by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/222

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.6.0...v1.6.1

## [v1.6.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.6.0) - 2026-11-07

### New Features

* Added a **MongoDB** monitor, with `mongodb://` and `mongodb+srv://` connection strings and a configurable command by [@TheoMeunier](https://github.com/TheoMeunier) in [#200](https://github.com/TheoMeunier/uptime-kotlin/pull/200)
* Added an **Oracle Database** monitor, with a configurable health check query by [@TheoMeunier](https://github.com/TheoMeunier) in [#202](https://github.com/TheoMeunier/uptime-kotlin/pull/202)
* Added a **WebSocket Upgrade** monitor for `ws://` and `wss://` URLs, with an optional subprotocol by [@TheoMeunier](https://github.com/TheoMeunier) in [#204](https://github.com/TheoMeunier/uptime-kotlin/pull/204)
* Added a **Docker Container** monitor that checks the container is running and, if it has a `HEALTHCHECK`, healthy by [@TheoMeunier](https://github.com/TheoMeunier) in [#205](https://github.com/TheoMeunier/uptime-kotlin/pull/205)
* Added a **Push (heartbeat)** monitor for crons, backups and batch jobs, which goes down when no call arrives in time by [@TheoMeunier](https://github.com/TheoMeunier) in [#209](https://github.com/TheoMeunier/uptime-kotlin/pull/209)
* Added **private status pages**, visible only to users signed in to the instance by [@TheoMeunier](https://github.com/TheoMeunier) in [#207](https://github.com/TheoMeunier/uptime-kotlin/pull/207)
* Added **multiple users** with Admin and Read-only roles by [@TheoMeunier](https://github.com/TheoMeunier) in [#208](https://github.com/TheoMeunier/uptime-kotlin/pull/208)

### Improvements

* A notification channel created from the monitor form is now enabled on that monitor straight away by [@TheoMeunier](https://github.com/TheoMeunier) in [#214](https://github.com/TheoMeunier/uptime-kotlin/pull/214)
* Added default values to notification fields (ntfy and Matrix server URLs, Webhook method), and the notification name now follows the selected type by [@TheoMeunier](https://github.com/TheoMeunier) in [#215](https://github.com/TheoMeunier/uptime-kotlin/pull/215)

### Bug Fixes

* Fixed TCP monitor creation: the port is now sent as a number, and the host accepts hostnames and IPv6 as well as IPv4 by [@TheoMeunier](https://github.com/TheoMeunier) in [#213](https://github.com/TheoMeunier/uptime-kotlin/pull/213), reported by [@ceejaybassist](https://github.com/ceejaybassist) in [#212](https://github.com/TheoMeunier/uptime-kotlin/issues/212) and [@JowisG](https://github.com/JowisG) in [#216](https://github.com/TheoMeunier/uptime-kotlin/issues/216)
* Fixed the email notification test failing to decrypt the SMTP password, and the notification dialog closing on an outside click by [@TheoMeunier](https://github.com/TheoMeunier) in [#206](https://github.com/TheoMeunier/uptime-kotlin/pull/206), reported by [@HenockMkb](https://github.com/HenockMkb) in [#210](https://github.com/TheoMeunier/uptime-kotlin/issues/210)

### Documentation

* Updated the demo preview and README video for v1.5, with a dark mode variant by [@TheoMeunier](https://github.com/TheoMeunier) in [#196](https://github.com/TheoMeunier/uptime-kotlin/pull/196) and [#197](https://github.com/TheoMeunier/uptime-kotlin/pull/197)

**Full Changelog**: [v1.5.0...v1.6.0](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.5.0...v1.6.0)

## [v1.5.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.5.0) - 2026-10-27

### New Features

* Added Web Push notifications with subscription management by [@TheoMeunier](https://github.com/TheoMeunier) in [#187](https://github.com/TheoMeunier/uptime-kotlin/pull/187)
* Added PagerDuty notifications with routing key support by [@TheoMeunier](https://github.com/TheoMeunier) in [#189](https://github.com/TheoMeunier/uptime-kotlin/pull/189)
* Added Matrix as a notification channel by [@TheoMeunier](https://github.com/TheoMeunier) in [#193](https://github.com/TheoMeunier/uptime-kotlin/pull/193)
* Added announcements with create, edit and delete operations by [@TheoMeunier](https://github.com/TheoMeunier) in [#188](https://github.com/TheoMeunier/uptime-kotlin/pull/188)
* Added Prometheus metrics by [@TheoMeunier](https://github.com/TheoMeunier) in [#190](https://github.com/TheoMeunier/uptime-kotlin/pull/190)
* Added an API version endpoint and the current version in the sidebar, with a link to the release by [@TheoMeunier](https://github.com/TheoMeunier) in [#194](https://github.com/TheoMeunier/uptime-kotlin/pull/194)

### Improvements

* Added hostname and IP address validation with custom error messages by [@TheoMeunier](https://github.com/TheoMeunier) in [#186](https://github.com/TheoMeunier/uptime-kotlin/pull/186)
* Added a warning when the example encryption master key is in use by [@TheoMeunier](https://github.com/TheoMeunier) in [#183](https://github.com/TheoMeunier/uptime-kotlin/pull/183)
* Added a documentation link to the sidebar by [@TheoMeunier](https://github.com/TheoMeunier) in [#182](https://github.com/TheoMeunier/uptime-kotlin/pull/182)
* Refactored the probe form's default values by [@TheoMeunier](https://github.com/TheoMeunier) in [#191](https://github.com/TheoMeunier/uptime-kotlin/pull/191)

### Bug Fixes

* Fixed reflection registration in the native build for snake_case serialized classes by [@TheoMeunier](https://github.com/TheoMeunier) in [#181](https://github.com/TheoMeunier/uptime-kotlin/pull/181)

### Documentation

* Updated the README for the `ENCRYPTION_MASTER_KEY` changes by [@TheoMeunier](https://github.com/TheoMeunier) in [#184](https://github.com/TheoMeunier/uptime-kotlin/pull/184)

### Maintenance

* Triggered the demo image build on version tags by [@TheoMeunier](https://github.com/TheoMeunier) in [#177](https://github.com/TheoMeunier/uptime-kotlin/pull/177) and [#178](https://github.com/TheoMeunier/uptime-kotlin/pull/178)
* Removed obsolete Docker build workflows and test files by [@TheoMeunier](https://github.com/TheoMeunier) in [#185](https://github.com/TheoMeunier/uptime-kotlin/pull/185)
* Removed an unused Prometheus metrics setting by [@TheoMeunier](https://github.com/TheoMeunier) in [#195](https://github.com/TheoMeunier/uptime-kotlin/pull/195)

**Full Changelog**: [v1.4.2...v1.5.0](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.4.2...v1.5.0)

## [v1.4.3](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.4.3) - 2026-10-19

### Improvements

* Added a startup warning when `ENCRYPTION_MASTER_KEY` still uses the example value by [@TheoMeunier](https://github.com/TheoMeunier) in [#183](https://github.com/TheoMeunier/uptime-kotlin/pull/183)
* Added a Documentation link to the sidebar, translated into all supported languages by [@TheoMeunier](https://github.com/TheoMeunier) in [#182](https://github.com/TheoMeunier/uptime-kotlin/pull/182)

### Bug Fixes

* Fixed JSON field naming in the native worker by registering `SnakeCaseStrategy` for reflection, matching the API by [@TheoMeunier](https://github.com/TheoMeunier) in [#181](https://github.com/TheoMeunier/uptime-kotlin/pull/181)
* Fixed the email notification Hostname field rejecting valid hostnames (e.g. `smtp.google.com`) with an "Invalid URL" error; it now accepts hostnames, domain names and IP addresses by [@TheoMeunier](https://github.com/TheoMeunier) in [#186](https://github.com/TheoMeunier/uptime-kotlin/pull/186), reported by [@sysctrladm](https://github.com/sysctrladm) in [#179](https://github.com/TheoMeunier/uptime-kotlin/issues/179)


### Documentation

* Updated the README to explain how to generate `ENCRYPTION_MASTER_KEY` and stop using the example value by [@TheoMeunier](https://github.com/TheoMeunier) in [#184](https://github.com/TheoMeunier/uptime-kotlin/pull/184)

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.4.2...v1.4.3

## [v1.4.2](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.4.2) - 2026-10-16

### New Features
- Added an **Incidents** page with ongoing incidents, history and filters, opened and closed automatically from alerts by [@TheoMeunier](https://github.com/TheoMeunier) in [#166](https://github.com/TheoMeunier/uptime-kotlin/pull/166) and [#167](https://github.com/TheoMeunier/uptime-kotlin/pull/167)
- Added incident acknowledgement and notes, and the dashboard's recent events now read from incidents by [@TheoMeunier](https://github.com/TheoMeunier) in [#175](https://github.com/TheoMeunier/uptime-kotlin/pull/175)
- Added monthly periods and CSV export for incidents by [@TheoMeunier](https://github.com/TheoMeunier) in [#176](https://github.com/TheoMeunier/uptime-kotlin/pull/176)
- Added a read-only demo mode with seeded monitors and status pages ([live demo](https://demo.uptime-kotlin.com)) by [@TheoMeunier](https://github.com/TheoMeunier) in [#163](https://github.com/TheoMeunier/uptime-kotlin/pull/163)

### Improvements
- Improved notification type handling with labels in the notification form by [@TheoMeunier](https://github.com/TheoMeunier) in [#172](https://github.com/TheoMeunier/uptime-kotlin/pull/172)
- Improved the sidebar search input layout and styling by [@TheoMeunier](https://github.com/TheoMeunier) in [#164](https://github.com/TheoMeunier/uptime-kotlin/pull/164)
- Used sequential group keys in the status page editor by [@TheoMeunier](https://github.com/TheoMeunier) in [#162](https://github.com/TheoMeunier/uptime-kotlin/pull/162)

### Bug Fixes
- Fixed the incident summary for periods that overlap an incident, with tests for outage calculations by [@TheoMeunier](https://github.com/TheoMeunier) in [#173](https://github.com/TheoMeunier/uptime-kotlin/pull/173)

### Documentation
- Updated the README with the live demo, features and configuration details by [@TheoMeunier](https://github.com/TheoMeunier) in [#168](https://github.com/TheoMeunier/uptime-kotlin/pull/168), [#169](https://github.com/TheoMeunier/uptime-kotlin/pull/169) and [#170](https://github.com/TheoMeunier/uptime-kotlin/pull/170)
- Updated the demo preview and its format for faster loading by [@TheoMeunier](https://github.com/TheoMeunier) in [#171](https://github.com/TheoMeunier/uptime-kotlin/pull/171) and [#174](https://github.com/TheoMeunier/uptime-kotlin/pull/174)

### Maintenance
- Removed the temporary deployment trigger of the demo workflow by [@TheoMeunier](https://github.com/TheoMeunier) in [#165](https://github.com/TheoMeunier/uptime-kotlin/pull/165)

**Full Changelog**: [v1.4.1...v1.4.2](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.4.1...v1.4.2)

## [v1.4.1](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.4.1) - 2026-10-13

### Bugs and Fixed
* fix: add compatibility flag to native build arguments in application.… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/161

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.4.0...v1.4.1

## [v1.4.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.4.0) - 2026-10-10

### New Features

Added Telegram notification support by @TheoMeunier in [#146](https://github.com/TheoMeunier/uptime-kotlin/pull/146)
Added Gotify notification support by @TheoMeunier in [#154](https://github.com/TheoMeunier/uptime-kotlin/pull/154)
Added status page management with CRUD operations and configurable layouts by @TheoMeunier in [#150](https://github.com/TheoMeunier/uptime-kotlin/pull/150)
Added an onboarding tour with guided steps and completion tracking by @TheoMeunier in [#148](https://github.com/TheoMeunier/uptime-kotlin/pull/148)
Added configurable notification date formatting with timezone support by @TheoMeunier in [#143](https://github.com/TheoMeunier/uptime-kotlin/pull/143)
Added configurable probe timeouts by @TheoMeunier in [#141](https://github.com/TheoMeunier/uptime-kotlin/pull/141)
Added notification timeout handling and HTTP client refactoring by @TheoMeunier in [#142](https://github.com/TheoMeunier/uptime-kotlin/pull/142)

### Improvements

Replaced LocalDateTime with Instant for more reliable time handling by @TheoMeunier in [#140](https://github.com/TheoMeunier/uptime-kotlin/pull/140)
Added configurable grid and list layouts for status displays by @TheoMeunier in [#149](https://github.com/TheoMeunier/uptime-kotlin/pull/149)
Improved JWT expiration handling and implemented refresh token locking to prevent concurrent refresh issues by @TheoMeunier in [#145](https://github.com/TheoMeunier/uptime-kotlin/pull/145)
Improved ping monitoring with corrected IP handling and default intervals by @TheoMeunier in [#152](https://github.com/TheoMeunier/uptime-kotlin/pull/152)
Improved ping response time accuracy by @TheoMeunier in [#157](https://github.com/TheoMeunier/uptime-kotlin/pull/157)
Improved public status page performance with expiration-based caching by @TheoMeunier in [#158](https://github.com/TheoMeunier/uptime-kotlin/pull/158)

### Bug Fixes & Validation

Added validation for the maximum number of probe IDs in status page requests by @TheoMeunier in [#153](https://github.com/TheoMeunier/uptime-kotlin/pull/153)
Reliability & Error Handling
Implemented graceful shutdown for executors with timeout handling by @TheoMeunier in [#151](https://github.com/TheoMeunier/uptime-kotlin/pull/151)
Improved worker heartbeat cleanup with rollback handling for more reliable state management by @TheoMeunier in [#159](https://github.com/TheoMeunier/uptime-kotlin/pull/159)

### Security

Implemented a secret re-encryption service and improved encryption handling by @TheoMeunier in [#156](https://github.com/TheoMeunier/uptime-kotlin/pull/156)

### Maintenance

Added AGENT.md with development guidelines, architecture preferences, and working conventions by @TheoMeunier in [#155](https://github.com/TheoMeunier/uptime-kotlin/pull/155)
Improved notification code readability and cleaned up comments by @TheoMeunier in [#147](https://github.com/TheoMeunier/uptime-kotlin/pull/147)

**Full Changelog**: [[v1.3.0...v1.4.0](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.3.0...v1.4.0)](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.3.0...v1.4.0)

## [v1.3.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.3.0) - 2026-10-05

### What's Changed

* feat: update README for improved project description and configuratio… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/127
* Doc/readme by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/128
* Doc/readme by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/129
* feat: update README with additional CI badges and correct JDBC URL by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/130
* feat: update Docker configuration in README to correct port mapping by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/131
* feat: add placeholder text for alert repeat seconds in multiple langu… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/132
* feat: add placeholder text for alert repeat seconds in multiple langu… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/133
* feat: add support for ARM64 builds and update README with ARM64 deplo… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/134
* Fix/first setup redirect to login by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/136
* Feat/managed jwt keys by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/137
* feat/all in one image by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/138
* refactor: reorganize Docker build configurations for multi-architectu… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/139

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.2.2...v1.3.0

## [v1.2.2](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.2.2) - 2026-10-02

### New Features

* Added manual probe check functionality by @TheoMeunier in [#88](https://github.com/TheoMeunier/uptime-kotlin/pull/88)
* Added duplicate monitor functionality with UI support by @TheoMeunier in [#104](https://github.com/TheoMeunier/uptime-kotlin/pull/104)
* Added recovery downtime tracking and notification updates by @TheoMeunier in [#92](https://github.com/TheoMeunier/uptime-kotlin/pull/92)
* Added configurable probe timeout support with validation by @TheoMeunier in [#119](https://github.com/TheoMeunier/uptime-kotlin/pull/119)
* Added per-probe log retention configuration by @TheoMeunier in [#120](https://github.com/TheoMeunier/uptime-kotlin/pull/120)
* Added scheduler strategy configuration and deployment tests by @TheoMeunier in [#114](https://github.com/TheoMeunier/uptime-kotlin/pull/114)
* Added encryption key rotation support by @TheoMeunier in [#117](https://github.com/TheoMeunier/uptime-kotlin/pull/117)

### Improvements

* Added ETag caching support for the probe status endpoint by @TheoMeunier in [#90](https://github.com/TheoMeunier/uptime-kotlin/pull/90)
* Improved probe task processing with enhanced logging and error handling by @TheoMeunier in [#118](https://github.com/TheoMeunier/uptime-kotlin/pull/118)
* Improved HTTP client handling by reusing clients across probe steps by @TheoMeunier in [#109](https://github.com/TheoMeunier/uptime-kotlin/pull/109), [#110](https://github.com/TheoMeunier/uptime-kotlin/pull/110) & [#111](https://github.com/TheoMeunier/uptime-kotlin/pull/111)
* Improved probe interval validation with minimum and maximum limits by @TheoMeunier in [#126](https://github.com/TheoMeunier/uptime-kotlin/pull/126)
* Improved responsive layouts and field variants by @TheoMeunier in [#121](https://github.com/TheoMeunier/uptime-kotlin/pull/121) & [#124](https://github.com/TheoMeunier/uptime-kotlin/pull/124)
* Improved error messages and crash handling across multiple languages by @TheoMeunier in [#115](https://github.com/TheoMeunier/uptime-kotlin/pull/115)
* Updated probe identifiers to use UUIDs across multiple resources by @TheoMeunier in [#89](https://github.com/TheoMeunier/uptime-kotlin/pull/89)

### Bug Fixes

* Fixed the logo image source on the loader page by @TheoMeunier in [#91](https://github.com/TheoMeunier/uptime-kotlin/pull/91)
* Added dedicated DNS query timeout handling by @TheoMeunier in [#125](https://github.com/TheoMeunier/uptime-kotlin/pull/125)
* Added unsaved changes protection for probe configuration by @TheoMeunier in [#105](https://github.com/TheoMeunier/uptime-kotlin/pull/105)

### Reliability & Error Handling

* Added frontend error boundaries with localized error messages by @TheoMeunier in [#108](https://github.com/TheoMeunier/uptime-kotlin/pull/108)
* Improved crash handling and localized error messages across the application by @TheoMeunier in [#115](https://github.com/TheoMeunier/uptime-kotlin/pull/115)
* Replaced the main application loading fallback with the LoaderPage component by @TheoMeunier in [#123](https://github.com/TheoMeunier/uptime-kotlin/pull/123)

### Maintenance

* Added Dependabot configuration for automated dependency updates by @TheoMeunier in [#93](https://github.com/TheoMeunier/uptime-kotlin/pull/93)
* Updated JUnit Jupiter from 5.10.0 to 6.1.3 by @dependabot[bot] in [#103](https://github.com/TheoMeunier/uptime-kotlin/pull/103)
* Updated Flyway PostgreSQL from 12.6.1 to 13.6.0 by @dependabot[bot] in [#102](https://github.com/TheoMeunier/uptime-kotlin/pull/102)
* Updated frontend production dependencies by @dependabot[bot] in [#96](https://github.com/TheoMeunier/uptime-kotlin/pull/96) & [#106](https://github.com/TheoMeunier/uptime-kotlin/pull/106)
* Updated frontend development dependencies by @dependabot[bot] in [#94](https://github.com/TheoMeunier/uptime-kotlin/pull/94)
* Updated GitHub Actions dependencies by @dependabot[bot] in [#95](https://github.com/TheoMeunier/uptime-kotlin/pull/95)
* Updated react-hook-form from 7.88.0 to 7.89.0 by @dependabot[bot] in [#116](https://github.com/TheoMeunier/uptime-kotlin/pull/116)

**Full Changelog**: [v1.2.1...v1.2.2](https://github.com/TheoMeunier/uptime-kotlin/compare/v1.2.1...v1.2.2)

## [v1.2.1](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.2.1) - 2026-09-17

### New Features
* feat: update font from Geist to Space Grotesk for improved typography by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/79
* feat: add breadcrumb navigation component for improved user experience by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/80
* feat: implement tab status indicator with favicon badge and dynamic t… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/81
* feat: add search functionality to sidebar monitors for improved usabi… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/83
* feat: add TLS certificate inspection and related fields for enhanced … by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/84
* Feat/display days expired certificate tls by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/85
* Feat/implementation fr lang by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/86
* feat: add language and theme preferences to user profile by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/87

### Improvements

### Bug Fixes

### Cleanup
* clear project by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/82

### Documentation
* feat: update image formats in README to WebP for improved performance by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/78

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.2.0...v1.2.1

## [v1.2.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.2.0) - 2026-09-15

### New Features
* Added purge functionality for monitor logs by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/59
* Added export functionality for probe logs with CSV download by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/60
* Enhanced dashboard and UI with session management and new components by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/62
* Added current password verification and password update functionality by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/63
* Added max latency configuration with validation and description by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/64
* Added slug confirmation input for monitor deletion by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/65 & https://github.com/TheoMeunier/uptime-kotlin/pull/66
* Implemented maintenance management features (creation, listing, scheduling) by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/68
* Added application-wide health page by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/74
* Added alert repeat functionality for ongoing probe failures by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/75
* Resend notification when a probe fails again by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/76

### Improvements
* Enhanced probe retrieval with notifications and optimized log queries by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/71

### Bug Fixes
* Fixed Nginx/Docker configuration for production by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/67
* Renamed maintenance windows SQL migration file for versioning consistency by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/70

### Cleanup
* Added ESLint integration and updated configuration for improved code quality by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/73
* Set up Playwright end-to-end testing by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/77

### Documentation
* Updated README for improved clarity on monitoring features by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/61
* Updated README with maintenance windows configuration and scheduling by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/72

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.1.1...v1.2.0

## [v1.1.1](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.1.1) - 2026-08-11

### v1.1.0

#### New Features
* feat: implement encryption for connection strings and add related tests by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/57
* feat: add ApiError class and improve error handling in API calls by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/58

#### Bug Fixes
* Fix/add default value for string connexion probe by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/54
* Fix/add new logo view by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/55
* fix: delete old refresh token after generating a new one by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/56

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.1.0...v1.1.1

## [v1.1.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.1.0) - 2026-08-04

### v1.1.0

#### New Features
- Add PostgreSQL monitoring support (health check + configuration) — #46
- Add MySQL / MariaDB monitoring support (health checks + configuration) — #47
- Add Microsoft SQL Server monitoring support (health check + configuration) — #48
- Add Redis monitoring support (health checks + configuration) — #49
- Add SMTP monitoring support (health checks + configuration) — #50
- Add Kafka monitoring support (health checks + configuration) — #51
- Add RabbitMQ monitoring support (health checks + configuration) — #52
- Add HTTP protocol params — #38
- Update logo — #43

#### Improvements
- Update process queue cluster handling — #40, #41
- Enhance CI configuration for building and pushing Docker images — #53

#### Bug Fixes
- Enhance probe update logic to invalidate related queries — #44
- Improve responsiveness and layout of status page components — #45

#### Cleanup
- Remove website project and drop associated CI/CD — #42

#### Documentation
- Update README to reflect changes in scheduler strategy and environment variables — #37

---

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.0.1...v1.1.0

## [v1.0.1](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.0.1) - 2026-07-13

### What's Changed
* feat: add NativeConfig class for reflection registration of SnakeCase… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/36


**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v1.0.0...v1.0.1

## [v1.0.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v1.0.0) - 2026-07-01

### What's Changed
* doc: update README and application.properties to clarify scheduler st… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/35


**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v0.3.0...v1.0.0

## [v0.3.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v0.3.0) - 2026-06-10

### What's Changed
* Feat/web site by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/28
* feat: add robots.txt and sitemap.xml for SEO optimization by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/29
* Fix/seo website by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/30
* Feat/cluster mode by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/31
* fix: update Docker build configurations to simplify paths and environ… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/32
* Fix/ci cd cluster by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/33
* Simplify Docker build configurations and streamline CI/CD workflows by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/34


**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v0.2.0...v0.3.0

## [v0.2.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v0.2.0) - 2026-05-07

### What's Changed
* fix vulnerabilites by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/8
* Refactor ProbeStatus component and enhance monitoring UI by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/9
* Feat/design monitor by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/10
* feat: add URL field to probe schema and display in status component by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/11
* feat: include URL in probe response DTO for enhanced data representation by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/12
* feat: add response time monitoring component and refactor chart display by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/13
* feat: enhance probe monitor log with filtering and improved UI by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/14
* feat: add URL field to probe response schema and display on show page by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/15
* feat: sort probes by name in getProbesLastHour method by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/16
* feat: add sparkline and incident bar data structures, update dashboar… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/17
* feat: refactor Sparkline component to use recharts for improved visua… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/18
* Fix/skeleton all front by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/19
* feat: add SSL certificate validation and warning messages to HTTP probe by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/20
* feat: add DNS record type selection and handling in probe configuration by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/21
* Rename 'url' to 'hostname' and 'ip' in probe forms and requests by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/22
* fix: update incident bar colors for better status representation by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/23
* fix: adjust stale time and add refetching options for probe data by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/24
* feat: implement webhook notification support with configurable method… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/25
* refactor: streamline webhook notification sending logic and improve c… by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/26
* fix: correct image filenames in README for status and monitor pages by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/27


**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/compare/v0.1.0...v0.2.0

## [v0.1.0](https://github.com/TheoMeunier/uptime-kotlin/releases/tag/v0.1.0) - 2026-02-19

### What's Changed
* Feat/testing notifications by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/1
* fix: doc docker compose for production by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/2
* feat: add setting profile and action for notifications by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/3
* add logo and update title by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/4
* Feat/add logo icon by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/5
* Feat/design login page by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/6
* fix: update send properties for password confirmation by @TheoMeunier in https://github.com/TheoMeunier/uptime-kotlin/pull/7

### New Contributors
* @TheoMeunier made their first contribution in https://github.com/TheoMeunier/uptime-kotlin/pull/1

**Full Changelog**: https://github.com/TheoMeunier/uptime-kotlin/commits/v0.1.0
