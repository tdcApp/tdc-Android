# TDC Android — Technical Project Overview & Handover Document

## 📌 Project Summary

**TDC (TIT Developer Community)** is a modern Android application built for student members and mentors of the Technocrats Developer Community to manage classes, live schedules, quizzes, assignments, and notifications. 

The app follows **Clean Architecture & MVVM** with **Jetpack Compose (Material 3)**, **Hilt Dependency Injection**, and connects to a **NestJS + Drizzle ORM + NeonDB (PostgreSQL)** backend.

---

## 🛠️ Tech Stack & Dependencies

| Component | Library / Framework | Purpose |
|---|---|---|
| **UI Framework** | Jetpack Compose (Material 3) | Declarative, modern UI with dark teal theme |
| **Authentication**| Firebase Auth (`23.2.0`, BOM `33.7.0`) | User sign-in/registration with Firebase Cloud Identity |
| **Navigation** | Navigation Compose (`2.8.5`) | Auth-gated, profile-gated & role-based screen navigation |
| **DI** | Hilt (`2.55`) | Dependency injection across view models, repositories & network |
| **Networking** | Retrofit (`2.11.0`) + OkHttp (`4.12.0`) | REST API client with Bearer auth token interceptor |
| **Serialization** | `kotlinx.serialization` (`1.7.3`) | JSON DTO parsing matching backend schemas |
| **Local Cache** | Room Database (`2.6.1`) | Offline quiz questions & pending submission queue |
| **Session & Prefs** | DataStore Preferences (`1.1.1`) | Auth token, Firebase UID, user role, and profile state |
| **Image Loading** | Coil (`2.7.0`) | Async image & banner rendering |
| **Backend** | NestJS + Drizzle ORM + NeonDB | Cloud REST API & PostgreSQL relational database |
| **Build Tools** | AGP (`8.8.2`), Kotlin (`2.1.0`), KSP (`2.1.0-1.0.29`) | Android build & symbol processing with JBR 21 |

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

Students are either provisioned in Firebase Auth or self-registered. Mentor accounts and elevated permissions are provisioned exclusively by TDC Administrators.

```
App Launch
    │
    ▼
SplashScreen
    │
    ├── Has Session? ──No──► LoginScreen
    │                             │
    │                             ▼
    │                        Firebase Auth (signInWithEmailAndPassword)
    │                        (Dev fallback: Mock dev accounts)
    │                             │
    │                             ├── Auth Failed ──► "You're not a part of TDC" Error
    │                             │
    │                             ▼
    │                        Save Firebase UID & Token to DataStore
    │                             │
    ▼                             ▼
Is Profile Complete? (isProfileComplete flag)
    │
    ├── No ────────► SetupScreen (Student Profile Completion)
    │                    │ (Full Name, Mobile Number, Language Batch, Technology Batch)
    │                    ▼
    │               Save Profile ──► Set isProfileComplete = true, role = "student"
    │                                         │
    └── Yes ──────────────────────────────────┴──► MainScreen
                                                     │
                                                     ├── Role == "mentor" / "admin" ──► Mentor UI (Dashboard, Submissions)
                                                     └── Role == "student"          ──► Student UI (Home, Assignments)
```

---

## 📊 Domain Models (ERD Alignment)

All models and DTOs correspond 1:1 with the NeonDB (PostgreSQL) relational schema:

| Model | Key Fields | Description |
|---|---|---|
| `UserProfile` | `id` (Firebase UID PK), `name`, `email`, `mobileNumber` (`phone`), `role`, `languageClassId`, `technologyClassId`, `enrollmentNumber`, `year`, `isProfileComplete` | User account & permissions (`student`, `mentor`, `admin`) |
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

