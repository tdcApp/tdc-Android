# TDC Android — Technical Project Overview & Handover Document

## 📌 Project Summary

**TDC (TIT Developer Community)** is a modern Android application built for student members and mentors of the Technocrats Developer Community to manage classes, live schedules, quizzes, assignments, and notifications. 

The app follows **Clean Architecture & MVVM** with **Jetpack Compose (Material 3)**, **Hilt Dependency Injection**, and connects to a **NestJS + Drizzle ORM + NeonDB (PostgreSQL)** backend.

---

## 🛠️ Tech Stack & Dependencies

| Component | Library / Framework | Purpose |
|---|---|---|
| **UI Framework** | Jetpack Compose (Material 3) | Declarative, modern UI with dark teal theme |
| **Navigation** | Navigation Compose (`2.8.5`) | Auth-gated, profile-gated & screen navigation |
| **DI** | Hilt (`2.52`) | Dependency injection across view models, repositories & network |
| **Networking** | Retrofit (`2.11.0`) + OkHttp (`4.12.0`) | REST API client with Bearer auth token interceptor |
| **Serialization** | `kotlinx.serialization` (`1.7.3`) | JSON DTO parsing matching backend schemas |
| **Local Cache** | Room Database (`2.6.1`) | Offline quiz questions & pending submission queue |
| **Session & Prefs** | DataStore Preferences (`1.1.1`) | Auth token, user role, and profile completion persistence |
| **Image Loading** | Coil (`2.7.0`) | Async image & banner rendering |
| **Backend** | NestJS + Drizzle ORM + NeonDB | Cloud REST API & PostgreSQL relational database |
| **Build Tools** | AGP (`8.8.2`), Kotlin (`2.1.0`), KSP (`2.1.0-1.0.29`) | Android build & symbol processing |

---

## 📂 Project Architecture & Directory Structure

The project strictly follows MVVM layering (`View (Composable) → ViewModel → Repository → Remote/Local DataSource`):

```
app/src/main/java/com/bagadbille/tdc/
├── TdcApplication.kt             # Hilt @HiltAndroidApp entry point
├── MainActivity.kt               # @AndroidEntryPoint Activity with Edge-to-Edge
├── navigation/
│   ├── Routes.kt                 # Screen destinations & BottomNavItem enum
│   └── NavGraph.kt               # Auth-gated & profile-completion gated navigation host
├── ui/
│   ├── auth/
│   │   ├── SplashScreen.kt       # Token check & profile completion gate
│   │   ├── LoginScreen.kt        # Email/password authentication
│   │   ├── SetupScreen.kt        # First-login onboarding (Name, Enrollment Number, Year)
│   │   ├── AuthViewModel.kt      # Login & session state management
│   │   └── SetupViewModel.kt     # Profile completion logic
│   ├── main/
│   │   └── MainScreen.kt         # Scaffold with 3-tab Bottom Navigation (Profile, Home, Assignments)
│   ├── home/
│   │   ├── HomeScreen.kt         # Top header (Logo, "TDC", Notification Bell) + 3 Tabs: General, Classes, Quiz
│   │   ├── general/              # Weekly schedule widget, upcoming classes, upcoming quizzes & ViewModel
│   │   ├── classes/              # Enrolled batches with technology/language badges & ViewModel
│   │   └── quiz/                 # Quiz list, full test-taking screen, results screen & ViewModels
│   ├── assignments/              # Ongoing & Past assignments list, detail & URL submission dialog
│   ├── profile/                  # User profile details (Role, Enrollment, Year), settings, and logout
│   ├── notifications/            # Full-screen notifications & announcements feed (accessed via header bell)
│   ├── theme/                    # Color.kt (Teal dark mode), Theme.kt, Type.kt
│   └── components/               # TdcButton, TdcTextField, LoadingScreen, ErrorScreen, EmptyStateScreen
├── data/
│   ├── repository/               # Repositories (Auth, Profile, Schedule, Class, Quiz, Assignment, Notification, Announcement)
│   ├── remote/
│   │   ├── api/                  # Retrofit interfaces (AuthApi, ProfileApi, ScheduleApi, QuizApi, etc.)
│   │   └── dto/                  # Serializable DTOs mapped to NeonDB/Drizzle schema
│   ├── local/
│   │   ├── TdcDatabase.kt        # Room DB definition
│   │   ├── DataStoreManager.kt   # Preference DataStore wrapper (Token, Role, ProfileComplete)
│   │   ├── entity/               # QuizQuestionEntity & PendingSubmissionEntity
│   │   └── dao/                  # QuizDao for local caching & offline queue
│   └── model/                    # Clean domain models matching ERD
└── di/
    ├── NetworkModule.kt          # Retrofit, OkHttp with Auth Interceptor, API providers
    ├── DatabaseModule.kt         # Room DB & DAO providers
    └── RepositoryModule.kt       # Interface-to-Implementation Hilt bindings
```

---

## 🔐 Authentication & Onboarding Gate Flow

Students are initially onboarded through Google Forms (collecting email & phone number) and imported directly into the backend `users` table in NeonDB.

