# TDC Android — Changelog

All notable changes to this project will be documented in this file.

---

## [0.1.0] — 2026-08-21

### Added — Full App Scaffold (Phases 1–9)
- **Gradle Setup**: Added Hilt, Navigation Compose, Retrofit, OkHttp, kotlinx.serialization, Room, DataStore, Coil dependencies
- **DI**: Hilt modules (Network, Database, Repository) + `TdcApplication`
- **Theming**: Dark theme with teal/blue accents, Material 3 color schemes, custom typography
- **Shared Components**: TdcCard, TdcButton, TdcTextField, LoadingScreen, ErrorScreen, EmptyStateScreen
- **Auth Flow**: Splash → Login → Signup with mock auth (Firebase placeholder)
- **Navigation**: Auth-gated nav graph, 3-tab bottom navigation (Profile, Home, Notifications)
- **Home Tabs**: General (announcements feed), Classes (enrolled classes + detail), Quiz (list + taking + results)
- **Profile Tab**: User info display, settings placeholder, logout
- **Notifications Tab**: Read/unread notification list with mock data
- **Quiz System**: MCQ support (single + multi answer), timer, auto-submit, "Results not out yet" state
- **Room Database**: Offline quiz question caching, pending submission queue
- **API Surface**: Retrofit interfaces + DTOs stubbed for all endpoints (mock data via repositories)