### 1. Dynamic Role-Based Main Shell (`MainScreen.kt`)
The bottom navigation dynamically adjusts based on the authenticated user's role (`UserProfile.isMentor`):
- **Mentor Experience**:
  - **Tab 1: Dashboard (`MentorDashboardScreen.kt`)**: Displays batch overview, active student counts, quick-action buttons (**Create Assignment**, **Create Quiz**), and recent submission feed.
  - **Tab 2: Assignments (`MentorAssignmentsScreen.kt`)**: Filter assignments by batch, view submission statistics (e.g. *12/28 Submitted*), and inspect student repository/deployment URLs.
  - **Tab 0: Profile (`ProfileScreen.kt`)**: Mentor profile details, TDC Mentor status chip, and theme toggles.
- **Student Experience**:
  - **Tab 1: Home (`HomeScreen.kt`)**: Weekly schedule widget, upcoming classes, and active quizzes.
  - **Tab 2: Assignments (`AssignmentsScreen.kt`)**: Ongoing & Past assignments with URL submission modal.
  - **Tab 0: Profile (`ProfileScreen.kt`)**: Student profile card, enrolled language & technology batch badges, and personal details.

### 2. Student Profile Setup (`SetupScreen.kt`)
First-time onboarding for newly registered Firebase users:
- **Full Name**: Student's registered display name.
- **Mobile Number**: Student's contact number (synced to Neon DB `mobile_number` column).
- **Language Batch Selector**: Dropdown to select enrolled programming language track (e.g., C++, Python DSA).
- **Technology Batch Selector**: Dropdown to select enrolled technology track (e.g., Android Development, Web Development).
- **Enrollment Number**: Optional university enrollment number.
- **Admin Notice Banner**: Informs students that elevated permissions/mentor status are managed by TDC Administrators.

### 3. Home Tab Shell (`HomeScreen.kt`)
- **Header Bar**: Community branding (School logo, centered "TDC" title) and an unread Notification Bell icon with red badge navigating to `NotificationsScreen`.
- **Tab Layout**: Strictly **3 tabs** — `General`, `Classes`, and `Quiz`.
  - **General Tab**:
    - **Interactive Weekly Schedule Widget**: 7-day rectangular grid (Sun – Sat) at the top. Shows class count indicators, today indicator, and selected day highlight.
    - **Upcoming Classes**: Shows upcoming classes chronologically starting from today with relative date tags ("Today", "Tomorrow", day of week) and platform links (`📍 Google Meet`, `📍 Discord`).
    - **Upcoming Quizzes**: Interactive quiz cards allowing students to attempt upcoming quizzes directly from the dashboard.
  - **Classes Tab**: Enrolled batch cards with technology/language icons, category chips, and "Enrolled" status badges.
  - **Quiz Tab**: List of active quizzes with target audience tags, creation dates, and direct "Start" actions.

### 4. Assignments Tab (`AssignmentsScreen.kt` & `AssignmentDetailScreen.kt`)
- **Tabs**: *Ongoing* and *Past* assignments.
- **Assignment Detail & Submissions**: Full assignment brief with due date, plus a submission dialog for entering project/repo URLs (`submissionUrl`), tracking submission state and confirmation.

### 5. Full-Screen Notifications (`NotificationsScreen.kt`)
- Accessed via the Home header bell icon.
- Two tabs: **Notifications** (alerts with unread indicators and read/unread toggle) and **Announcements** (community news and batch announcements).

### 6. Quiz Taking & Results (`QuizScreens.kt`)
- **Quiz Taking**: Countdown timer, progress bar, single-choice options (A/B/C/D), question navigation, and auto-submit dialog.
- **Quiz Results**: Score overview card, percentage calculations, and submission timestamp.

---

## 🚀 How to Run & Build

1. Open project in **Android Studio** or **VS Code / Antigravity IDE**.
2. Build toolchain requirements:
   - Java 21: Uses **Android Studio JBR** (`C:\Program Files\Android\Android Studio\jbr`).
   - Automatically detected by `gradlew.bat` or via `gradle.properties` (`org.gradle.java.home`).
3. Build project:
   ```powershell
   .\gradlew.bat assembleDebug
   ```
4. Output APK location:
   `app/build/outputs/apk/debug/app-debug.apk`
