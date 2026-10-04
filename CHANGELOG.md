# TDC Android — Changelog

All notable changes to this project will be documented in this file.

---

## [0.3.1] — 2026-10-04

### Added
- **Profile Screen Redesign & Alignment**:
  - Re-styled [ProfileScreen.kt](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/profile/ProfileScreen.kt) to match the exact design language of `HomeScreen` and `AssignmentsScreen` (TDC brand header, school icon, notification bell with unread badge).
  - Modern hero profile card with dynamic initials avatar, student name, email, and role/year/status chips (`STUDENT`, `3rd Year`, `Active`).
  - Added **Enrolled Batches** section displaying student's enrolled **Language Track** (e.g. *Python DSA*) and **Technology Track** (e.g. *Android Dev (Kotlin + Jetpack)*) with corresponding icons and badges.
  - Added categorized **Personal Details** card for phone, enrollment number, academic year, and registered email with clean dividers.
- **Interactive Edit Profile Dialog**:
  - Implemented an `EditProfileDialog` with full validation for Name and Phone number updates.
  - Linked to `ProfileViewModel.updateProfile` and `ProfileRepository.updateProfile` with async loading indicator and snackbar feedback.
- **Dynamic Dark Theme Toggle**:
  - Injected `DataStoreManager` into [MainActivity.kt](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/MainActivity.kt) and observed `isDarkTheme` via `collectAsStateWithLifecycle()`.
  - Profile screen's "Dark Theme" switch now writes directly to DataStore, instantly updating the app theme dynamically in real-time.

---

## [0.3.0] — 2026-10-01

### Added
- **Interactive Weekly Schedule Widget**:
  - Embedded an interactive 7-day week selector (Sun – Sat) at the top of the **General** home tab.
  - Square/rectangular day cells with real-time class counters, distinct highlights for **Today**, and active **Selected Day** styling.
  - Tapping any day dynamically reveals that day's scheduled sessions with timings and platform tags (`📍 Google Meet`, `📍 Discord`, `📍 Zoom`).
- **Upcoming Classes & Quizzes on Dashboard**:
  - Chronological "Upcoming Classes" section on General screen showing next sessions with relative date chips ("Today", "Tomorrow", day name).
  - Clickable "Upcoming Quizzes" section launching test-taking directly from the dashboard.
  - Tab navigation shortcuts ("All Classes", "All Quizzes") to smoothly switch between Home tabs.
- **First-Time Profile Setup Onboarding**:
  - Added [`SetupScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/auth/SetupScreen.kt) & [`SetupViewModel.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/auth/SetupViewModel.kt) for first-time login profile completion (Full Name, Enrollment Number, College Year dropdown).
  - Extended auth gate pattern in [`SplashScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/auth/SplashScreen.kt) and [`NavGraph.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/navigation/NavGraph.kt): redirects to `Setup` if `isProfileComplete == false`.
- **Assignment URL Submissions**:
  - Implemented URL submission dialog and submission tracking in [`AssignmentDetailViewModel.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentDetailViewModel.kt) and [`AssignmentDetailScreen.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/ui/assignments/AssignmentDetailScreen.kt) (GitHub repo / hosted link submission).
- **New Domain Models & Repositories**:
  - Added [`Schedule.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/data/model/Schedule.kt) & [`ScheduleRepository.kt`](file:///c:/Users/Akash/Desktop/TDC/app/src/main/java/com/bagadbille/tdc/data/repository/ScheduleRepository.kt) for timetable management.
  - Added `AssignmentSubmission` and `QuizSubmission` models matching the backend database schema.

### Changed
- **Home Screen Structure**:
  - Consolidated into strictly **3 tabs**: `General`, `Classes`, and `Quiz` (standalone schedule tab merged directly into General dashboard).
  - Relocated announcement feeds from General screen into the dedicated `NotificationsScreen` (with Notifications and Announcements tabs).
- **Domain Models & ERD Realignment (NeonDB + Drizzle ORM)**:
  - `UserProfile`: Added `role` (student/mentor/admin), `phone`, `enrollmentNumber`, `year`, `isProfileComplete`. Removed deprecated `avatar` and `section`.
  - `Quiz` & `QuizQuestion`: Updated to single-answer MCQ schema (`optionA`..`optionD` + `correctAnswer`), matching the relational schema.
  - `Assignment`: Simplified schema with URL-based submissions (`submissionUrl`) rather than raw file attachments.
  - `ClassInfo`: Streamlined to `name`, `type` (language, technology), and timestamps.
- **Branding & Context Refinement**:
  - Splash screen branding updated to "TIT Developer Community".
  - All mock data updated from high-school subjects to TDC developer community context (C++ Fundamentals, Python DSA, Web Dev React, Android Dev Kotlin, LeetCode Challenges, Discord/Google Meet).
- **UI Enhancements**:
  - `ClassesScreen`: Added modern enrolled batch cards with technology/language icons and "Enrolled" status badges.
  - `QuizScreens`: Redesigned quiz cards with audience badges, timestamps, and interactive "Start" actions.

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
