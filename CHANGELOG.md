# TDC Android — Changelog

All notable changes to this project will be documented in this file.

---

## [0.2.0] — 2026-09-01

### Added
- **Home Screen Header**: Top header bar with School logo, centered "TDC" title, and Notification Bell icon with red badge indicator for unread notifications.
- **Assignments Feature**:
  - New domain model [`Assignment.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/data/model/Assignment.kt) (`Assignment`, `AssignmentStatus`, `AssignmentAttachment`).
  - New repository [`AssignmentRepository.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/data/repository/AssignmentRepository.kt) with mock data and Hilt binding in `RepositoryModule`.
  - New [`AssignmentsScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentsScreen.kt) with Ongoing and Past tab views.
  - New [`AssignmentDetailScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentDetailScreen.kt) displaying assignment details, attachments, and a `+` Floating Action Button for submissions.
  - ViewModels: [`AssignmentsViewModel.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentsViewModel.kt) and [`AssignmentDetailViewModel.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentDetailViewModel.kt).

### Changed
- **Bottom Navigation**: Replaced "Notifications" bottom tab with "Assignments" tab in [`Routes.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/navigation/Routes.kt) and [`MainScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/main/MainScreen.kt).
- **Tab State Persistence**: Used `rememberSaveable` for bottom navigation tab state in `MainScreen` to preserve selected tab across back navigation.
- **Notifications Screen**: Converted `NotificationsScreen` into a dedicated full-screen destination (`Screen.Notifications`) opened via the Home header bell icon. Wrapped in a `Scaffold` + `TopAppBar` with back navigation support and system status bar inset handling.

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