```
App Launch
    │
    ▼
SplashScreen
    │
    ├── Has Token? ──No──► LoginScreen
    │                           │
    │                           ▼
    │                      POST /auth/login (Email + Phone/Password)
    │                           │
    │                           ▼
    │                      Save Token to DataStore
    │                           │
    ▼                           ▼
Is Profile Complete? (isProfileComplete flag)
    │
    ├── No ────────► SetupScreen (First-Time Onboarding)
    │                    │ (Full Name, Enrollment No., Year)
    │                    ▼
    │               PUT /profile/setup ──► Set isProfileComplete = true
    │                                         │
    └── Yes ──────────────────────────────────┴──► MainScreen (Profile, Home, Assignments)
```

---

## 📊 Domain Models (ERD Alignment)

| Model | Key Fields | Description |
|---|---|---|
| `UserProfile` | `id`, `name`, `email`, `phone`, `role`, `enrollmentNumber`, `year`, `isProfileComplete` | User account & permissions (`student`, `mentor`, `admin`) |
| `ClassInfo` | `id`, `name`, `type` (`language`, `technology`), `createdAt` | Batches/Courses offered by TDC |
| `Schedule` | `id`, `teacherUid`, `classId`, `dayOfWeek` (0–6), `startTime`, `endTime`, `subject`, `room` | Weekly timetable sessions with platform links (`Google Meet`, `Discord`, `Zoom`) |
| `Quiz` | `id`, `title`, `description`, `classId`, `teacherUid`, `durationMinutes`, `audience`, `createdAt` | Quizzes & screening tests |
| `QuizQuestion` | `id`, `quizId`, `question`, `optionA`, `optionB`, `optionC`, `optionD`, `correctAnswer` | Single-answer multiple choice questions |
| `QuizSubmission` | `id`, `quizId`, `studentUid`, `answers`, `score`, `totalQuestions`, `submittedAt` | Student test results |
| `Assignment` | `id`, `title`, `description`, `dueAt`, `teacherUid`, `classId`, `createdAt` | Coding assignments and project tasks |
| `AssignmentSubmission`| `assignmentId`, `studentUid`, `submissionUrl`, `submittedAt` | Link-based submission (GitHub repo / deployed site) |
| `Announcement` | `id`, `title`, `content`, `audience`, `teacherUid`, `createdAt` | Community-wide or batch updates |
| `AppNotification` | `id`, `title`, `body`, `type`, `isRead`, `createdAt` | Push / in-app notifications |

---

## 📱 Screen & Feature Details

### 1. Home Tab Shell (`HomeScreen.kt`)
- **Header Bar**: Community branding (School logo, centered "TDC" title) and an unread Notification Bell icon with red badge navigating to `NotificationsScreen`.
- **Tab Layout**: Strictly **3 tabs** — `General`, `Classes`, and `Quiz`.
  - **General Tab**:
    - **Interactive Weekly Schedule Widget**: 7-day rectangular grid (Sun – Sat) at the top. Shows class count indicators, today indicator, and selected day highlight. Tapping any day immediately previews that day's scheduled classes and timing.
    - **Upcoming Classes**: Shows upcoming classes chronologically starting from today with relative date tags ("Today", "Tomorrow", day of week) and platform links (`📍 Google Meet`, `📍 Discord`). Has an "All Classes" shortcut to switch to the Classes tab.
    - **Upcoming Quizzes**: Interactive quiz cards allowing students to attempt upcoming quizzes directly from the dashboard. Has an "All Quizzes" shortcut to switch to the Quiz tab.
  - **Classes Tab**: Enrolled batch cards with technology/language icons, category chips, and "Enrolled" status badges.
  - **Quiz Tab**: List of active quizzes with target audience tags, creation dates, and direct "Start" actions.

### 2. Assignments Tab (`AssignmentsScreen.kt` & `AssignmentDetailScreen.kt`)
- **Tabs**: *Ongoing* and *Past* assignments.
- **Assignment Cards**: Title, short description, due date, and status badges.
- **Assignment Detail & Submissions**: Full assignment brief with due date, plus a submission dialog for entering project/repo URLs (`submissionUrl`), tracking submission state and confirmation.

### 3. Full-Screen Notifications (`NotificationsScreen.kt`)
- Accessed via the Home header bell icon.
- Two tabs: **Notifications** (alerts with unread indicators and read/unread toggle) and **Announcements** (community news and batch announcements shifted here from General).

### 4. Profile Tab (`ProfileScreen.kt`)
- Displays user's name, role badge (`Student`, `Mentor`), email, phone, enrollment number, and college year.
- Dark Theme toggle and Logout action (clears DataStore tokens and redirects to Login).

### 5. Quiz Taking & Results (`QuizScreens.kt`)
- **Quiz Taking**: Countdown timer, progress bar, single-choice options (A/B/C/D), question navigation, and auto-submit dialog.
- **Quiz Results**: Score overview card, percentage calculations, and submission timestamp.

---

## 🚀 How to Run & Build

1. Open project in **Android Studio**.
2. Ensure Gradle JDK points to **Android Studio JBR** (`C:\Program Files\Android\Android Studio\jbr` or via `gradle.properties`).
3. Build project:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. Run on connected Android device or emulator (Shift + F10).
